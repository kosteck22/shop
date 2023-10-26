package pl.zielona_baza.admin.user;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.MethodsUtil;
import pl.zielona_baza.admin.category.CategoryValidationError;
import pl.zielona_baza.admin.category.CategoryValidatorResult;
import pl.zielona_baza.admin.exception.CustomValidationException;
import pl.zielona_baza.common.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserValidator {
    private static final String ERROR_DELIMITER = ";";
    private final List<UserValidationError> errors = new ArrayList<>();

    public UserValidatorResult validate(UserDTO userDTO, MultipartFile multipartFile, UserRepository userRepository, boolean isCreatingNewUser) {
        //Validate password
        if (isCreatingNewUser) {
            if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
                errors.add(UserValidationError.PASSWORD_REQUIRED_FOR_NEW_USER);
            }
        }
        if (!isEmailUnique(userDTO.getId(), userDTO.getEmail(), userRepository)) {
            errors.add(UserValidationError.EMAIL_MUST_BE_UNIQUE);
        }
        if (multipartFile != null && !isFileImg(multipartFile)) {
            errors.add(UserValidationError.FILE_NOT_IMG);
        }

        if (errors.isEmpty()) {
            return UserValidatorResult.success();
        }
        String failureMessage = errors.stream().map(err -> err.message).collect(Collectors.joining(ERROR_DELIMITER));
        return UserValidatorResult.failure(failureMessage);
    }

    private boolean isEmailUnique(Integer id, String email, UserRepository repository) {
        Optional<User> user = repository.findByEmail(email);

        return user.isEmpty() || Objects.equals(user.get().getId(), id);
    }
    private boolean isFileImg(MultipartFile file) {
        return MethodsUtil.isImage(file);
    }
}
