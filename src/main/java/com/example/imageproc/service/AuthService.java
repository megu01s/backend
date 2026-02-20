package com.example.imageproc.service;

import com.example.imageproc.entity.UserEntity;
import com.example.imageproc.dto.LoginRequest;
import com.example.imageproc.dto.SignUpRequest;

public interface AuthService {

    UserEntity register(SignUpRequest request);

    String login(LoginRequest request);

    UserEntity getCurrentUser();
}
