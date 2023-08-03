package pl.zielona_baza.admin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pl.zielona_baza.admin.user.UserRepository;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final UserRepository userRepository;

    public WebSecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //custom auth provider
        http.authenticationProvider(authenticationProvider());

        //css, img and js access
        http.authorizeRequests()
                .antMatchers(
             "/css/**",
                        "/webjars/**",
                        "/images/**",
                        "/fontawesome/**",
                        "/js/**",
                        "/webfonts/**").permitAll();

        http.authorizeRequests(auth -> {
            auth
                    .mvcMatchers(
                    "/states/list_by_country/**").hasAnyAuthority("Admin", "Salesperson")
                    .mvcMatchers(
                    "/users/**",
                            "/settings/**",
                            "/countries/**",
                            "/states/**").hasAuthority("Admin")
                    .mvcMatchers(
                    "/products/**",
                            "/categories/**",
                            "/brands/**").hasAnyAuthority("Admin", "Editor")
                    .mvcMatchers(
                    "/products/new",
                            "/products/delete/**").hasAnyAuthority("Admin", "Editor")
                    .mvcMatchers(
                  "/products/edit/**",
                           "/products/save",
                           "/products/check_unique").hasAnyAuthority("Admin", "Editor", "Salesperson")
                    .mvcMatchers(
                   "/products",
                            "/products/",
                            "/products/detail/**",
                            "/products/page/**").hasAnyAuthority("Admin", "Editor", "Salesperson", "Shipper")
                    .mvcMatchers(
                            "/products/detail/**",
                            "/customers/detail/**").hasAnyAuthority("Admin", "Editor", "Salesperson", "Assistant")
                    .mvcMatchers(
                   "/orders",
                            "/orders/page/**",
                            "/orders/detail/**").hasAnyAuthority("Admin", "Salesperson", "Shipper")
                    .mvcMatchers(
                   "/customers/**",
                            "/orders/**",
                            "/shipping_rates/**",
                            "/get_shipping_cost",
                            "/search_product",
                            "/reports/**").hasAnyAuthority("Admin", "Salesperson")
                    .mvcMatchers(
                    "/orders_shipper/update/**").hasAnyAuthority("Shipper")
                    .mvcMatchers(
                       "/reviews/**").hasAnyAuthority("Admin", "Assistant")

                    .anyRequest().authenticated();
        });

        //login, logout, remember me
        http.formLogin()
                .loginPage("/login")
                .usernameParameter("email")
                .permitAll()
        .and()
                .logout().deleteCookies("JSESSIONID")
        .and()
                .rememberMe()
                    .key("AbcDEFghIjKlMNopQrS_1234567890");

        http.headers().frameOptions().sameOrigin();

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService());

        return authenticationProvider;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new ShopUserDetailsService(userRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
