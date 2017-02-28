package com.nuptsast.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuptsast.domain.Grading;
import com.nuptsast.domain.Standard;
import com.nuptsast.domain.Subject;
import com.nuptsast.repository.StandardRepository;
import com.nuptsast.repository.SubjectRepository;
import org.junit.Assert;
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

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by zheng on 2016/11/19.
 * For fit-jpa.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithMockUser(roles = "ADMIN")
public class SubjectControllerTest {
  @Autowired
  private WebApplicationContext context;


  private MockMvc mockMvc;

  @Autowired
  private SubjectRepository subjectRepository;
  private List<Subject> subjects = Arrays.asList(
      new Subject("50米", 2, 2000, 0),
      new Subject("引体向上", 0, 50, 0),
      new Subject("2000米", 2, 2000, 0)
  );

  @Autowired
  private StandardRepository standardRepository;

  @Before
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    subjects.forEach(subject -> {
      try {
        mockMvc.perform(post("/subject").contentType(MediaType.APPLICATION_JSON)
                                        .content(asJsonString(subject)))
               .andExpect(status().isCreated());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    Assert.assertEquals(subjects, subjectRepository.findAll());
    subjects = subjectRepository.findAll();
  }

  @Test
  public void addSubject() throws Exception {
    Subject subject = new Subject("立定跳远", 2, 400, 0);
    mockMvc.perform(post("/subject").contentType(MediaType.APPLICATION_JSON).content(asJsonString(subject)))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "http://localhost/subject/" + subjects.stream()
                                                                                        .mapToInt(Subject::getId)
                                                                                        .map(v -> v + 1).max().getAsInt()));
  }

  @Test
  public void allSubject() throws Exception {
    mockMvc.perform(get("/subject"))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(subjects)));
  }

  @Test
  public void getSubject() throws Exception {
    subjects.forEach(subject -> {
      try {
        mockMvc.perform(get("/subject/{id}", subject.getId()))
               .andExpect(status().isOk())
               .andExpect(content().json(asJsonString(subject)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void updateSubject() throws Exception {
    subjects.forEach(subject -> {
      try {
        mockMvc.perform(get("/subject/{id}", subject.getId()))
               .andExpect(status().isOk())
               .andExpect(content().json(asJsonString(subject)));
        subject.setMax(3);
        mockMvc.perform(put("/subject/{id}", subject.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(subject)))
               .andExpect(status().isNoContent());
        mockMvc.perform(get("/subject/{id}", subject.getId()))
               .andExpect(status().isOk())
               .andExpect(content().json(asJsonString(subject)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void deleteSubject() throws Exception {
    subjects.forEach(subject -> {
      try {
        mockMvc.perform(get("/subject/{id}", subject.getId()))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().json(asJsonString(subject)));
        mockMvc.perform(delete("/subject/{id}", subject.getId()))
               .andExpect(status().isNoContent());
        mockMvc.perform(get("/subject/{id}", subject.getId()))
               .andExpect(status().isNotFound());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    mockMvc.perform(get("/subject"))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(Collections.emptyList())));
  }


  @Test
  public void testGrading() throws Exception {
    Subject subject = subjects.get(0);
    List<Grading> gradings = Arrays.asList(
        new Grading(0, 1, 1),
        new Grading(2, 3, 2),
        new Grading(4, 5, 3)
    );
    Standard standard = new Standard(gradings, "标准");
    mockMvc.perform(get("/subject/{id}/standard", subject.getId()))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(Collections.emptyList())));
    System.out.println(asJsonString(gradings));
    mockMvc.perform(post("/subject/{id}/standard", subject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(standard)))
           .andDo(print())
           .andExpect(status().isNoContent());
    Collection<Standard> standards = standardRepository.findAll();
    mockMvc.perform(get("/subject/{id}/standard", subject.getId()))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(standards)));
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