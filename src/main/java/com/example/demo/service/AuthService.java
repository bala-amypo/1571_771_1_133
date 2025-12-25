package com.example.demo.service;

import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.RegisterRequestDto;
import com.example.demo.exception.BadRequestException;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto dto) throws BadRequestException;
    AuthResponseDto login(AuthRequestDto dto) throws BadRequestException;
}