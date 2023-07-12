package pl.zielona_baza.admin.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.zielona_baza.common.entity.Role;
import pl.zielona_baza.common.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.List;


public class ShopUserDetails  implements UserDetails {

    private final User user;

    public ShopUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        List<GrantedAuthority> authorityList = new ArrayList<>();
        roles.forEach(role -> {
            authorityList.add(new SimpleGrantedAuthority(role.getName()));
        });
        return authorityList;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public boolean hasRole(String roleName) {
        return user.hasRole(roleName);
    }
}
