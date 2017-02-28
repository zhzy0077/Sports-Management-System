package com.nuptsast.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuptsast.domain.*;
import com.nuptsast.repository.CourseRepository;
import com.nuptsast.repository.SubjectRepository;
import com.nuptsast.repository.WeightRepository;
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
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by zheng on 2016/11/18.
 * For fit-jpa.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithMockUser(roles = "ADMIN")
public class CourseControllerTest {
  @Autowired
  WebApplicationContext webApplicationContext;
  @Autowired
  private CourseRepository courseRepository;
  @Autowired
  private SubjectRepository subjectRepository;

  private MockMvc mockMvc;

  private static boolean init = false;
  private List<Teacher> teachers = Arrays.asList(
      new Teacher("19870006", "张三", 0),
      new Teacher("20140012", "李四", 1),
      new Teacher("20121234", "王五", 2)
  );
  private List<Student> students = Arrays.asList(
      new Student("B14112133", "郑致远", "B140407", false),
      new Student("B14040703", "张三", "B140407", false),
      new Student("B14040624", "刘强胜", "B140406", false)
  );
  private List<Course> courses = Arrays.asList(
      new Course("男生篮球周二8-9节", 201601),
      new Course("男生篮球周二3-4节", 201601),
      new Course("男生网球周二3-4节", 201601)
  );
  private List<Subject> subjects = Arrays.asList(
      new Subject("50米", 2, 2000, 0),
      new Subject("引体向上", 0, 50, 0),
      new Subject("2000米", 2, 2000, 0)
  );
  @Autowired
  private WeightRepository weightRepository;

  @Before
  public void setUp() throws Exception {
    System.out.println("Set up");
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    teachers.forEach(v -> {
      try {
        mockMvc.perform(post("/teacher").contentType(MediaType.APPLICATION_JSON)
                                        .content(asJsonString(v)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    students.forEach(v -> {
      try {
        mockMvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON)
                                        .content(asJsonString(v)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    courses.forEach(v -> {
      try {
        mockMvc.perform(post("/course").contentType(MediaType.APPLICATION_JSON)
                                       .content(asJsonString(v)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    subjects.forEach(subject -> {
      try {
        mockMvc.perform(post("/subject").contentType(MediaType.APPLICATION_JSON)
                                        .content(asJsonString(subject)));
      } catch (Exception e) {
        e.printStackTrace();
      }

    });
    courses = courseRepository.findAll();
    subjects = subjectRepository.findAll();
    System.out.println("Set up finish");
  }

  @Test
  public void addCourse() throws Exception {
    System.out.println(courseRepository.findAll());
    Course course = new Course("男生网球周四3-4节", 201601);
    mockMvc.perform(post("/course").contentType(MediaType.APPLICATION_JSON)
                                   .content(asJsonString(course)))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location",
                                      "http://localhost/course/" + (courses.stream()
                                                                           .mapToInt(Course::getId)
                                                                           .max()
                                                                           .getAsInt() + 1)));
  }

  @Test
  public void findById() throws Exception {
    System.out.println(courseRepository.findAll());
    courses.forEach(course -> {
      try {
        System.out.println("/course/" + course.getId());
        mockMvc.perform(get("/course/" + course.getId()))
               .andExpect(status().isOk())
               .andExpect(content().json(asJsonString(course)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void findByNameAndSemester() throws Exception {
    System.out.println(courseRepository.findAll());
    System.out.println("begin");
    courses.forEach(course -> {
      try {
        mockMvc.perform(get("/course").param("name", course.getName())
                                      .param("semester", String.valueOf(course.getSemester())))
               .andExpect(status().isOk())
               .andDo(res -> System.out.println("Got: " + res.getResponse().getContentAsString()))
               .andExpect(content().json(asJsonString(Collections.singletonList(course))));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void findBySemester() throws Exception {
    mockMvc.perform(get("/course").param("semester", "201601"))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(courses)));
  }

  @Test
  public void updateCourse() throws Exception {
    System.out.println(courseRepository.findAll());
    courses.forEach(course -> {
      try {
        mockMvc.perform(get("/course/" + course.getId()))
               .andExpect(status().isOk())
               .andExpect(content().json(asJsonString(course)));
        course.setSemester(201602);
        mockMvc.perform(put("/course/" + course.getId()).contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(course)))
               .andExpect(status().isNoContent());
        mockMvc.perform(get("/course/" + course.getId()))
               .andExpect(status().isOk())
               .andExpect(content().json(asJsonString(course)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void deleteCourse() throws Exception {
    System.out.println(courseRepository.findAll());
    courses.forEach(course -> {
      try {
        mockMvc.perform(get("/course/" + course.getId()))
               .andExpect(status().isOk())
               .andExpect(content().json(asJsonString(course)));
        mockMvc.perform(delete("/course/" + course.getId()))
               .andExpect(status().isNoContent());
        mockMvc.perform(get("/course/" + course.getId()))
               .andExpect(status().isNotFound());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void testTeacher() throws Exception {
    System.out.println(courseRepository.findAll());
    Teacher teacher = teachers.get(0);
    courses.forEach(course -> {
      try {
        mockMvc.perform(get("/course/" + course.getId() + "/teacher"))
               .andExpect(status().isNotFound());
        mockMvc.perform(put("/course/" + course.getId() + "/teacher")
                            .param("employeeId", String.valueOf(teacher.getEmployeeId())))
               .andDo(print())
               .andExpect(status().isNoContent());
        mockMvc.perform(get("/course/" + course.getId() + "/teacher"))
               .andExpect(status().isOk())
               .andExpect(content().json(asJsonString(teacher)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void testStudent() throws Exception {
    System.out.println(courseRepository.findAll());
    Course course = courses.get(0);
    mockMvc.perform(get("/course/" + course.getId() + "/student"))
           .andExpect(status().isOk())
//           .andDo(res -> {
//             System.out.println(res.getResponse().getContentAsString());
//           });
           .andExpect(content().json(asJsonString(Collections.emptyList())));
    students.forEach(student -> {
      try {
        mockMvc.perform(post("/course/" + course.getId() + "/student")
                            .param("studentId", String.valueOf(student.getStudentId())))
               .andExpect(status().isNoContent());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    mockMvc.perform(get("/course/" + course.getId() + "/student"))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(students)));
    students.forEach(student -> {
      try {
        mockMvc.perform(delete("/course/" + course.getId() + "/student/" + student.getStudentId()))
               .andExpect(status().isNoContent());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    mockMvc.perform(get("/course/" + course.getId() + "/student"))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(Collections.emptyList())));
  }

  @Test
  public void testSubject() throws Exception {
    Course course = courses.get(0);
    mockMvc.perform(get("/course/{courseId}/standard", course.getId()))
           .andExpect(status().isOk())
           .andExpect(content().json(asJsonString(Collections.emptyList())));

    subjects.forEach(subject -> {
      try {
        mockMvc.perform(post("/course/{courseId}/standard", course.getId())
                            .param("subjectId", String.valueOf(subject.getId()))
                            .param("weight", String.valueOf(40)))
               .andExpect(status().isNoContent());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    List<Weight> weights = weightRepository.findAll();
    mockMvc.perform(get("/course/{courseId}/weight", course.getId()))
           .andExpect(status().isOk())
           .andDo(print())
           .andExpect(content().json(asJsonString(weights)));
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