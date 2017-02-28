package com.nuptsast.repository;

import com.nuptsast.domain.Course;
import com.nuptsast.domain.Student;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
public interface CourseRepository extends JpaRepository<Course, Integer> {
  Optional<Course> findByNameAndSemester(String name, Integer semester);

  Optional<Course> findById(Integer id);

  List<Course> findBySemester(Integer semester);
  List<Course> findByTeacher_EmployeeId(String employeeId);

//  @Async
  @Query("select c from Course c where :student member of c.students and c.semester = :semester")
  List<Course> findByStudentIdAndSemester(@Param("student") Student student, @Param("semester") Integer semester);
}