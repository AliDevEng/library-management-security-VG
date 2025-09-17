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

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
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
    */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF-skydd kan inaktiveras för JWT eftersom vi inte använder cookies
                .csrf(csrf -> csrf.disable())

                // SESSION MANAGEMENT: DET VIKTIGA STEGET!
                // Säg till Spring Security att INTE skapa eller använda sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // AUTHORIZATION RULES: Vilka endpoints kräver autentisering
                .authorizeHttpRequests(authz -> authz

                        // Publika endpoints (ingen autentisering krävs)
                        .requestMatchers("/auth/**").permitAll()        // Inloggning & registrering
                        .requestMatchers("/test").permitAll()           // Test-endpoint
                        .requestMatchers("/test/**").permitAll()        // Alla test-endpoints

                        // ADMIN-ONLY områden - endast ADMIN får komma åt
                        .requestMatchers("/admin/**").hasRole("ADMIN")      // Admin-panelen
                        .requestMatchers("/users/**").hasRole("ADMIN")      // Användarhantering
                        .requestMatchers("/authors/**").hasRole("ADMIN")    // Författarhantering

                        // USER och ADMIN områden - både USER och ADMIN får komma åt
                        .requestMatchers("/books/**").hasAnyRole("USER", "ADMIN")    // Böcker
                        .requestMatchers("/loans/**").hasAnyRole("USER", "ADMIN")    // Lån

                        // Alla andra requests kräver autentisering
                        .anyRequest().authenticated()
                )


                // LÄGG TILL VÅRT JWT-FILTER
                // Detta filter körs FÖRE Spring Security's vanliga autentisering
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
