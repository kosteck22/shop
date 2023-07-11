package pl.zielona_baza.admin.customer;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
public class CustomerUpdateRequest {
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

    @NotBlank
    @Pattern(regexp = "[+]{1}(?:[0-9\\-\\(\\)\\/\\.]\\s?){6,15}[0-9]{1}$",
             message = "Enter valid phone number(The numbers should start with a plus sign. " +
                       "It should be followed by Country code and National number.)")
    private String phoneNumber;

    @NotBlank
    @Size(min = 9, max = 64)
    private String addressLine1;

    @Size(max = 64)
    private String addressLine2;

    @NotBlank
    @Size(min = 3, max = 10)
    private String postalCode;

    @NotBlank
    @Size(min = 3, max = 45)
    private String city;

    @NotBlank
    @Size(min = 3, max = 45)
    private String state;

    @Positive
    private Integer country;
}
