package edu.franklin.cecas.repository;

import java.util.*;
import edu.franklin.cecas.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByStudentId(Integer studentId);

    List<User> findByRole(UserRole role);

    List<User> findByProgram(String program);
}