package com.nuptsast.repository;

import com.nuptsast.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
public interface TeacherRepository extends JpaRepository<Teacher, String> {
  Optional<Teacher> findByEmployeeId(String employeeId);
}
