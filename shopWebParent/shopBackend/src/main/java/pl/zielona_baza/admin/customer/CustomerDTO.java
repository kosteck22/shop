package pl.zielona_baza.admin.customer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.zielona_baza.common.entity.Country;

import javax.persistence.Transient;
import java.util.Date;

@Getter
@Setter
@Builder
public class CustomerDTO {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String city;
    private String state;
    private Country country;
    private Date createdAt;
    private boolean enabled;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getAddress() {
        String address = firstName;

        if (lastName != null && !lastName.isEmpty()) address += " " + lastName;

        if (!addressLine1.isEmpty()) address += ", " + addressLine1;

        if (addressLine2 != null && !addressLine2.isEmpty()) address += " " + addressLine2;

        if (!city.isEmpty()) address += ", " + city;

        if (state != null && !state.isEmpty()) address += ", " + state;

        address += ", " + country.getName();

        if (!postalCode.isEmpty()) address += ". Postal Code: " + postalCode;
        if (!phoneNumber.isEmpty()) address += ". Phone Number: " + phoneNumber;

        return address;
    }
}


