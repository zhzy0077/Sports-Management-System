package com.nuptsast.service;

import com.nuptsast.domain.Course;
import com.nuptsast.domain.Student;
import com.nuptsast.domain.StudentDetails;
import com.nuptsast.exception.InternalException;
import com.nuptsast.repository.StudentRepository;
import com.nuptsast.util.Utilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
@Service
public class StudentServiceImpl implements StudentService {
  private final StudentRepository studentRepository;
  private final PasswordEncoder encoder;
  private final Log logger = LogFactory.getLog(this.getClass());

  @Autowired
  public StudentServiceImpl(StudentRepository studentRepository, PasswordEncoder encoder) {
    this.studentRepository = studentRepository;
    this.encoder = encoder;
  }

  @Override
  @Secured("ROLE_ADMIN")
  public Student addStudent(Student student) {
//    studentRepository.findByStudentId(student.getStudentId())
//                     .ifPresent(t -> {
//                       throw new ConflictException();
//                     });
    student.setPassword("0");
    CompletableFuture.runAsync(() -> {
//        logger.info("start");
      student.setPassword(encoder.encode(student.getStudentId()));
//        logger.info("finish");
      studentRepository.save(student);
    });
    return studentRepository.save(student);
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void updateStudent(Student student) {
    studentRepository.findByStudentId(student.getStudentId())
                     .ifPresent(stu -> Utilities.copyProperties(student, stu));
  }

  @Override
  @Secured("ROLE_ADMIN")
  public void deleteStudent(String studentId) {
    studentRepository.delete(studentId);
  }

  @Override
  @PreAuthorize("hasAnyRole('ADMIN', 'ALL', #studentId)")
  public Student fetchByStudentId(String studentId) {
    return Utilities.fetch(() -> studentRepository.findByStudentId(studentId));
  }

  @Override
  @Secured({ "ROLE_ALL", "ROLE_ADMIN" })
  public List<Student> fetchByClassId(String classId) {
    return studentRepository.findByClassId(classId);
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void resetPassword(String studentId) {
    Student student = Utilities.fetch(() -> studentRepository.findByStudentId(studentId));
    student.setPassword(encoder.encode(studentId));
  }

  @Override
  @Transactional
  @PreAuthorize("hasAnyRole('ALL', 'ADMIN', #studentId)")
  public void setPassword(String studentId, String originPassword, String newPassword) {
    Student student = Utilities.fetch(() -> studentRepository.findByStudentId(studentId));
    if (encoder.matches(originPassword, student.getPassword())) {
      student.setPassword(encoder.encode(newPassword));
    } else {
      throw new IllegalArgumentException("Wrong password");
    }
  }

  @Override
  public List<Student> fetchByName(String name) {
    return studentRepository.findByNameContaining(name);
  }

  @Override
  public List<Student> fetchByClassIdAndName(String name, String classId) {
    return studentRepository.findByNameAndClassId(name, classId);
  }

  @Override
  @Transactional
  public int uploadStudent(InputStream inputStream) {
    try {
      List<Student> students = Utilities.readExcel(inputStream,
                                                   row -> new Student(row.get(0),
                                                                      row.get(1),
                                                                      row.get(2),
                                                                      Objects.equals(row.get(3), "女")));
      for (int i = 0; i < students.size(); i++) {
        try {
//          Student student = students.get(i);
//          student.setPassword("0");
//          CompletableFuture.supplyAsync(() -> {
//            student.setPassword(encoder.encode(student.getStudentId()));
//            return studentRepository.save(student);
//          });
//          addStudent(students.get(i));
//          student.setPassword(encoder.encode(student.getStudentId()));
        } catch (DataIntegrityViolationException e) {
          throw new IllegalArgumentException("Exception happened on line: " + (i + 1));
        }
      }
      students.parallelStream()
          .forEach(student -> {
            student.setPassword(encoder.encode(student.getStudentId()));
          });
      studentRepository.save(students);
      return students.size();
    } catch (IOException e) {
      throw new InternalException();
    } catch (InvalidFormatException e) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public List<Course> findCourse(String studentId) {
    return new ArrayList<>(Utilities.fetch(() -> studentRepository.findByStudentId(studentId)).getCourses());
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Student student = studentRepository.findByStudentId(username)
                                       .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));
    return new StudentDetails(student);
  }
}
