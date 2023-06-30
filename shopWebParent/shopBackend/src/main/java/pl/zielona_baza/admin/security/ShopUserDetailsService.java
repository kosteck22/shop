package pl.zielona_baza.admin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.zielona_baza.admin.user.UserRepository;
import pl.zielona_baza.common.entity.User;

public class ShopUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User userByEmail = userRepository.getUserByEmail(email);

        if(userByEmail != null) return new ShopUserDetails(userByEmail);

        throw new UsernameNotFoundException("User " + email + " not found.");
    }
}
