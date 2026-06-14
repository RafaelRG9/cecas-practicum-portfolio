package edu.franklin.cecas.web;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.franklin.cecas.config.SecurityConfig;
import edu.franklin.cecas.service.CecasUserDetailsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {HelloController.class, AuthController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class PublicApiTest {
    
    @MockitoBean
    private CecasUserDetailsService cecasUserDetailsService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHelloIsPublic() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Backend is running"));
    }

    @Test
    public void testCurrentUserReturnsAnonymousResponseWhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.email").isEmpty())
                .andExpect(jsonPath("$.role").isEmpty());
    }

    @Test
    @WithMockUser(username = "derek@franklin.edu", roles = {"STUDENT"})
    public void testCurrentUserReturnsAuthenticatedResponseWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.email").value("derek@franklin.edu"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }
}
