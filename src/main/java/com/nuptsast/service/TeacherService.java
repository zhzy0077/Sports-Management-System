package com.nuptsast.service;

import com.nuptsast.domain.Teacher;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.InputStream;
import java.util.List;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
public interface TeacherService extends UserDetailsService {
  Teacher addTeacher(Teacher teacher);

  void updateTeacher(Teacher teacher);

  void deleteTeacher(String employeeId);

  Teacher fetchByEmployeeId(String employeeId);

  List<Teacher> allTeachers();

  void resetPassword(String teacherId);

  void setPassword(String studentId, String originPassword, String newPassword);

  void uploadTeacher(InputStream inputStream);
}
