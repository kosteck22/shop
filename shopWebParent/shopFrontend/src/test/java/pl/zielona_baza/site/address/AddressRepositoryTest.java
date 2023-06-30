package pl.zielona_baza.site.address;

import org.assertj.core.util.Streams;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.Address;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.Customer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @Order(1)
    public void testAddNew() {
        //given
        Customer customer = testEntityManager.find(Customer.class, 1);
        Country country = testEntityManager.find(Country.class, 1);
        Address address = Address.builder()
                .firstName("Kamil")
                .lastName("Ostafil")
                .phoneNumber("123451234")
                .addressLine1("Lewakowskiego 7/3")
                .city("Rzeszów")
                .state("Podkarpacie")
                .postalCode("35-125")
                .customer(customer)
                .country(country)
                .defaultForShipping(true)
                .build();

        //when
        Address savedAddress = addressRepository.save(address);

        //then
        assertThat(savedAddress).isNotNull().isInstanceOf(Address.class);
        assertThat(savedAddress.getId()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    public void testFindByCustomer() {
        //given
        Customer customer = testEntityManager.find(Customer.class, 1);

        //when
        List<Address> customerAddresses = addressRepository.findByCustomer(customer);

        //then
        assertThat(customerAddresses).isNotEmpty();
        assertThat(customerAddresses.size()).isGreaterThan(0);
    }

    @Test
    @Order(3)
    public void testFindByIdAndCustomer() {
        //given
        Integer addressId = 1;
        Integer customerId = 1;

        //when
        Address address = addressRepository.findByIdAndCustomer(addressId, customerId);

        //then
        assertThat(address).isNotNull().isInstanceOf(Address.class);
    }

    @Test
    @Order(4)
    public void testDeleteByIdAndCustomer() {
        //given
        Integer addressId = 1;
        Integer customerId = 1;

        //when
        addressRepository.deleteByIdAndCustomer(addressId, customerId);

        //then
        assertThat(addressRepository.findById(addressId)).isEmpty();
    }

    @Test
    public void test() {
        byte[] bytes = new byte[]{65, 66, 100, -65, 123, 124};
        String i = new String(bytes);
        System.out.println(i);
    }

    public static String functionLambda(Function<String, String> fun, String inputString) {
        return fun.apply(inputString);
    }
}




















