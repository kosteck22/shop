package pl.zielona_baza.admin.user;

import org.springframework.stereotype.Service;
import pl.zielona_baza.admin.DTOMapper;
import pl.zielona_baza.common.entity.User;

@Service
public class UserDTOMapper implements DTOMapper<User, UserDTO> {

    @Override
    public UserDTO apply(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .photos(user.getPhotos())
                .photosImagePath(user.getPhotosImagePath())
                .enabled(user.isEnabled())
                .roles(user.getRoles())
                .fullName(user.getFullName())
                .build();
    }
}
