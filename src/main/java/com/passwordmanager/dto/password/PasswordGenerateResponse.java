package com.passwordmanager.dto.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordGenerateResponse {
    private String password;
    private int length;
    private double entropyBits;
    private String strength;
}
