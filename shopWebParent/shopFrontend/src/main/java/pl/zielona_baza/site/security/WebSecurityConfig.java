package pl.zielona_baza.site.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pl.zielona_baza.site.security.oauth.CustomerOAuth2UserService;
import pl.zielona_baza.site.security.oauth.OAuth2LoginSuccessHandler;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private CustomerOAuth2UserService oAuth2UserService;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private DatabaseLoginSuccessHandler databaseLoginSuccessHandler;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //custom auth provider
        http.authenticationProvider(daoAuthenticationProvider());

        //css, img and js access

        http.authorizeRequests()
                .antMatchers(
             "/account_details",
                        "/update_account_details",
                        "/cart",
                        "/orders/**",
                        "/address_book/**",
                        "/checkout",
                        "place_order",
                        "/process_paypal_order",
                        "/reviews/**",
                        "/write_review/product/**",
                        "/post_review").authenticated()
                .antMatchers(
              "/css/**",
                        "/webjars/**",
                        "/images/**",
                        "/js/**").permitAll()
                .anyRequest().permitAll()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .usernameParameter("email")
                    .successHandler(databaseLoginSuccessHandler)
                    .permitAll()
                .and()
                .oauth2Login()
                    .loginPage("/login")
                    .userInfoEndpoint()
                    .userService(oAuth2UserService)
                    .and()
                    .successHandler(oAuth2LoginSuccessHandler)
                .and()
                .logout().permitAll()
                .and()
                .rememberMe()
                    .key("1234567890_aBcDeFgHiJkLMNOpqRsTuVwXuZ")
                    .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(14))
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService());

        return provider;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomerUserDetailsService();
    }
}
