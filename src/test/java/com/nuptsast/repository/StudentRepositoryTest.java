package com.nuptsast.repository;

import com.nuptsast.domain.Student;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;

/**
 * Created by zheng on 2016/11/16.
 * For fit-jpa.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StudentRepositoryTest {

  @Autowired
  private StudentRepository studentRepository;

  private Student student = new Student("B14112133", "郑致远", "B140407", false);

  @Before
  public void setUp() {
    student.setPassword("123456");
    student = studentRepository.save(student);
  }

  @Test
  @Transactional
  public void testStudent() throws Exception {
//    student = studentRepository.save(student);
    Student fetched = studentRepository.findByStudentId("B14112133")
                                       .get();
    assertEquals(fetched, student);
  }
}