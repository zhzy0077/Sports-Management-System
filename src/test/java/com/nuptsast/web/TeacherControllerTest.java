package com.nuptsast.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuptsast.domain.Teacher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@WithMockUser(roles = "ADMIN")
public class TeacherControllerTest {
  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  private Teacher[] teachers = new Teacher[]{
      new Teacher("19870006", "张三", 0),
      new Teacher("20140012", "李四", 1),
      new Teacher("20121234", "王五", 2),
  };

  @Before
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    Arrays.stream(teachers)
          .forEach(v -> {
            try {
              mockMvc.perform(post("/teacher")
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .content(asJsonString(v)));
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
  }

  @Test
  public void addTeacher() throws Exception {
    Teacher teacher = new Teacher("19880001", "赵六", 0);
    mockMvc.perform(post("/teacher").contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(teacher)))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "http://localhost/teacher/19880001"));
  }

  @Test
  public void addTeacher_Conflict() throws Exception {
    Teacher teacher = new Teacher("19870006", "赵六", 0);
    mockMvc.perform(post("/teacher").contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(teacher)))
           .andExpect(status().isConflict());
  }

  @Test
  public void findTeacher() throws Exception {
    Arrays.stream(teachers)
          .forEach(v -> {
            try {
              mockMvc.perform(get("/teacher/" + String.valueOf(v.getEmployeeId())))
                     .andExpect(status().isOk())
                     .andExpect(content().json(asJsonString(v)));
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
  }

  @Test
  public void findTeacher_NotFound() throws Exception {
    mockMvc.perform(get("/teacher/" + "19880001"))
           .andExpect(status().isNotFound());
  }

  @Test
  public void updateTeacher() throws Exception {
    Teacher teacher = teachers[0];
    teacher.setAuthority(1);
    mockMvc.perform(put("/teacher/" + String.valueOf(teacher.getEmployeeId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(teacher)))
           .andExpect(status().isNoContent());
    mockMvc.perform(get("/teacher/" + String.valueOf(teacher.getEmployeeId())))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(teacher)));
  }

  @Test
  public void deleteTeacher() throws Exception {
    Arrays.stream(teachers)
          .forEach(v -> {
            try {
              System.out.println(v);
              mockMvc.perform(get("/teacher/" + String.valueOf(v.getEmployeeId())))
                     .andExpect(status().isOk())
                     .andExpect(content().json(asJsonString(v)));
              mockMvc.perform(delete("/teacher/" + String.valueOf(v.getEmployeeId())))
                     .andExpect(status().isNoContent());
              mockMvc.perform(get("/teacher/" + String.valueOf(v.getEmployeeId())))
                     .andExpect(status().isNotFound());
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
  }

  @Test
  public void allTeachers() throws Exception {
    mockMvc.perform(get("/teacher"))
           .andExpect(content().json(asJsonString(Arrays.asList(teachers))));
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