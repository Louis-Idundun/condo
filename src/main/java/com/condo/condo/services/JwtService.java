package com.condo.condo.services;

import java.util.Map;

public interface JwtService {
    String extractEmailAddressFromToken(String token);
    String generateJwtToken(Map<String, Object> claims, String emailAddress, Long expiryDate);
    Boolean isExpired(String token);
}
