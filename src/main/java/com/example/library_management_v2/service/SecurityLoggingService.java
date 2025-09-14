package com.example.library_management_v2.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;


 // Service för strukturerad säkerhetsloggning
 // Loggar alla viktiga säkerhetshändelser för övervakning och analys

@Service
public class SecurityLoggingService {

    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    // Logga lyckad inloggning
    public void logSuccessfulLogin(String username) {
        String clientInfo = getClientInfo();
        securityLogger.info("SUCCESSFUL_LOGIN | User: {} | {}", username, clientInfo);
        auditLogger.info("LOGIN_SUCCESS | User: {} | Time: {} | {}",
                username, LocalDateTime.now(), clientInfo);
    }

    // Logga misslyckad inloggning

    public void logFailedLogin(String username, String reason) {
        String clientInfo = getClientInfo();
        securityLogger.warn("FAILED_LOGIN | User: {} | Reason: {} | {}", username, reason, clientInfo);
        auditLogger.warn("LOGIN_FAILED | User: {} | Reason: {} | Time: {} | {}",
                username, reason, LocalDateTime.now(), clientInfo);
    }


     // Logga användarregistrering

    public void logUserRegistration(String email) {
        String clientInfo = getClientInfo();
        securityLogger.info("USER_REGISTRATION | Email: {} | {}", email, clientInfo);
        auditLogger.info("REGISTRATION | Email: {} | Time: {} | {}",
                email, LocalDateTime.now(), clientInfo);
    }

    // Logga CSRF-skyddsförsök

    public void logCsrfProtection(String endpoint, String action) {
        String clientInfo = getClientInfo();
        securityLogger.warn("CSRF_PROTECTION | Endpoint: {} | Action: {} | {}",
                endpoint, action, clientInfo);
    }


     // Logga åtkomstförsök till skyddade resurser

    public void logAccessAttempt(String resource, String username, boolean allowed) {
        String clientInfo = getClientInfo();
        String status = allowed ? "ALLOWED" : "DENIED";
        securityLogger.info("ACCESS_ATTEMPT | Resource: {} | User: {} | Status: {} | {}",
                resource, username, status, clientInfo);

        if (!allowed) {
            auditLogger.warn("ACCESS_DENIED | Resource: {} | User: {} | Time: {} | {}",
                    resource, username, LocalDateTime.now(), clientInfo);
        }
    }


    // Logga misstänkta säkerhetsincidenter

    public void logSecurityIncident(String incident, String details) {
        String clientInfo = getClientInfo();
        securityLogger.error("SECURITY_INCIDENT | Type: {} | Details: {} | {}",
                incident, details, clientInfo);
        auditLogger.error("SECURITY_INCIDENT | Type: {} | Details: {} | Time: {} | {}",
                incident, details, LocalDateTime.now(), clientInfo);
    }


    // Logga dataändring (för audit trail)

    public void logDataChange(String entity, String action, String username) {
        String clientInfo = getClientInfo();
        auditLogger.info("DATA_CHANGE | Entity: {} | Action: {} | User: {} | Time: {} | {}",
                entity, action, username, LocalDateTime.now(), clientInfo);
    }

    // Logga utloggning

    public void logLogout(String username) {
        String clientInfo = getClientInfo();
        securityLogger.info("LOGOUT | User: {} | {}", username, clientInfo);
        auditLogger.info("LOGOUT | User: {} | Time: {} | {}",
                username, LocalDateTime.now(), clientInfo);
    }


     // Logga administrativ åtkomst

    public void logAdminAccess(String username, String action) {
        String clientInfo = getClientInfo();
        securityLogger.info("ADMIN_ACCESS | User: {} | Action: {} | {}", username, action, clientInfo);
        auditLogger.info("ADMIN_ACCESS | User: {} | Action: {} | Time: {} | {}",
                username, action, LocalDateTime.now(), clientInfo);
    }


     // Hämta klientinformation för loggning

    private String getClientInfo() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            return String.format("IP: %s | UserAgent: %s",
                    ipAddress, userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "Unknown");
        } catch (Exception e) {
            return "IP: Unknown | UserAgent: Unknown";
        }
    }


    // Hämta klientens IP-adress (hanterar proxy/load balancer)

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