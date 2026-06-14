package edu.franklin.cecas.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.franklin.cecas.support.MySqlServiceTest;
import edu.franklin.cecas.repository.UserRepository;

@MySqlServiceTest
public class MySqlServiceTestSmokeTest {
    
    @Autowired
    private UserRepository userRepository;

    @Test
    public void contextLoadsAndRepositoryAvailable() {
        assertThat(userRepository).isNotNull();
    }
}
