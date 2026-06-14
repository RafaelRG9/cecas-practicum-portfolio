package edu.franklin.cecas.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.franklin.cecas.support.MySqlServiceTest;
import edu.franklin.cecas.repository.UserRepository;

@MySqlServiceTest
public class AuthServiceTest {

    // @Autowired
    // private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private PasswordEncoder passwordEncoder; - save for later

    @Test
    public void registerPersistsEncodedPasswordAndDefaultFlags() {
        // arrange

        // act

        // assert user saved
        // assert email correct
        // assert password matches - password encoding will be separate ticket
        // assert flags initialized correctly
    }
}