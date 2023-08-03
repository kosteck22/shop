package pl.zielona_baza.admin.user;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.AmazonS3Util;
import pl.zielona_baza.admin.exception.ValidationException;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.common.entity.Role;
import pl.zielona_baza.common.entity.User;

import java.io.IOException;
import java.util.*;

import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.*;
import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.validateSortDir;

@Service
@Transactional
public class UserService {
    private static final int USERS_PER_PAGE = 10;
    private static final List<String> SORTABLE_FIELDS_AVAILABLE = new ArrayList<>(
            List.of("id", "email", "firstName", "lastName", "enabled"));
    private final PasswordEncoder passwordEncoder;
    private final UserDTOMapper userDTOMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(PasswordEncoder passwordEncoder, UserDTOMapper userDTOMapper, UserRepository userRepository, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userDTOMapper = userDTOMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> listAll() {
        return userRepository.findAll(Sort.by("lastName").ascending());
    }

    public void listByPage(Integer pageNumber, String sortField, String sortDir, Integer limit, String keyword, Model model) {
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, USERS_PER_PAGE);
        sortField = validateSortField(sortField, SORTABLE_FIELDS_AVAILABLE, "id");
        sortDir = validateSortDir(sortDir);

        PagingAndSortingHelper helper = new PagingAndSortingHelper( "listUsers", sortField, sortDir, keyword, limit);

        helper.listEntities(pageNumber, userRepository, model, userDTOMapper);
    }

    public List<Role> listRoles() { return roleRepository.findAll(); }

    public void save(UserDTO userDTO, MultipartFile multipartFile) throws UserNotFoundException, ValidationException, IOException {
        Integer userId = userDTO.getId();
        boolean isCreatingNewUser = (userId == null);
        User userFromDB;

        //Check if user exist for given id and get him from DB
        if (!isCreatingNewUser) {
            userFromDB = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User with id %d not found".formatted(userId)));
        } else {
            userFromDB = new User();
        }

        //Validate password
        if (isCreatingNewUser) {
            if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
                throw new ValidationException("Password for new user is required");
            }
        }

        //Validate email
        if (!isEmailUnique(userId, userDTO.getEmail())) {
            throw new ValidationException("Choose another email. This one is already taken");
        }

        userFromDB.setEmail(userDTO.getEmail());
        userFromDB.setFirstName(userDTO.getFirstName());
        userFromDB.setLastName(userDTO.getLastName());
        userFromDB.setEnabled(userDTO.isEnabled());
        userFromDB.setRoles(userDTO.getRoles());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            userFromDB.setPassword(encodedPassword);
        }

        if(!multipartFile.isEmpty()) {
            String fileName = userFromDB.getFirstName() + UUID.randomUUID();
            userFromDB.setPhotos(fileName);

            User savedUser = userRepository.save(userFromDB);

            String uploadDir = "user-photos/" + savedUser.getId();

            //Upload to Amazon S3
            AmazonS3Util.removeFolder(uploadDir);
            AmazonS3Util.uploadFile(uploadDir, fileName, multipartFile.getInputStream());
        } else {
            if (isCreatingNewUser) {
                userFromDB.setPhotos(null);
            }

            userRepository.save(userFromDB);
        }
    }

    public void updateAccount(UserDTO userUpdateRequest, MultipartFile multipartFile, String email) throws UserNotFoundException, IOException {
        User userInDB = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with given email %s".formatted(email)));

        userInDB.setFirstName(userUpdateRequest.getFirstName());
        userInDB.setLastName(userUpdateRequest.getLastName());

        if(!multipartFile.isEmpty()) {
            String fileName = userUpdateRequest.getFirstName() + UUID.randomUUID();
            userInDB.setPhotos(fileName);

            User updatedUser = userRepository.save(userInDB);

            String uploadDir = "user-photos/" + updatedUser.getId();

            //Upload to Amazon S3
            AmazonS3Util.removeFolder(uploadDir);
            AmazonS3Util.uploadFile(uploadDir, fileName, multipartFile.getInputStream());
        } else {
            userRepository.save(userInDB);
        }
    }

    public void delete(Integer id) throws UserNotFoundException {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id %d not found".formatted(id)));

        userRepository.delete(user);

        String userPhotosDir = "user-photos/" + id;
        AmazonS3Util.removeFolder(userPhotosDir);
    }

    public boolean isEmailUnique(Integer id, String email) {
        Optional<User> user = userRepository.findByEmail(email);

        return user.isEmpty() || Objects.equals(user.get().getId(), id);
    }

    public UserDTO getUserById(Integer id) throws UserNotFoundException {
        return userRepository
                .findById(id)
                .map(userDTOMapper)
                .orElseThrow(() -> new UserNotFoundException("Could not find any user with ID %d".formatted(id)));
    }

    public UserDTO updateUserEnabledStatus(Integer id, boolean enabled) throws UserNotFoundException {
        UserDTO user = userRepository
                .findById(id)
                .map(userDTOMapper)
                .orElseThrow(() -> new UserNotFoundException("Could not find any user with ID %d".formatted(id)));

        userRepository.updateEnabledStatus(id, enabled);

        return user;
    }

    public UserDTO getUserByEmail(String email) throws UserNotFoundException {
        return userRepository
                .findByEmail(email)
                .map(userDTOMapper)
                .orElseThrow(() -> new UserNotFoundException("User with the given email %s not found".formatted(email)));
    }
}
