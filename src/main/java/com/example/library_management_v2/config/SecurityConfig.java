package com.example.library_management_v2.config;

import com.example.library_management_v2.config.AuthenticationEventHandlers.CustomAuthenticationFailureHandler;
import com.example.library_management_v2.config.AuthenticationEventHandlers.CustomAuthenticationSuccessHandler;
import com.example.library_management_v2.config.AuthenticationEventHandlers.CustomLogoutSuccessHandler;
import com.example.library_management_v2.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private CustomLogoutSuccessHandler logoutSuccessHandler;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Tillåt alla att komma åt startsidan och publika resurser
                        .requestMatchers("/", "/home", "/public/**").permitAll()

                        // Registrering tillgänglig för icke-authentiserade användare
                        .requestMatchers("/register", "users/register").permitAll()

                        // ADMIN område
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/authors/**").hasRole("ADMIN")

                        // USER och ADMIN område
                        .requestMatchers("/books/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/loans/**").hasAnyRole("USER", "ADMIN")

                        // Alla andra requests kräver autentisering
                        .anyRequest().authenticated()
                )
                // UTÖKAD: Formlogin med anpassade handlers för säkerhetsloggning
                .formLogin(form -> form
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll()
                )
                // UTÖKAD: Logout med anpassad handler för säkerhetsloggning
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .permitAll()
                )

        // CSRF-skydd är aktiverat (inget .disable())
        ;

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Använd vår UserDetailsService från databasen
        authProvider.setUserDetailsService(userDetailsService);

        // Använd BCrypt för lösenordsjämförelse
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }
}
