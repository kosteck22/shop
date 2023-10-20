package pl.zielona_baza.common.entity.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import pl.zielona_baza.common.entity.Address;
import pl.zielona_baza.common.entity.Customer;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 45)
    @NotBlank
    @Size(min = 2, max = 45)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 45)
    @NotBlank
    @Size(min = 2, max = 45)
    private String lastName;

    @Column(name = "phone_number", nullable = false, length = 15)
    @NotBlank
    @Pattern(regexp = "[+]{1}(?:[0-9\\-\\(\\)\\/\\.]\\s?){6,15}[0-9]{1}$",
            message = "Enter valid phone number(The numbers should start with a plus sign. " +
                    "It should be followed by Country code and National number.)")
    private String phoneNumber;

    @Column(name = "address_line_1", nullable = false, length = 64)
    @NotBlank
    @Size(min = 9, max = 64)
    private String addressLine1;

    @Column(name = "address_line_2", length = 64)
    @Size(max = 64)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 45)
    @NotBlank
    @Size(min = 3, max = 45)
    private String city;

    @Column(name = "state", nullable = false, length = 45)
    @NotBlank
    @Size(min = 3, max = 45)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 10)
    @NotBlank
    @Size(min = 3, max = 10)
    private String postalCode;

    @Column(nullable = false, length = 45)
    @NotBlank
    @Size(min = 2, max = 45)
    private String country;

    private Date orderTime;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal shippingCost;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal productCost;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal subtotal;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal total;

    @DecimalMax(value = "50.0", inclusive = false)
    @Digits(integer = 2, fraction = 2)
    private float tax;

    private int deliverDays;
    private Date deliverDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderDetail> orderDetails = new HashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("updatedTime ASC")
    private List<OrderTrack> orderTracks = new ArrayList<>();

    public Order(Integer id, Date orderTime, BigDecimal productCost, BigDecimal subtotal, BigDecimal total) {
        this.id = id;
        this.orderTime = orderTime;
        this.productCost = productCost;
        this.subtotal = subtotal;
        this.total = total;
    }

    @Transient
    public String getDestination() {
        StringBuilder destination = new StringBuilder();
        if (city != null && !city.isEmpty()) {
            destination.append(city);
            destination.append(", ");
        }
        if (state != null && !state.isEmpty()) {
            destination.append(state);
            destination.append(", ");
        }
        if (country != null && !country.isEmpty()) {
            destination.append(country);
        }

        return destination.toString();
    }

    @Transient
    public String getProductNames() {
        StringBuilder productNames = new StringBuilder();

        productNames.append("<ul>");

        orderDetails.forEach(detail -> {
            productNames.append("<li>");
            productNames.append(detail.getProduct().getShortName());
            productNames.append("</li>");
        });

        productNames.append("</ul>");

        return productNames.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id.equals(order.id) && firstName.equals(order.firstName) && lastName.equals(order.lastName) && phoneNumber.equals(order.phoneNumber) && addressLine1.equals(order.addressLine1) && Objects.equals(addressLine2, order.addressLine2) && city.equals(order.city) && state.equals(order.state) && postalCode.equals(order.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, phoneNumber, addressLine1, addressLine2, city, state, postalCode);
    }

    public void copyAddressFromCustomer() {
        setFirstName(customer.getFirstName());
        setLastName(customer.getLastName());
        setPhoneNumber(customer.getPhoneNumber());
        setAddressLine1(customer.getAddressLine1());
        setAddressLine2(customer.getAddressLine2());
        setCity(customer.getCity());
        setCountry(customer.getCountry().getName());
        setPostalCode(customer.getPostalCode());
        setState(customer.getState());
    }

    public void copyShippingAddress(Address address) {
        setFirstName(address.getFirstName());
        setLastName(address.getLastName());
        setPhoneNumber(address.getPhoneNumber());
        setAddressLine1(address.getAddressLine1());
        setAddressLine2(address.getAddressLine2());
        setCity(address.getCity());
        setCountry(address.getCountry().getName());
        setPostalCode(address.getPostalCode());
        setState(address.getState());
    }

    @Transient
    public String getShippingAddress() {
        String address = firstName;

        if (lastName != null && !lastName.isEmpty()) address += " " + lastName;

        if (!addressLine1.isEmpty()) address += ", " + addressLine1;

        if (addressLine2 != null && !addressLine2.isEmpty()) address += " " + addressLine2;

        if (!city.isEmpty()) address += ", " + city;

        if (state != null && !state.isEmpty()) address += ", " + state;

        address += ", " + country;

        if (!postalCode.isEmpty()) address += ". Postal Code: " + postalCode;
        if (!phoneNumber.isEmpty()) address += ". Phone Number: " + phoneNumber;

        return address;
    }

    @Transient
    public String getDeliverDateOnForm() {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormatter.format(this.deliverDate);
    }

    public void setDeliverDateOnForm(String dateString) {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.deliverDate = dateFormatter.parse(dateString);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    @Transient
    public String getRecipientName() {
        String name = firstName;

        if (lastName != null && !lastName.isEmpty()) name += " " + lastName;

        return name;
    }

    @Transient
    public String getRecipientAddress() {
        String address = addressLine1;

        if (addressLine2 != null && !addressLine2.isEmpty()) address += " " + addressLine2;

        if (!city.isEmpty()) address += ", " + city;

        if (state != null && !state.isEmpty()) address += ", " + state;

        address += ", " + country;

        if (!postalCode.isEmpty()) address += ". " + postalCode;

        return address;
    }

    @Transient
    public boolean isCOD() {
        return paymentMethod.equals(PaymentMethod.COD);
    }

    @Transient
    public boolean isPicked() {
        return hasStatus(OrderStatus.PICKED);
    }

    @Transient
    public boolean isProcessing() { return hasStatus(OrderStatus.PROCESSING); }

    @Transient
    public boolean isShipping() {
        return hasStatus(OrderStatus.SHIPPING);
    }

    @Transient
    public boolean isDelivered() {
        return hasStatus(OrderStatus.DELIVERED);
    }

    @Transient
    public boolean isReturned() {
        return hasStatus(OrderStatus.RETURNED);
    }

    @Transient
    public boolean isReturnRequested() {
        return hasStatus(OrderStatus.RETURN_REQUESTED);
    }

    public boolean hasStatus(OrderStatus orderStatus) {
        return orderTracks.stream()
                .anyMatch(aTrack -> aTrack.getStatus().equals(orderStatus));
    }
}
