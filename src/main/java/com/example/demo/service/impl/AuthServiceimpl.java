package com.example.demo.service.impl;

import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.RegisterRequestDto;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.UserAccountRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserAccountRepository userAccountRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDto register(RegisterRequestDto dto) {
        // Check if email already exists
        if (userAccountRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }
        
        // Create new user
        UserAccount user = new UserAccount();
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        
        userAccountRepository.save(user);
        
        // Generate token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        
        String token = jwtUtil.generateToken(claims, user.getEmail());
        return new AuthResponseDto(token, String.valueOf(jwtUtil.getExpirationMillis()));
    }

    @Override
    public AuthResponseDto login(AuthRequestDto dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            
            UserAccount user = userAccountRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new BadRequestException("Invalid credentials"));
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("role", user.getRole());
            
            String token = jwtUtil.generateToken(claims, user.getEmail());
            return new AuthResponseDto(token, String.valueOf(jwtUtil.getExpirationMillis()));
            
        } catch (Exception e) {
            throw new BadRequestException("Invalid email or password");
        }
    }
}