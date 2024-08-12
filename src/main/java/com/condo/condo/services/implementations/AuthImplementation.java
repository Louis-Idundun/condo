package com.condo.condo.services.implementations;

import com.condo.condo.dtos.LoginDto;
import com.condo.condo.dtos.ResetPasswordDto;
import com.condo.condo.dtos.SignupDto;
import com.condo.condo.enums.Role;
import com.condo.condo.enums.VerifyAction;
import com.condo.condo.exceptions.FlexisafException;
import com.condo.condo.mapper.UserMapper;
import com.condo.condo.models.User;
import com.condo.condo.payloads.ApiResponse;
import com.condo.condo.payloads.UserData;
import com.condo.condo.payloads.UserResponse;
import com.condo.condo.repositories.UserRepository;
import com.condo.condo.services.AuthService;
import com.condo.condo.services.EmailService;
import com.condo.condo.utils.ForgotPasswordTemplate;
import com.condo.condo.utils.SignupEmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthImplementation implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtImplementation jwtImplementation;
    private final EmailService emailService;

    private final Long expire = 900000L;
    protected String generateToken(User user, Long expiryDate) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("first_name", user.getFirstName());
        claims.put("last_name", user.getLastName());
        return jwtImplementation.generateJwtToken(claims, user.getEmailAddress(), expiryDate);
    }


    /**
     * Registers a new user.
     *
     * @param signupDto The DTO containing user signup information.
     * @return ResponseEntity containing ApiResponse with a message indicating successful signup, requesting for email verification or error message if the signup failed.
     * @throws FlexisafException if the provided email address already exists or if the passwords do not match.
     */
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> signup(SignupDto signupDto) {
        Optional<User> userOptional = userRepository.findByEmailAddress(signupDto.emailAddress());
        if(userOptional.isPresent()){
            throw new FlexisafException("Email Address already exists");
        } else if(signupDto.password().equals(signupDto.confirmPassword())){
            User user = UserMapper.mapToUser(signupDto, new User());
            user.setPassword(passwordEncoder.encode(signupDto.password()));
            user.setRole(Role.CUSTOMER);
            User savedUser = userRepository.save(user);

            if(savedUser == null){
                return new ResponseEntity<>(new ApiResponse<>("Registration unsuccessful",
                        HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            }

            emailService.sendEmail(signupDto.emailAddress(), "Verify your email address",
                    SignupEmailTemplate.signup(signupDto.firstName(),
                            generateToken(savedUser, expire)));
        } else{
            throw new FlexisafException("Provided passwords do not match");
        }
        return ResponseEntity.ok(new ApiResponse<>("Check your email for verification link",
                HttpStatus.OK));
    }

    /**
     * @param token The token for verifying the email
     * @return ResponseEntity containing APiResponse with message indicating success or error accordingly
     */
    @Override
    public ResponseEntity<ApiResponse<String>> confirmEmail(String token) {
        String email = jwtImplementation.extractEmailAddressFromToken(token);
        if(email != null){
            if(jwtImplementation.isExpired(token)){
                throw new FlexisafException("Link has expired. Please request for a new link");
            } else{
                User user = userRepository.findByEmailAddress(email).orElseThrow(()
                        -> new FlexisafException("User not found"));
                if(!user.isEnabled()){
                    user.setEnabled(true);
                    userRepository.save(user);

                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(user.getEmailAddress(), user.getPassword())
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    UserResponse userResponse = new UserResponse();
                    userResponse.setAccessToken(generateToken(user, null));
                    userResponse.setUserData(UserMapper.mapToUserData(user, new UserData()));
                    return ResponseEntity.ok(new ApiResponse<>(String.format(
                            "Welcome! '%s'. You have successfully signed up", user.getFirstName()), HttpStatus.OK));
                } else{
                    throw new FlexisafException("Your email address is already verified");
                }
            }
        } else{
            throw new FlexisafException("Link is not properly formatted");
        }
    }

    /**
     * Logs in a user with the provided credentials.
     *
     * @param loginDto The DTO containing user login information.
     * @return ResponseEntity containing ApiResponse with LoginResponse indicating successful login or error message if login failed.
     * @throws FlexisafException() if the user with the provided email address is not found.
     */
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> login(LoginDto loginDto) {
        Optional<User> userOptional = userRepository.findByEmailAddress(loginDto.emailAddress());
        if(userOptional.isPresent()){
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.emailAddress(), loginDto.Password())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserResponse userResponse = new UserResponse();
            userResponse.setAccessToken(generateToken(userOptional.get(), null));
            userResponse.setUserData(UserMapper.mapToUserData(userOptional.get(), new UserData()));

            return ResponseEntity.ok(new ApiResponse<>(userResponse, String.format("Welcome back '%s'. You are now logged in", userOptional.get().getFirstName())));
        } else {
            throw new FlexisafException("User not found");
        }
    }

    /**
     * @param email The email address of the user
     * @param type Enum indicating the type of verification; whether signup or password reset
     * @return ResponseEntity containing ApiResponse indicating success or error accordingly
     */
    @Override
    public ResponseEntity<ApiResponse<String>> sendLink(String email, VerifyAction type) {
        User user = userRepository.findByEmailAddress(email).orElseThrow(()
                -> new FlexisafException("User not found"));
        if(type == VerifyAction.SIGNUP){
            if(user.isEnabled()){
                throw new FlexisafException("Email address is already verified");
            } else{
                emailService.sendEmail(email, "Verify your email address",
                        SignupEmailTemplate.signup(user.getFirstName(),
                                generateToken(user, expire)));

                return ResponseEntity.ok(new ApiResponse<>("Check your email for verification link", HttpStatus.OK));
            }
        } else if(type == VerifyAction.PASSWORD_RESET){
            emailService.sendEmail(email, "Password reset",
                    ForgotPasswordTemplate.password(user.getFirstName(), generateToken(user, expire)));

            user.setPasswordRecovery(true);
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse<>("Check your email for password reset link", HttpStatus.OK));
        } else{
            throw new FlexisafException("Invalid verification type");
        }
    }

    /**
     * @param token Token from which the email address will be extracted
     * @Param dto ResetPasswordDto containing new password and confirmation
     * @return ResponseEntity containing ApiResponse indicating status of the operation whether successful or not
     */
    @Override
    public ResponseEntity<ApiResponse<String>> resetPassword(String token, ResetPasswordDto reset) {
        String email = jwtImplementation.extractEmailAddressFromToken(token);
        if(email != null){
            if(jwtImplementation.isExpired(token)){
                throw new FlexisafException("Link has expired. Please request for a new link");
            } else{
                User user = userRepository.findByEmailAddress(email).orElseThrow(()
                        -> new FlexisafException("User not found"));
                if(!user.getPasswordRecovery()){
                    throw new FlexisafException("Password reset was not initiated");
                } else{
                    if(reset.newPassword().equals(reset.confirmPassword())){
                        user.setPassword(passwordEncoder.encode(reset.newPassword()));
                        user.setPasswordRecovery(false);
                        userRepository.save(user);
                        return ResponseEntity.ok(new ApiResponse<>("Password reset successfully", HttpStatus.OK));
                    } else{
                        throw new FlexisafException("Passwords do not match");
                    }
                }
            }
        } else{
            throw new FlexisafException("Link not properly formatted");
        }
    }
}
