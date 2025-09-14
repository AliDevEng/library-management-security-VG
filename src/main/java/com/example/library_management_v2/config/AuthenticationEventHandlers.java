package com.example.library_management_v2.config;

import com.example.library_management_v2.service.SecurityLoggingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


 // Handlers för att logga autentiseringshändelser
 // Dessa triggas automatiskt av Spring Security vid inloggning/utloggning

@Component
public class AuthenticationEventHandlers {

    @Autowired
    private SecurityLoggingService securityLoggingService;


    // Handler för lyckad inloggning, sker automatisk vid framgångsrik inloggning

    @Component
    public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {

            // Logga lyckad inloggning
            String username = authentication.getName();
            securityLoggingService.logSuccessfulLogin(username);

            // Kontrollera om det är admin-inloggning
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                securityLoggingService.logAdminAccess(username, "LOGIN");
            }

            // Omdirigera till standard success URL (Spring Security default)
            response.sendRedirect("/");
        }
    }

    // Handler för misslyckad inloggning, sker automatisk vid framgångsrik inloggning
    @Component
    public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

        @Override
        public void onAuthenticationFailure(HttpServletRequest request,
                                            HttpServletResponse response,
                                            AuthenticationException exception) throws IOException, ServletException {

            // Försök att hämta användarnamn från request
            String attemptedUsername = request.getParameter("username");
            if (attemptedUsername == null) {
                attemptedUsername = "unknown";
            }

            // Logga misslyckad inloggning med orsak
            String failureReason = exception.getClass().getSimpleName();
            securityLoggingService.logFailedLogin(attemptedUsername, failureReason);

            // Omdirigera till inloggningssida med felmeddelande
            response.sendRedirect("/login?error=true");
        }
    }

    // Handler för utloggning, sker automatisk vid framgångsrik inloggning
    @Component
    public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

        @Override
        public void onLogoutSuccess(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication) throws IOException, ServletException {

            if (authentication != null) {
                String username = authentication.getName();
                securityLoggingService.logLogout(username);

                // Kontrollera om det var admin som loggade ut
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                    securityLoggingService.logAdminAccess(username, "LOGOUT");
                }
            }

            // Omdirigera till startsidan
            response.sendRedirect("/?logout=true");
        }
    }
}