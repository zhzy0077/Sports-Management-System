package com.nuptsast.service;

import com.nuptsast.domain.Course;
import com.nuptsast.domain.Student;
import com.nuptsast.domain.Teacher;
import com.nuptsast.domain.Weight;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
public interface CourseService {
  Course addCourse(Course course);

  List<Course> fetch(String name, Integer semester);

  List<Course> fetchByTeacher(String name, Integer semester, String employeeId);

  Course fetch(Integer id);

  void update(Course course);

  void delete(Integer courseId);

  Teacher fetchTeacher(Integer courseId);

  void setTeacher(Integer courseId, String employeeId);

  void addStudent(Integer courseId, String studentId);

  Collection<Student> getStudent(Integer courseId);

  int uploadStudent(InputStream inputStream);

  void deleteStudent(Integer courseId, String studentId);

  void addSubject(Integer courseId, Integer subjectId, Integer weight, Integer standardId);

  Collection<Weight> getSubject(Integer courseId);

  Weight getSubject(Integer courseId, Integer weightId);

  void updateWeight(Integer courseId, Integer weightId, Integer newWeight);

  void updateStandard(Integer courseId, Integer weightId, Integer standardId);

  void deleteSubject(Integer courseId, Integer weightId);

  void writeTemplate(Integer courseId, OutputStream outputStream);

  void uploadCourse(InputStream inputStream);

  void exportExcelByCourse(Integer courseId, OutputStream stream);

  List<Map<String, Object>> generateCourseInformation(Integer courseId);

  void exportExcelBySemester(Integer semester, OutputStream outputStream);

}
