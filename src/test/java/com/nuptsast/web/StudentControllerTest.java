package com.nuptsast.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuptsast.domain.Student;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@WithMockUser(roles = "ADMIN")
public class StudentControllerTest {
  @Autowired
  private WebApplicationContext context;

  private Student students[] = new Student[]{
      new Student("B14112133", "郑致远", "B140407", false),
      new Student("B14040703", "张三", "B140407", false),
      new Student("B14040624", "刘强胜", "B140406", false)
  };

  private MockMvc mockMvc;

  @Before
  public void setUp() throws Exception {
    mockMvc = webAppContextSetup(context).build();
    Arrays.stream(students)
          .forEach(v -> {
            try {
              mockMvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON)
                                              .content(asJsonString(v)));
              System.out.println(asJsonString(v));
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
  }

  @Test
  public void testAddStudentConflict() throws Exception {
    mockMvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(students[0])))
           .andExpect(status().isConflict());
  }

  @Test
  public void addStudent() throws Exception {
    Student student = new Student("B14112132", "邵应虎", "B141121", false);
    mockMvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(student)))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "http://localhost/student/" + student.getStudentId()));
  }

  @Test
  public void getStudent() throws Exception {
    mockMvc.perform(get("/student/B14112133"))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(students[0])));
  }

  @Test
  public void getStudentByClassId() throws Exception {
    mockMvc.perform(get("/student").param("classId", "B140407"))
           .andExpect(content().json(asJsonString(Arrays.asList(students[0], students[1]))));
  }

  @Test
  public void getStudentByClassId_NullList() throws Exception {
    mockMvc.perform(get("/student").param("classId", "B140404"))
           .andExpect(content().json(asJsonString(Lists.emptyList())));
  }

  @Test
  public void updateStudent() throws Exception {
    Student student = new Student("B14112133", "郑致远", "B140407", false);
    mockMvc.perform(put("/student/B14112133").contentType(MediaType.APPLICATION_JSON)
                                             .content(asJsonString(student)))
           .andExpect(status().isNoContent());
    mockMvc.perform(get("/student/B14112133"))
           .andExpect(content().json(asJsonString(student)));
  }

  @Test
  public void deleteStudent() throws Exception {
    mockMvc.perform(delete("/student/B14112133"))
           .andExpect(status().isNoContent());
    mockMvc.perform(get("/student/B14112133"))
           .andExpect(status().isNotFound());
  }


  public static String asJsonString(final Object obj) {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Student asJsonObject(final String obj) {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(obj, Student.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}