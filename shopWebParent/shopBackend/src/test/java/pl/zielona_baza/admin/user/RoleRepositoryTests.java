package pl.zielona_baza.admin.user;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import pl.zielona_baza.common.entity.Role;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(value = false)
@TestMethodOrder(OrderAnnotation.class)
public class RoleRepositoryTests {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @Order(1)
    public void testCreateAdminRole() {
        //given
        Role role = Role.builder()
                .name("Admin")
                .description("access to everything")
                .build();
        //when
        Role savedRole = roleRepository.save(role);

        //then
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isGreaterThan(0);
        assertThat(savedRole.getName()).isEqualTo("Admin");
    }

    @ParameterizedTest
    @MethodSource("rolesForUsers")
    @Order(2)
    public void testCreateRoles(String roleName, String description) {
        //given
        Role role = Role.builder()
                .name(roleName)
                .description(description)
                .build();

        //when
        Role savedRole = roleRepository.save(role);

        //then
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isGreaterThan(0);
        assertThat(savedRole.getName()).isEqualTo(roleName);
        assertThat(savedRole.getDescription()).isEqualTo(description);
    }

    private static Stream<Arguments> rolesForUsers() {
        return Stream.of(
                Arguments.of("Salesperson", "manage product price, customers, shipping, orders, and sales report"),
                Arguments.of("Editor", "manage categories, brands, products, articles and menus"),
                Arguments.of("Shipper", "view products, view orders and update order status"),
                Arguments.of("Assistant", "manage questions and reviews")
        );
    }
}
