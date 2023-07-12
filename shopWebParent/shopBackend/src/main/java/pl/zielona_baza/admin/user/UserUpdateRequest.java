package pl.zielona_baza.admin.user;

import lombok.Getter;
import lombok.Setter;
import pl.zielona_baza.common.entity.Role;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserUpdateRequest {
    @NotBlank
    @Size(min = 2, max = 45)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 45)
    private String lastName;

    private Integer id;
    private String email;

    private Set<Role> roles = new HashSet<>();

    private String photos;

    private String photosImagePath;
}
