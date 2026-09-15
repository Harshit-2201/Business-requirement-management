package com.passwordmanager.controller;

import com.passwordmanager.dto.audit.AuditLogResponse;
import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.security.UserPrincipal;
import com.passwordmanager.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "9. Audit Module", description = "Security event logs (LOGIN, LOGIN_FAILED, VAULT_UNLOCKED, PASSWORD_VIEWED, CREDENTIAL events). Zero plaintexts.")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Get Audit Logs", description = "Fetches immutable audit logs of user security actions. Supports pagination.")
    public ResponseEntity<ApiResponse<?>> getAuditLogs(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Return unpaged complete list if true") @RequestParam(defaultValue = "false") boolean all) {

        if (all) {
            List<AuditLogResponse> logs = auditService.getLogsForUser(userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", logs));
        }

        Page<AuditLogResponse> pagedLogs = auditService.getLogsForUserPaginated(
                userPrincipal.getId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(ApiResponse.success("Paginated audit logs retrieved", pagedLogs));
    }
}
