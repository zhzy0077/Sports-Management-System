package com.nuptsast.web;

import com.nuptsast.domain.Teacher;
import com.nuptsast.exception.InternalException;
import com.nuptsast.service.TeacherService;
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
public class TeacherController {
  private final TeacherService teacherService;

  @Autowired
  public TeacherController(TeacherService teacherService) {
    this.teacherService = teacherService;
  }

  @RequestMapping(value = "/teacher", method = RequestMethod.POST)
  public ResponseEntity<?> addTeacher(@RequestBody Teacher teacher, UriComponentsBuilder uriBuilder) {
    teacherService.addTeacher(teacher);
    return ResponseEntity.created(uriBuilder.path("/teacher/{id}")
                                            .buildAndExpand(teacher.getEmployeeId())
                                            .toUri())
                         .build();
  }

  @RequestMapping(value = "/teacher/{employeeId}", method = RequestMethod.GET)
  public Teacher findTeacher(@PathVariable String employeeId) {
    return teacherService.fetchByEmployeeId(employeeId);
  }

  @RequestMapping(value = "/teacher/{employeeId}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateTeacher(@PathVariable String employeeId, @RequestBody Teacher teacher) {
    if (!teacher.getEmployeeId().equals(employeeId)) {
      return ResponseEntity.badRequest().build();
    }
    teacherService.updateTeacher(teacher);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/teacher/{employeeId}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteTeacher(@PathVariable String employeeId) {
    teacherService.deleteTeacher(employeeId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/teacher", method = RequestMethod.GET)
  public List<Teacher> allTeachers() {
    return teacherService.allTeachers();
  }


  @RequestMapping(value = "/teacher/{employeeId}/password", method = RequestMethod.PUT)
  public ResponseEntity<?> setPassword(@PathVariable String employeeId,
                                       @RequestParam String originPassword,
                                       @RequestParam String newPassword) {
    teacherService.setPassword(employeeId, originPassword, newPassword);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/teacher/{employeeId}/reset", method = RequestMethod.PUT)
  public ResponseEntity<?> resetPassword(@PathVariable String employeeId) {
    teacherService.resetPassword(employeeId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/teacher/upload", method = RequestMethod.POST)
  public ResponseEntity<?> uploadTeacher(@RequestParam("file") MultipartFile file) {
    try (InputStream stream = file.getInputStream()) {
      teacherService.uploadTeacher(stream);
    } catch (IOException e) {
      e.printStackTrace();
      throw new InternalException();
    }
    return ResponseEntity.noContent().build();
  }
}
