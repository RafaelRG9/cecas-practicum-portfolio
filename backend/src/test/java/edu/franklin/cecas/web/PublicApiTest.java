package edu.franklin.cecas.web;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.franklin.cecas.config.SecurityConfig;
import edu.franklin.cecas.service.AuthService;
import edu.franklin.cecas.service.CecasUserDetailsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {HelloController.class, AuthController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class PublicApiTest {
    
    @MockitoBean
    private CecasUserDetailsService cecasUserDetailsService;

    @MockitoBean
    private AuthService authService;
    
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHelloIsPublic() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Backend is running"));
    }
}
