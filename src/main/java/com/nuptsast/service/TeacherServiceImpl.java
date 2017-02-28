package com.nuptsast.service;

import com.nuptsast.domain.Teacher;
import com.nuptsast.domain.TeacherDetails;
import com.nuptsast.exception.ConflictException;
import com.nuptsast.exception.InternalException;
import com.nuptsast.exception.NotFoundException;
import com.nuptsast.repository.TeacherRepository;
import com.nuptsast.util.Utilities;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
@Service
public class TeacherServiceImpl implements TeacherService {
  private final TeacherRepository teacherRepository;
  private final PasswordEncoder encoder;

  @Autowired
  public TeacherServiceImpl(TeacherRepository teacherRepository, PasswordEncoder encoder) {
    this.teacherRepository = teacherRepository;
    this.encoder = encoder;
  }

  @Override
  @Secured("ROLE_ADMIN")
  public Teacher addTeacher(Teacher teacher) {
    teacherRepository.findByEmployeeId(teacher.getEmployeeId())
                     .ifPresent(v -> {
                       throw new ConflictException();
                     });
    teacher.setPassword(encoder.encode(String.valueOf(teacher.getEmployeeId())));
    return teacherRepository.save(teacher);
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void updateTeacher(Teacher teacher) {
    teacherRepository.findByEmployeeId(teacher.getEmployeeId())
                     .ifPresent(tea -> Utilities.copyProperties(teacher, tea));
  }

  @Override
  @Secured("ROLE_ADMIN")
  public void deleteTeacher(String employeeId) {
    teacherRepository.delete(employeeId);
  }

  @Override
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public Teacher fetchByEmployeeId(String employeeId) {
    return teacherRepository.findByEmployeeId(employeeId)
                            .orElseThrow(NotFoundException::new);
  }

  @Override
  @Secured("ROLE_ADMIN")
  public List<Teacher> allTeachers() {
    return teacherRepository.findAll();
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void resetPassword(String teacherId) {
    Teacher teacher = Utilities.fetch(() -> teacherRepository.findByEmployeeId(teacherId));
    teacher.setPassword(encoder.encode(teacherId));
  }

  @Override
  @Transactional
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public void setPassword(String employeeId, String originPassword, String newPassword) {
    Teacher teacher = Utilities.fetch(() -> teacherRepository.findByEmployeeId(employeeId));
    if (encoder.matches(originPassword, teacher.getPassword())) {
      teacher.setPassword(encoder.encode(newPassword));
    } else {
      throw new IllegalArgumentException("Wrong password");
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Teacher teacher = teacherRepository.findByEmployeeId(username)
                                       .orElseThrow(() -> new UsernameNotFoundException("username not found: " + username));
    return new TeacherDetails(teacher);
  }

  @Override
  @Transactional
  @PreAuthorize("hasAnyRole('ADMIN')")
  public void uploadTeacher(InputStream inputStream) {
    try {
      List<Teacher> teachers = Utilities.readExcel(inputStream,
                                                   row -> new Teacher(row.get(0),
                                                                      row.get(1),
                                                                      Integer.parseInt(row.get(2))));
      for (int i = 0; i < teachers.size(); i++) {
        try {
          addTeacher(teachers.get(i));
        } catch (DataIntegrityViolationException e) {
          throw new IllegalArgumentException("Exception happened on line: " + (i + 1));
        }
      }
    } catch (IOException e) {
      throw new InternalException();
    } catch (InvalidFormatException e) {
      throw new IllegalArgumentException();
    }
  }
}
