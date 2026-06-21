package edu.franklin.cecas.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import edu.franklin.cecas.config.SecurityConfig;
import edu.franklin.cecas.service.CecasUserDetailsService;
import edu.franklin.cecas.service.UserService;
import edu.franklin.cecas.dto.UserProfileResponse;
import edu.franklin.cecas.dto.ChangePasswordRequest;
import edu.franklin.cecas.dto.UserDTO;

@WebMvcTest(controllers = UserController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CecasUserDetailsService cecasUserDetailsService;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "chair@test.com", roles = { "CHAIR" })
    void testGetStudentByStudentId() throws Exception {
        when(userService.getStudentByStudentId(1001)).thenReturn(new UserDTO());
        mockMvc.perform(get("/api/users/1001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(userService).getStudentByStudentId(1001);
    }

    @Test
    @WithMockUser(username = "student@test.com", roles = { "STUDENT" })
    void testGetUserProfile() throws Exception {
        UserProfileResponse resp = new UserProfileResponse("student@test.com", "Student Name", "STUDENT");
        when(userService.getUserProfile(eq("student@test.com"))).thenReturn(resp);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("student@test.com"))
                .andExpect(jsonPath("$.fullName").value("Student Name"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        verify(userService).getUserProfile("student@test.com");
    }

    @Test
    @WithMockUser(username = "student@test.com", roles = { "STUDENT" })
    void testChangePassword() throws Exception {
        // at least 8 characters, otherwise it will throw a 500
        ChangePasswordRequest req = new ChangePasswordRequest("currentPass", "newPassword", "newPassword");

        doNothing().when(userService).changePassword(eq("student@test.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/users/change-password")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk()).andDo(print());
        verify(userService).changePassword(eq("student@test.com"), any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(username = "chair@test.com", roles = { "CHAIR" })
    void testForceChangePassword() throws Exception {
        // at least 8 characters, otherwise it will throw a 500
        ChangePasswordRequest req = new ChangePasswordRequest("tempPass", "newPassword", "newPassword");
        when(userService.isMustChangePassword(eq("chair@test.com"))).thenReturn(true);
        doNothing().when(userService).changePassword(eq("chair@test.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/users/force-change-password")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("mustChangePassword flag cleared")));

        verify(userService).isMustChangePassword("chair@test.com");
        verify(userService).changePassword(eq("chair@test.com"), any(ChangePasswordRequest.class));
    }

    /**
     * Tests that an anonymous users are blocked from /api/users/me
     * 
     * @throws Exception
     */
    @Test
    void testGetUserProfileRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me").with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Tests that the student is blocked from a chair only route.
     * 
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "student@test.com", roles = { "STUDENT" })
    void testStudentCannotGetStudentByStudentId() throws Exception {
        mockMvc.perform(get("/api/users/1001"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that student is blocked from chair password flow.
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "student@test.com", roles = { "STUDENT" })
    void testStudentCannotForceChangePassword() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest("tempPass", "newPassword", "newPassword");

        mockMvc.perform(post("/api/users/force-change-password")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that anonymous user is blocked from change password.
     * @throws Exception
     */
    @Test
    void testChangePasswordRequiresAuthentication() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest("currentPass", "newPassword", "newPassword");

        mockMvc.perform(post("/api/users/change-password")
                .with(anonymous())
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
