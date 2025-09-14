package com.example.library_management_v2.controller;

import com.example.library_management_v2.service.SecurityLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Anpassad felhanterare som ersätter Spring Boots standard "Whitelabel Error Page"
 * Visar säkra, professionella felmeddelanden utan att avslöja systemdetaljer
 */
@Controller
public class CustomErrorController implements ErrorController {

    @Autowired
    private SecurityLoggingService securityLoggingService;

    /**
     * Huvud-endpoint för alla fel
     * Denna metod anropas automatiskt när fel uppstår i applikationen
     */
    @RequestMapping("/error")
    @ResponseBody
    public Map<String, Object> handleError(HttpServletRequest request) {

        // Hämta statuskod för att avgöra typ av fel
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        Integer statusCode = (status != null) ? (Integer) status : 500;

        // Hämta original URL som orsakade felet
        String originalUrl = (String) request.getAttribute("jakarta.servlet.error.request_uri");

        // Generera unikt fel-ID för spårning
        String errorId = UUID.randomUUID().toString().substring(0, 8);

        // Logga felet för säkerhetsanalys
        logSecurityEvent(statusCode, originalUrl, errorId, request);

        // Skapa säkert svar baserat på feltyp
        return createErrorResponse(statusCode, errorId, originalUrl);
    }

    /**
     * Loggar säkerhetshändelser baserat på feltyp
     */
    private void logSecurityEvent(Integer statusCode, String originalUrl, String errorId, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);

        switch (statusCode) {
            case 403:
                // 403 Forbidden - potentiell obehörig åtkomst
                securityLoggingService.logSecurityIncident("ACCESS_FORBIDDEN",
                        String.format("403 error for URL: %s | ErrorID: %s | %s", originalUrl, errorId, clientInfo));
                break;

            case 404:
                // 404 Not Found - kan indikera reconnaissance attacker
                securityLoggingService.logSecurityIncident("RESOURCE_NOT_FOUND",
                        String.format("404 error for URL: %s | ErrorID: %s | %s", originalUrl, errorId, clientInfo));
                break;

            case 500:
                // 500 Internal Server Error - systemfel som bör utredas
                securityLoggingService.logSecurityIncident("INTERNAL_ERROR",
                        String.format("500 error for URL: %s | ErrorID: %s | %s", originalUrl, errorId, clientInfo));
                break;

            default:
                // Andra felkoder
                securityLoggingService.logSecurityIncident("HTTP_ERROR",
                        String.format("%d error for URL: %s | ErrorID: %s | %s", statusCode, originalUrl, errorId, clientInfo));
        }
    }

    /**
     * Skapar säkra felmeddelanden som inte avslöjar systemdetaljer
     */
    private Map<String, Object> createErrorResponse(Integer statusCode, String errorId, String originalUrl) {
        Map<String, Object> response = new HashMap<>();

        // Grundläggande information
        response.put("timestamp", LocalDateTime.now());
        response.put("errorId", errorId);
        response.put("status", statusCode);

        // Säkra, användarvänliga meddelanden baserat på feltyp
        switch (statusCode) {
            case 403:
                response.put("error", "Åtkomst nekad");
                response.put("message", "Du har inte behörighet att komma åt denna resurs. Logga in med ett konto som har rätt behörigheter.");
                response.put("suggestion", "Kontakta administratör om du tror att du borde ha åtkomst till denna sida.");
                break;

            case 404:
                response.put("error", "Sidan hittades inte");
                response.put("message", "Den begärda resursen kunde inte hittas på servern.");
                response.put("suggestion", "Kontrollera att URL:en är korrekt eller gå tillbaka till startsidan.");
                break;

            case 405:
                response.put("error", "Metod inte tillåten");
                response.put("message", "HTTP-metoden som användes är inte tillåten för denna resurs.");
                response.put("suggestion", "Kontrollera API-dokumentationen för korrekta HTTP-metoder.");
                break;

            case 500:
                response.put("error", "Internt serverfel");
                response.put("message", "Ett oväntat fel inträffade på servern. Problemet har loggats och kommer att utredas.");
                response.put("suggestion", "Försök igen om en stund. Kontakta support om problemet kvarstår.");
                break;

            case 503:
                response.put("error", "Tjänsten inte tillgänglig");
                response.put("message", "Servern är tillfälligt otillgänglig på grund av underhåll eller överbelastning.");
                response.put("suggestion", "Försök igen om några minuter.");
                break;

            default:
                response.put("error", "Ett fel inträffade");
                response.put("message", "Ett oväntat fel inträffade vid behandling av din begäran.");
                response.put("suggestion", "Försök igen eller kontakta support om problemet kvarstår.");
        }

        // Hjälpsam information utan att avslöja systemdetaljer
        response.put("supportInfo", Map.of(
                "errorId", errorId,
                "timestamp", LocalDateTime.now(),
                "helpText", "Ange fel-ID och tidsstämpel när du kontaktar support"
        ));

        return response;
    }

    /**
     * Hämtar klientinformation för loggning
     */
    private String getClientInfo(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        return String.format("IP: %s | UserAgent: %s",
                ipAddress, userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "Unknown");
    }

    /**
     * Hämtar klientens IP-adress (hanterar proxy/load balancer)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}