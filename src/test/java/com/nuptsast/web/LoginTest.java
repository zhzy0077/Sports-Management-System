package com.nuptsast.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuptsast.domain.Student;
import com.nuptsast.repository.StudentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;
import java.util.Arrays;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by zheng on 2016/11/21.
 * For fit-jpa.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithMockUser(roles = "ADMIN")
public class LoginTest {
  @Autowired
  WebApplicationContext context;
  private MockMvc mockMvc;
  private Student students[] = new Student[]{
      new Student("B14112133", "郑致远", "B140407", false),
      new Student("B14040703", "张三", "B140407", false),
      new Student("B14040624", "刘强胜", "B140406", false)
  };
  @Autowired
  private StudentRepository studentRepository;
  @Autowired
  private PasswordEncoder encoder;


  @Before
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .apply(springSecurity())
                             .build();
    Arrays.stream(students)
          .forEach(v -> {
            v.setPassword(encoder.encode(v.getStudentId()));
            studentRepository.save(v);
            v.setPassword(null);
          });
  }

  @Test
  @WithMockUser(roles = { "ADMIN", "ALL" })
  public void getStudent() throws Exception {
    Arrays.stream(students)
          .forEach(student -> {
                     try {
                       mockMvc.perform(get("/student/{studentId}", student.getStudentId()))
                              .andExpect(status().isOk())
                              .andExpect(content().json(asJsonString(student)));
                     } catch (Exception e) {
                       e.printStackTrace();
                     }
                   }
          );
  }

  @Test
  @WithMockUser(roles = "ALL")
  public void getStudent_ALL() throws Exception {
    Arrays.stream(students)
          .forEach(student -> {
                     try {
                       mockMvc.perform(get("/student/{studentId}", student.getStudentId()))
                              .andExpect(status().isOk())
                              .andExpect(content().json(asJsonString(student)));
                     } catch (Exception e) {
                       e.printStackTrace();
                     }
                   }
          );
  }

  @Test
  @WithMockUser(roles = { "ONLY", "B14112133" })
  public void getStudent_ONLY() throws Exception {
    Student student = students[0];
    mockMvc.perform(get("/student/{studentId}", student.getStudentId()))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(student)));
  }

  @Test
  @WithMockUser(roles = { "ONLY", "B14112132" })
  public void getStudent_ONLY2() throws Exception {
    Student student = students[0];
    mockMvc.perform(get("/student/{studentId}", student.getStudentId()))
           .andExpect(status().isForbidden());
  }

  @Test
  @WithAnonymousUser
  public void testLogin() throws Exception {
    Student student = students[0];
    mockMvc.perform(get("/student/{studentId}", student.getStudentId()))
           .andExpect(status().isUnauthorized());
    mockMvc.perform(post("/login").param("username", "B14112133").param("password", "B14112133"))
           .andExpect(status().isOk())
           .andDo(print());
  }

  @Test
  @WithAnonymousUser
  public void testLogin2() throws Exception {
    mockMvc.perform(post("/login").param("username", "B14040703").param("password", "B1404070"))
           .andExpect(status().isUnauthorized())
           .andDo(print());
  }

  public static String asJsonString(final Object obj) {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
