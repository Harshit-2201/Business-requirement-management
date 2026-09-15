package com.passwordmanager.dto.audit;

import com.passwordmanager.entity.AuditEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private AuditEventType eventType;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
