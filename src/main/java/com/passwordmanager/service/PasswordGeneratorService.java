package com.passwordmanager.service;

import com.passwordmanager.dto.password.PasswordGenerateRequest;
import com.passwordmanager.dto.password.PasswordGenerateResponse;
import com.passwordmanager.entity.AuditEventType;
import com.passwordmanager.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordGeneratorService {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    private static final String AMBIGUOUS = "il1Lo0O|I";

    private final SecureRandom secureRandom = new SecureRandom();
    private final AuditService auditService;

    public PasswordGenerateResponse generatePassword(PasswordGenerateRequest request, User user, HttpServletRequest httpRequest) {
        StringBuilder charPool = new StringBuilder();
        List<Character> mandatoryChars = new ArrayList<>();

        String lower = filterAmbiguous(LOWERCASE, request.isExcludeAmbiguous());
        String upper = filterAmbiguous(UPPERCASE, request.isExcludeAmbiguous());
        String nums = filterAmbiguous(NUMBERS, request.isExcludeAmbiguous());
        String syms = filterAmbiguous(SYMBOLS, request.isExcludeAmbiguous());

        if (request.isLowercase() && !lower.isEmpty()) {
            charPool.append(lower);
            mandatoryChars.add(lower.charAt(secureRandom.nextInt(lower.length())));
        }
        if (request.isUppercase() && !upper.isEmpty()) {
            charPool.append(upper);
            mandatoryChars.add(upper.charAt(secureRandom.nextInt(upper.length())));
        }
        if (request.isNumbers() && !nums.isEmpty()) {
            charPool.append(nums);
            mandatoryChars.add(nums.charAt(secureRandom.nextInt(nums.length())));
        }
        if (request.isSymbols() && !syms.isEmpty()) {
            charPool.append(syms);
            mandatoryChars.add(syms.charAt(secureRandom.nextInt(syms.length())));
        }

        if (charPool.isEmpty()) {
            charPool.append(lower).append(nums);
        }

        int targetLength = Math.max(8, Math.min(request.getLength(), 128));
        List<Character> passwordChars = new ArrayList<>(mandatoryChars);

        String pool = charPool.toString();
        while (passwordChars.size() < targetLength) {
            passwordChars.add(pool.charAt(secureRandom.nextInt(pool.length())));
        }

        Collections.shuffle(passwordChars, secureRandom);

        StringBuilder generated = new StringBuilder();
        for (char c : passwordChars) {
            generated.append(c);
        }

        double entropy = targetLength * (Math.log(pool.length()) / Math.log(2));
        String strength = calculateStrengthCategory(entropy, targetLength);

        if (user != null) {
            auditService.logEvent(user, AuditEventType.PASSWORD_GENERATED,
                    "Generated password with length " + targetLength, httpRequest);
        }

        return PasswordGenerateResponse.builder()
                .password(generated.toString())
                .length(targetLength)
                .entropyBits(Math.round(entropy * 100.0) / 100.0)
                .strength(strength)
                .build();
    }

    private String filterAmbiguous(String input, boolean exclude) {
        if (!exclude) return input;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (AMBIGUOUS.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String calculateStrengthCategory(double entropy, int length) {
        if (length < 8 || entropy < 30) return "Very Weak";
        if (length < 10 || entropy < 50) return "Weak";
        if (length < 14 || entropy < 70) return "Fair";
        if (length < 18 || entropy < 90) return "Strong";
        return "Very Strong";
    }
}
