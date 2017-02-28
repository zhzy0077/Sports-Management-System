package com.nuptsast.web;

import com.nuptsast.domain.Course;
import com.nuptsast.domain.Student;
import com.nuptsast.exception.InternalException;
import com.nuptsast.service.ScoreService;
import com.nuptsast.service.StudentService;
import com.nuptsast.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
@RestController
public class StudentController {
  private final StudentService studentService;
  private final ScoreService scoreService;

  @Autowired
  public StudentController(StudentService studentService, ScoreService scoreService) {
    this.studentService = studentService;
    this.scoreService = scoreService;
  }

  @RequestMapping(value = "/student", method = RequestMethod.POST)
  public ResponseEntity<?> addStudent(@RequestBody Student student, UriComponentsBuilder uriBuilder) {
    studentService.addStudent(student);
    return ResponseEntity.created(uriBuilder.path("/student/{id}")
                                            .buildAndExpand(student.getStudentId())
                                            .toUri())
                         .build();
  }

  @RequestMapping(value = "/student", method = RequestMethod.GET)
  public List<Student> getStudentByClassId(@RequestParam(required = false) String classId,
                                           @RequestParam(required = false) String name) {
    if (classId == null) {
      return studentService.fetchByName(name);
    } else if (name == null) {
      return studentService.fetchByClassId(classId);
    } else {
      return studentService.fetchByClassIdAndName(name, classId);
    }
  }

  @RequestMapping(value = "/student/{studentId}/password", method = RequestMethod.PUT)
  public ResponseEntity<?> setPassword(@PathVariable String studentId,
                                       @RequestParam String originPassword,
                                       @RequestParam String newPassword) {
    studentService.setPassword(studentId, originPassword, newPassword);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/student/{studentId}/reset", method = RequestMethod.PUT)
  public ResponseEntity<?> resetPassword(@PathVariable String studentId) {
    studentService.resetPassword(studentId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/student/{studentId}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateStudent(@PathVariable String studentId,
                                         @RequestBody Student student) {
    if (!student.getStudentId().equals(studentId)) {
      return ResponseEntity.badRequest().build();
    }
    studentService.updateStudent(student);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/student/{studentId}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteStudent(@PathVariable String studentId) {
    studentService.deleteStudent(studentId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/student/{id}", method = RequestMethod.GET)
  public Student getStudent(@PathVariable String id) {
    return studentService.fetchByStudentId(id);
  }

  @RequestMapping(value = "/student/{id}/score/{semester}", method = RequestMethod.GET)
  public String getScore(@PathVariable String id,
                         @PathVariable Integer semester) {
    return Utilities.generateJson(scoreService.getScoreBySemester(id, semester));
  }

  @RequestMapping(value = "/student/upload", method = RequestMethod.POST)
  public ResponseEntity<?> uploadStudent(@RequestParam("file") MultipartFile file) {
    try (InputStream stream = file.getInputStream()) {
      studentService.uploadStudent(stream);
    } catch (IOException e) {
      e.printStackTrace();
      throw new InternalException();
    }
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/student/{studentId}/course", method = RequestMethod.GET)
  public List<Course> findCourse(@PathVariable String studentId) {
    return studentService.findCourse(studentId);
  }

}
