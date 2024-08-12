package com.condo.condo.controllers;

import com.condo.condo.dtos.LoginDto;
import com.condo.condo.dtos.ResetPasswordDto;
import com.condo.condo.dtos.SignupDto;
import com.condo.condo.enums.VerifyAction;
import com.condo.condo.payloads.ApiResponse;
import com.condo.condo.payloads.UserResponse;
import com.condo.condo.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(
            summary = "Signup",
            description = "For signup and creating a user"
    )
    public ResponseEntity<ApiResponse<String>> signup(@RequestBody SignupDto signupDto){
        return authService.signup(signupDto);
    }

    @GetMapping("/confirm-email")
    @Operation(summary = "Email confirmation",
            description = "Verifies the email address of a user upon signup")
    public ResponseEntity<ApiResponse<String>> confirmEmail(@RequestParam String token){
        return authService.confirmEmail(token);
    }

    @PostMapping("/login")
    @Operation(summary = "login",
            description = "Authenticates the user with email and password")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginDto loginDto){
        return authService.login(loginDto);
    }

    @GetMapping("send-link")
    @Operation(summary = "Verification link",
            description = "This endpoint sends verification link either for email verification or password reset upon request")
    public ResponseEntity<ApiResponse<String>> sendLink(@RequestParam String email, @RequestParam VerifyAction type){
        return authService.sendLink(email, type);
    }

    @PostMapping("password-reset")
    @Operation(summary = "Password reset",
            description = "Resets the password of a user")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam String token, @RequestBody ResetPasswordDto dto){
        return authService.resetPassword(token, dto);
    }
}
