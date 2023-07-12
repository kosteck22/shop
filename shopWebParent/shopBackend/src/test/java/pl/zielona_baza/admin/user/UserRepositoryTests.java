package pl.zielona_baza.admin.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.Role;
import pl.zielona_baza.common.entity.User;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
@TestMethodOrder(OrderAnnotation.class)
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @Order(1)
    public void testCreateNewUserWithOneRole() {
        //given
        Role roleAdmin = entityManager.find(Role.class, 1);
        User user = User.builder()
                .email("kamil.ostafil@gmail.com")
                .password("admin123")
                .firstName("Kamil")
                .lastName("Ostafil")
                .roles(new HashSet<>())
                .build();
        user.addRole(roleAdmin);

        //when
        User savedUser = userRepository.save(user);

        //then
        assertThat(roleAdmin).isNotNull();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser).isInstanceOf(User.class);
        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    public void testCreateNewUserWithTwoRoles() {
        //given
        User user = User.builder()
                .email("danielkugla@wp.com")
                .password("admin123")
                .firstName("Daniel")
                .lastName("Kugla")
                .roles(new HashSet<>())
                .build();
        Role roleEditor = entityManager.find(Role.class, 3);
        Role roleAssistant = entityManager.find(Role.class, 5);
        user.addRole(roleEditor);
        user.addRole(roleAssistant);

        //when
        User savedUser = userRepository.save(user);

        //then
        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    @Order(3)
    public void testGetUserById() {
        //when
        User user = userRepository.findById(1).get();

        //then
        assertThat(user).isNotNull();
    }

    @Test
    @Order(4)
    public void testUpdateUserDetails() {
        //given
        User user = userRepository.findById(1).get();
        user.setEnabled(true);
        user.setEmail("kamil.ostafil@gmail.com");

        //when
        User savedUser = userRepository.save(user);

        //then
        assertThat(savedUser.getEmail()).isEqualTo("kamil.ostafil@gmail.com");
        assertThat(savedUser.isEnabled()).isEqualTo(true);
    }

    @Test
    @Order(5)
    public void testUpdateUserRoles() {
        //given
        User user = userRepository.findById(2).get();
        Role roleEditor = entityManager.find(Role.class, 3);
        int numberOfRoles = user.getRoles().size();
        //when
        user.getRoles().remove(roleEditor);
        //then
        assertThat(numberOfRoles).isEqualTo(2);
        assertThat(user.getRoles().size()).isEqualTo(1);
    }

    @Test
    @Order(6)
    public void testDeleteUser() {
        //given
        Integer userId = 2;

        //when
        userRepository.deleteById(userId);

        //then
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    public void testGetUserByEmailThatNoExist() {
        //given
        String email = "abc@gmail.com";

        //when
        User user = userRepository.findByEmail(email).get();

        //then
        assertThat(user).isNull();
    }

    @Test
    public void testGetUserByEmail() {
        //given
        String email = "kamil.ostafil@gmail.com";

        //when
        User user = userRepository.findByEmail(email).get();

        //then
        assertThat(user).isNotNull();
    }

    @Test
    public void testCountUserByID() {
        //given
        Integer userId = 1;

        //when
        Long result = userRepository.countById(userId);

        //then
        assertThat(result).isNotNull().isGreaterThan(0);
    }

    @Test
    @Order(7)
    public void testDisabledUser() {
        //given
        Integer id = 1;
        boolean isEnabled = false;

        //when
        boolean userIsEnabled = userRepository.findById(id).get().isEnabled();
        userRepository.updateEnabledStatus(id, isEnabled);
        boolean userIsEnabledResult = userRepository.findById(id).get().isEnabled();

        //then
        assertThat(userIsEnabled).isEqualTo(true);
        assertThat(userIsEnabledResult).isEqualTo(false);
    }

    @Test
    @Order(8)
    public void testEnabledUser() {
        //given
        Integer id = 1;
        boolean isEnabled = true;

        //when
        boolean userIsDisabled = userRepository.findById(id).get().isEnabled();
        userRepository.updateEnabledStatus(id, isEnabled);
        boolean userIsEnabledResult = userRepository.findById(id).get().isEnabled();

        //then
        assertThat(userIsDisabled).isEqualTo(false);
        assertThat(userIsEnabledResult).isEqualTo(true);
    }
}
