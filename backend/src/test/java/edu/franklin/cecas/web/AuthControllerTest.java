package edu.franklin.cecas.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import edu.franklin.cecas.config.SecurityConfig;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.dto.CurrentUserResponse;
import edu.franklin.cecas.dto.LoginRequest;
import edu.franklin.cecas.dto.RegisterRequest;
import edu.franklin.cecas.exception.EmailAlreadyExistsException;
import edu.franklin.cecas.exception.InvalidCredentialsException;
import edu.franklin.cecas.service.AuthService;
import edu.franklin.cecas.service.CecasUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(controllers = { AuthController.class })
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
public class AuthControllerTest {

        @MockitoBean
        private CecasUserDetailsService cecasUserDetailsService;

        @MockitoBean
        private AuthService authService;

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        void testRegisterUser() throws Exception {

                RegisterRequest request = new RegisterRequest();
                request.setEmail("student@test.com");
                request.setPassword("Password123!");
                request.setFullName("Test Student");
                request.setProgram("Computer Science");
                request.setRole(UserRole.STUDENT);

                CurrentUserResponse response = new CurrentUserResponse(
                                true,
                                "student@test.com",
                                "STUDENT");
                when(authService.register(any(RegisterRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("student@test.com"))
                                .andExpect(jsonPath("$.role").value("STUDENT"));

                verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        void testLoginUser() throws Exception {

                LoginRequest request = new LoginRequest();
                request.setEmail("student@test.com");
                request.setPassword("Password123!");

                CurrentUserResponse response = new CurrentUserResponse(true,
                                "student@test.com",
                                "STUDENT");

                when(authService.login(
                                any(LoginRequest.class),
                                any(HttpServletRequest.class),
                                any(HttpServletResponse.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("student@test.com"))
                                .andExpect(jsonPath("$.role").value("STUDENT"));

                verify(authService).login(
                                any(LoginRequest.class),
                                any(HttpServletRequest.class),
                                any(HttpServletResponse.class));
        }

        @Test
        void testRegisterUserWithDuplicateEmail() throws Exception {

                RegisterRequest request = new RegisterRequest();
                request.setEmail("existing@test.com");
                request.setPassword("Password123!");
                request.setFullName("Test Student");
                request.setProgram("Computer Science");
                request.setRole(UserRole.STUDENT);

                when(authService.register(any(RegisterRequest.class)))
                                .thenThrow(new EmailAlreadyExistsException(
                                                "Email already exists"));

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.title")
                                                .value("Email already exists"));
        }

        @Test
        void testLoginUserWithInvalidCredentials() throws Exception {

                LoginRequest request = new LoginRequest();
                request.setEmail("student@test.com");
                request.setPassword("wrongpassword");

                when(authService.login(
                                any(LoginRequest.class),
                                any(HttpServletRequest.class),
                                any(HttpServletResponse.class)))
                                .thenThrow(new InvalidCredentialsException(
                                                "Invalid email or password"));

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.title")
                                                .value("Invalid Credentials"));
        }

        /**
         * Tests that logging in without CSRF is forbidden.
         * 
         * @throws Exception
         */
        @Test
        void testLoginUserWithoutCsrfIsForbidden() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setEmail("student@test.com");
                request.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());

                verify(authService, never()).login(
                                any(LoginRequest.class),
                                any(HttpServletRequest.class),
                                any(HttpServletResponse.class));
        }

        /**
         * Tests that register without CSRF is forbidden
         */
        @Test
        void testRegisterUserWithoutCsrfIsForbidden() throws Exception {
                RegisterRequest request = new RegisterRequest();
                request.setEmail("student@test.com");
                request.setPassword("Password123!");
                request.setFullName("Test Student");
                request.setProgram("Computer Science");
                request.setRole(UserRole.STUDENT);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());

                verify(authService, never()).register(any(RegisterRequest.class));
        }
}