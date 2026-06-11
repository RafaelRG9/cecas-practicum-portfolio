package edu.franklin.cecas.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;


import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.support.MySqlTestcontainers;

@DataJpaTest
@Import(MySqlTestcontainers.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    
        @Autowired
        private UserRepository userRepository;

        private User createTestUser() {
            User user = new User();

            user.setFullName("John Doe");
            user.setEmail("john@test.com");
            user.setPassword("password");
            user.setRole(UserRole.STUDENT);
            user.setStudentId(12345);
            user.setProgram("Computer Science");
            user.setIsActive(true);
            user.setMustChangePassword(false);
            user.setEmailVerified(true);

            return user;
        }

    @Test
    public void testFindByEmail() {
        User user = createTestUser();
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("john@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@test.com");
    }


}