package edu.franklin.cecas.repository;

import java.util.Optional;
import java.util.List; //This needed to be added for List to be used - Rafael Ramirez-Gaston

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

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
    
    @Test
    public void testExistsByEmail() {
        User user = createTestUser();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("john@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    public void testFindByStudentId() {
        User user = createTestUser();
        userRepository.save(user);

        Optional<User> result = userRepository.findByStudentId(12345);

        assertThat(result.get().getStudentId()).isEqualTo(12345);
    }

    @Test
    public void testFindByRole() {
        User user = createTestUser();
        userRepository.save(user);

        List<User> results = userRepository.findByRole(UserRole.STUDENT);

        assertThat(results).isNotEmpty();

        assertThat(results.get(0).getRole()).isEqualTo(UserRole.STUDENT);
    }

    @Test
    public void testFindByProgram() {
        User user = createTestUser();
        userRepository.save(user);

        List<User> results = userRepository.findByProgram("Computer Science");

        assertThat(results).isNotEmpty();

        assertThat(results.get(0).getRole()).isEqualTo("Computer Science");
    }

}