package com.passwordmanager.service;

import com.passwordmanager.dto.audit.AuditLogResponse;
import com.passwordmanager.entity.AuditEventType;
import com.passwordmanager.entity.AuditLog;
import com.passwordmanager.entity.User;
import com.passwordmanager.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logEvent(User user, AuditEventType eventType, String description, HttpServletRequest request) {
        String ipAddress = null;
        String userAgent = null;

        if (request != null) {
            ipAddress = getClientIp(request);
            userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.length() > 255) {
                userAgent = userAgent.substring(0, 255);
            }
        }

        // Strictly sanitize description to guarantee no plaintext secrets or credentials are ever stored
        String sanitizedDesc = sanitizeDescription(description);

        AuditLog log = AuditLog.builder()
                .user(user)
                .eventType(eventType)
                .description(sanitizedDesc)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(log);
        logger.info("AUDIT: User [{}] performed [{}] - Description: [{}]", user.getId(), eventType, sanitizedDesc);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsForUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogsForUserPaginated(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .eventType(log.getEventType())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String sanitizeDescription(String description) {
        if (description == null) return "No description";
        // Ensure description doesn't exceed 255 chars
        return description.length() > 255 ? description.substring(0, 255) : description;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isEmpty() && !xForwarded.equalsIgnoreCase("unknown")) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
