package edu.franklin.cecas.service;

import edu.franklin.cecas.dto.AuthResponse;
import edu.franklin.cecas.dto.LoginRequest;
import edu.franklin.cecas.dto.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);
}
