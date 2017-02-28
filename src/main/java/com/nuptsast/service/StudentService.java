package com.nuptsast.service;

import com.nuptsast.domain.Course;
import com.nuptsast.domain.Student;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.InputStream;
import java.util.List;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
public interface StudentService extends UserDetailsService {
  Student addStudent(Student student);

  void updateStudent(Student student);

  void deleteStudent(String studentId);

  Student fetchByStudentId(String studentId);

  List<Student> fetchByName(String name);

  List<Student> fetchByClassId(String classId);

  List<Student> fetchByClassIdAndName(String name, String classId);

  void resetPassword(String studentId);

  void setPassword(String studentId, String originPassword, String newPassword);

  int uploadStudent(InputStream inputStream);

  List<Course> findCourse(String studentId);
}
