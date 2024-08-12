package com.condo.condo.services;

import com.condo.condo.dtos.LoginDto;
import com.condo.condo.dtos.ResetPasswordDto;
import com.condo.condo.dtos.SignupDto;
import com.condo.condo.enums.VerifyAction;
import com.condo.condo.payloads.ApiResponse;
import com.condo.condo.payloads.UserResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<ApiResponse<String>> signup(SignupDto signupDto);
    ResponseEntity<ApiResponse<String>> confirmEmail(String token);
    ResponseEntity<ApiResponse<UserResponse>> login(LoginDto loginDto);
    ResponseEntity<ApiResponse<String>> sendLink(String email, VerifyAction type);
    ResponseEntity<ApiResponse<String>> resetPassword(String token, ResetPasswordDto dto);
}
