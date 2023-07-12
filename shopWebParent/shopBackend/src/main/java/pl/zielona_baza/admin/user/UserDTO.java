package pl.zielona_baza.admin.user;

import lombok.*;
import pl.zielona_baza.common.entity.Role;

import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;

    @Email
    @Size(min = 7, max = 45)
    @NotBlank
    private String email;

    @Pattern(regexp = "^$|^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,20}$",
            message = "Password must contains at least one digit, one upper case alphabet, " +
                    "one lower case alphabet, one special character, " +
                    "without white spaces and have range between 8 and 20")
    private String password;

    @NotBlank
    @Size(min = 2, max = 45)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 45)
    private String lastName;

    @Size(max = 64)
    private String photos;

    private String photosImagePath;

    private boolean enabled;

    private Set<Role> roles = new HashSet<>();

    private String fullName;
}
