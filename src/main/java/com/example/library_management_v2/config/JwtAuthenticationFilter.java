package com.example.library_management_v2.config;

import com.example.library_management_v2.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter - vår "säkerhetsvakt" för alla HTTP requests
 *
 * Detta filter körs för VARJE request till vår applikation och kontrollerar:
 * 1. Finns det en JWT-token med i request?
 * 2. Är token giltig och inte utgången?
 * 3. Kan vi identifiera användaren från token?
 *
 * Om allt är okej, "loggar" filtret in användaren för denna specifika request.
 *
 * Tänk på det som en dörrvakt som kollar ditt ID varje gång du vill komma in i byggnaden.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    // Denna metod körs för varje HTTP request till vår applikation
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Steg 1: Försök hämta JWT-token från request
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Kolla om Authorization header finns och börjar med "Bearer "
        // Format: "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // Extrahera själva token (ta bort "Bearer " från början)
            jwt = authorizationHeader.substring(7);

            try {
                // Försök extrahera användarnamnet från token
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Om något går fel med token, logga och fortsätt utan autentisering
                logger.error("Kunde inte extrahera användarnamn från JWT token: " + e.getMessage());
            }
        }

        // Steg 2: Om vi har ett användarnamn OCH användaren inte redan är autentiserad
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                // Hämta användardetaljer från databasen
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Steg 3: Validera token mot användaruppgifterna
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // Token är giltig! Skapa autentiserings-objekt för Spring Security

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,           // Användarinformation
                                    null,                 // Lösenord (behövs inte för JWT)
                                    userDetails.getAuthorities()  // Användarens roller/behörigheter
                            );

                    // Lägg till extra detaljer om request
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Sätt autentisering i Spring Security context
                    // Nu "vet" Spring Security vem användaren är för denna request
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.info("JWT autentisering lyckades för användare: " + username);
                } else {
                    logger.warn("JWT token validering misslyckades för användare: " + username);
                }

            } catch (Exception e) {
                logger.error("Fel vid JWT autentisering: " + e.getMessage());
            }
        }

        // Steg 4: Fortsätt med nästa filter i kedjan
        // Detta är viktigt - det låter request fortsätta till vår controller
        filterChain.doFilter(request, response);
    }


    // Denna metod avgör om filtret ska köras för en specifik request
    // Vi vill inte köra JWT-kontroll för vissa endpoints (som inloggning)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Kör inte JWT-kontroll för:
        String[] excludedPaths = {
                "/auth/login",      // Inloggnings-endpoint
                "/auth/register",   // Registrerings-endpoint
                "/test",           // Test-endpoints
                "/h2-console"      // H2 databas-konsol (om du använder den)
        };

        for (String excludedPath : excludedPaths) {
            if (path.startsWith(excludedPath)) {
                return true;  // Hoppa över JWT-kontroll
            }
        }

        return false;  // Kör JWT-kontroll som vanligt
    }
}