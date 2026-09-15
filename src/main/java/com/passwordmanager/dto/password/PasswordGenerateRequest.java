package com.passwordmanager.dto.password;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordGenerateRequest {

    @Min(value = 8, message = "Length must be at least 8")
    @Max(value = 128, message = "Length cannot exceed 128")
    @Builder.Default
    private int length = 16;

    @Builder.Default
    private boolean uppercase = true;

    @Builder.Default
    private boolean lowercase = true;

    @Builder.Default
    private boolean numbers = true;

    @Builder.Default
    private boolean symbols = true;

    @Builder.Default
    private boolean excludeAmbiguous = false;
}
