package edu.franklin.cecas.repository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.support.MySqlDataJpaTest;

@MySqlDataJpaTest
public class UserRepositoryTest {
    
        @Autowired
        private UserRepository userRepository;

        private User createTestUser() {
            User user = new User();

            user.setFullName("John Doe");
            user.setEmail("john@test.com");
            user.setPassword("123456");
            user.setRole(UserRole.STUDENT);
            user.setStudentId(12345);
            user.setProgram("Computer Science");
            user.setIsActive(true);
            user.setMustChangePassword(false);
            user.setEmailVerified(true);

            return user;
        }

        private User createSecondTestUser() {
            User user = new User();

            user.setFullName("Jane Smith");
            user.setEmail("jane@test.com");
            user.setPassword("abcdef");
            user.setRole(UserRole.STUDENT);
            user.setStudentId(54321);
            user.setProgram("Computer Science");
            user.setIsActive(true);
            user.setMustChangePassword(false);
            user.setEmailVerified(true);

            return user;
        }

    @Test
    public void testFindByEmailIgnoreCase() {
        User user = createTestUser();
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmailIgnoreCase("John@Test.com");

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

        assertThat(result).isPresent();
        assertThat(result.get().getStudentId()).isEqualTo(12345);
    }

    @Test
    public void testFindByRole() {
        User user = createTestUser();
        User secondUser = createSecondTestUser();

        userRepository.save(user);
        userRepository.save(secondUser);

        List<User> results = userRepository.findAllByRoleAndIsActiveTrue(UserRole.STUDENT);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(results.get(1).getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(results.size()).isEqualTo(2);
    }

    @Test
    public void testFindByProgram() {
        User user = createTestUser();
        User secondUser = createSecondTestUser();

        userRepository.save(user);
        userRepository.save(secondUser);

        List<User> results = userRepository.findByProgram("Computer Science");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getProgram()).isEqualTo("Computer Science");
        assertThat(results.get(1).getProgram()).isEqualTo("Computer Science");
        assertThat(results.size()).isEqualTo(2);
    }
    @Test
    public void testFindAllByRoleAndIsActiveTrue() {

        User user1 = createTestUser();
        User user2 = createSecondTestUser();

        User inactiveUser = createTestUser();
        inactiveUser.setEmail("inactive@test.com");
        inactiveUser.setIsActive(false);

        userRepository.saveAll(List.of(user1, user2, inactiveUser));

        List<User> results =
            userRepository.findAllByRoleAndIsActiveTrue(UserRole.STUDENT);


        assertThat(results).hasSize(2);
        
        assertThat(results)
            .allMatch(u -> u.getRole() == UserRole.STUDENT);

        assertThat(results)
            .allMatch(User::getIsActive);

        assertThat(results)
            .extracting(User::getEmail)
            .containsExactlyInAnyOrder("john@test.com", "jane@test.com");
    }
}
