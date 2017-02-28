package com.nuptsast.service;

import com.nuptsast.domain.Student;
import com.nuptsast.exception.ConflictException;
import com.nuptsast.exception.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@WithMockUser(roles = "ADMIN")
public class StudentServiceTest {
  @Autowired
  private StudentService studentService;

  private Student[] students = new Student[]{
      new Student("B14112133", "郑致远", "B140407", false),
      new Student("B14040703", "张三", "B140407", false),
      new Student("B14040624", "刘强胜", "B140406", false)
  };

  @Before
  public void setUp() throws Exception {
    for (Student student : students) {
      studentService.addStudent(student);
    }
  }

  @Test
  @Transactional
  public void addStudent() throws Exception {
    Student student = new Student("B14040625", "刘强", "B140406", false);
    student = studentService.addStudent(student);
    assertEquals(student,
                 studentService.fetchByStudentId("B14040625"));
  }

  @Test(expected = ConflictException.class)
  @Transactional
  public void addStudentConflict() throws Exception {
    studentService.addStudent(new Student("B14040624", "刘强", "B140406", false));
    assertEquals(new Student("B14040624", "刘强", "B140406", false),
                 studentService.fetchByStudentId("B14040625"));
  }

  @Test
  @Transactional
  public void updateStudent() throws Exception {
    studentService.updateStudent(new Student("B14040624", "刘胜", "B140406", false));
    assertEquals(new Student("B14040624", "刘胜", "B140406", false),
                 studentService.fetchByStudentId("B14040624"));
  }

  @Test(expected = NotFoundException.class)
  @Transactional
  public void deleteStudent() throws Exception {
    studentService.deleteStudent("B14112133");
    assertNull(studentService.fetchByStudentId("B14112133"));
  }

  @Test
  @Transactional
  public void fetchByStudentId() throws Exception {
    assertEquals(students[0],
                 studentService.fetchByStudentId("B14112133"));
  }

  @Test
  @Transactional
  public void fetchByClassId() throws Exception {
    assertEquals(Arrays.asList(Arrays.copyOfRange(students, 0, 2)),
                 studentService.fetchByClassId("B140407"));
  }

}