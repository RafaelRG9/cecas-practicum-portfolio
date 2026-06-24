package edu.franklin.cecas.repository;

import java.util.*;
import edu.franklin.cecas.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByStudentId(Integer studentId);

    List<User> findAllByRoleAndIsActiveTrue(UserRole role);

    List<User> findByProgram(String program);
}