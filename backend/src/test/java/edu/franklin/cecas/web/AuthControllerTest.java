package edu.franklin.cecas.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import edu.franklin.cecas.config.SecurityConfig;
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

    private RegisterRequest createValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("student@test.com");
        request.setPassword("Password123!");
        request.setFullName("Test Student");
        request.setProgram("Computer Science");
        request.setStudentId(1234);
        return request;
    }

    @Test
    void testRegisterUser() throws Exception {
        RegisterRequest request = createValidRegisterRequest();

        CurrentUserResponse response = new CurrentUserResponse(
                false,
                "student@test.com",
                "STUDENT",
                false);
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@test.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.mustChangePassword").value(false))
                .andExpect(jsonPath("$.authenticated").value(false));

        verify(authService).register(argThat(registerRequest -> registerRequest.getEmail().equals("student@test.com")
                && registerRequest.getFullName().equals("Test Student")
                && registerRequest.getProgram().equals("Computer Science")
                && registerRequest.getStudentId().equals(1234)));
    }

    @Test
    void testLoginUser() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("student@test.com");
        request.setPassword("Password123!");

        CurrentUserResponse response = new CurrentUserResponse(true,
                "student@test.com",
                "STUDENT",
                false);

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
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.mustChangePassword").value(false));

        verify(authService).login(
                any(LoginRequest.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class));
    }

    @Test
    void testGetCurrentUserReturnsAnonymousWhenUnauthenticated() throws Exception {
        CurrentUserResponse response = new CurrentUserResponse(
                false,
                null,
                null,
                false);

        when(authService.getCurrentUserResponse(isNull()))
                .thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.email").isEmpty())
                .andExpect(jsonPath("$.role").isEmpty())
                .andExpect(jsonPath("$.mustChangePassword").value(false));

        verify(authService).getCurrentUserResponse(isNull());
    }

    @Test
    void testGetCurrentUserReturnsDelegatedAuthenticatedPayload() throws Exception {
        CurrentUserResponse response = new CurrentUserResponse(
                true,
                "derek@test.com",
                "CHAIR",
                true);

        when(authService.getCurrentUserResponse(any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/auth/me")
                .with(user("derek@test.com").roles("CHAIR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.email").value("derek@test.com"))
                .andExpect(jsonPath("$.role").value("CHAIR"))
                .andExpect(jsonPath("$.mustChangePassword").value(true));

        verify(authService).getCurrentUserResponse(any(Authentication.class));
    }

    @Test
    void testRegisterUserWithDuplicateEmail() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setEmail("existing@test.com");
        request.setStudentId(2134);

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
        RegisterRequest request = createValidRegisterRequest();
        request.setStudentId(1432);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterWithoutStudentIdFails() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setStudentId(null);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.studentId").exists());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterWithNonPositiveStudentIdFails() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setStudentId(0);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.studentId", containsString("greater than 0")));

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterWithBlankProgramFails() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setProgram("   ");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.program").exists());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterWithLongProgramNameFails() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setProgram("A".repeat(46));

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.program").exists());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterWithShortEmailFails() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setEmail("this-isnt-an-email");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterUserWithShortPasswordFailsValidation() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setPassword("short");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.password").exists());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterUserWithBlankFullNameFailsValidation() throws Exception {
        RegisterRequest request = createValidRegisterRequest();
        request.setFullName("   ");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.fullName").exists());

        verify(authService, never()).register(any(RegisterRequest.class));
    }
}
