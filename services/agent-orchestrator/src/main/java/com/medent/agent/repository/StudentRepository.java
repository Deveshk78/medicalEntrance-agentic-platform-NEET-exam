package com.medent.agent.repository;

import com.medent.agent.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends MongoRepository<Student, String> {
    Optional<Student> findByCognitoSub(String cognitoSub);
    List<Student> findByStatus(Student.StudentStatus status);
    long countByStatus(Student.StudentStatus status);
}
