package edu.franklin.cecas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import edu.franklin.cecas.service.CecasUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CecasUserDetailsService cecasUserDetailsService;

    SecurityConfig(CecasUserDetailsService cecasUserDetailsService) {
        this.cecasUserDetailsService = cecasUserDetailsService;
        }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();


        http
            .csrf(csrf -> csrf.disable()) // Temporarily disable CSRF until authentication is implemented
            .securityContext(sc -> sc.securityContextRepository(securityContextRepository))
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(sf -> sf.migrateSession()) // rotate session on auth
            )
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/api/hello", "/api/auth/me", "/api/users/**").permitAll()
                .anyRequest().authenticated()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies("CECASSESSION") // must match application.properties
                .addLogoutHandler(new HeaderWriterLogoutHandler(
                    new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES)
                ))
                .permitAll()
            )
            .httpBasic(Customizer.withDefaults()); // Use HTTP Basic authentication for simplicity in development

        return http.build();
    }
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        // rotates session id to mitigate fixation attacks
        return new ChangeSessionIdAuthenticationStrategy();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(cecasUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider; 
    }
}
