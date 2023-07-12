package pl.zielona_baza.admin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.zielona_baza.admin.user.UserRepository;
import pl.zielona_baza.common.entity.User;

public class ShopUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public ShopUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User userByEmail = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(email)));

        return new ShopUserDetails(userByEmail);
    }
}
