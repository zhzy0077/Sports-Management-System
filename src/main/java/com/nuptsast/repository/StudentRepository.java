package com.nuptsast.repository;

import com.nuptsast.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by zheng on 2016/11/16.
 * For fit-jpa.
 */
public interface StudentRepository extends JpaRepository<Student, String> {
  Optional<Student> findByStudentId(String studentId);

  @Async
  @Query("select s from Student s where s.studentId = :studentId")
  Future<Student> findByStudentIdAsyc(@Param("studentId") String studentId);

  List<Student> findByClassId(String classId);

  List<Student> findByNameContaining(String name);

  List<Student> findByNameAndClassId(String name, String classId);
}
