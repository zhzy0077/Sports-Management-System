package com.nuptsast.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuptsast.domain.*;
import com.nuptsast.repository.CourseRepository;
import com.nuptsast.repository.ScoreRepository;
import com.nuptsast.repository.StudentRepository;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by zheng on 2016/11/20.
 * For fit-jpa.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithMockUser(roles = "ADMIN")
public class IntegralTest {
  private List<Student> students = Arrays.asList(
      new Student("B14112133", "郑致远", "B140407", false),
      new Student("B14040703", "张三", "B140407", false),
      new Student("B14040624", "刘强胜", "B140406", false)
  );
  private List<Teacher> teachers = Arrays.asList(
      new Teacher("19870006", "张三", 0),
      new Teacher("20140012", "李四", 1),
      new Teacher("20121234", "王五", 2)
  );
  private List<Subject> subjects = Arrays.asList(
      new Subject("50米", 2, 2000, 0),
      new Subject("引体向上", 0, 50, 0),
      new Subject("2000米", 2, 2000, 0),
      new Subject("平时成绩", 0, 100, 0)
  );

  private List<Course> courses = Arrays.asList(
      new Course("男生篮球周二8-9节", 201601),
      new Course("男生篮球周二3-4节", 201601),
      new Course("男生网球周二3-4节", 201601)
  );
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private WeightRepository weightRepository;

  @Autowired
  private CourseRepository courseRepository;

  private MockMvc mockMvc;
  @Autowired
  private StudentRepository studentRepository;
  @Autowired
  private ScoreRepository scoreRepository;

  @Before
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  public void integral() throws Exception {
    // add teacher
    for (Teacher teacher : teachers) {
      mockMvc.perform(post("/teacher").contentType(MediaType.APPLICATION_JSON)
                                      .content(asJsonString(teacher)))
             .andExpect(status().isCreated());
    }
    // add student
    for (Student student : students) {
      mockMvc.perform(post("/student").contentType(MediaType.APPLICATION_JSON)
                                      .content(asJsonString(student)))
             .andExpect(status().isCreated());
    }
    // add course
    for (Course course : courses) {
      mockMvc.perform(post("/course").contentType(MediaType.APPLICATION_JSON)
                                     .content(asJsonString(course)))
             .andExpect(status().isCreated())
             .andDo(result -> {
               String[] args = result.getResponse().getHeader("Location").split("/");
               course.setId(Integer.parseInt(args[args.length - 1]));
             });
      // set teacher
      Teacher teacher = teachers.get(1);
      mockMvc.perform(put("/course/{courseId}/teacher", course.getId())
                          .param("employeeId", String.valueOf(teacher.getEmployeeId())))
             .andExpect(status().isNoContent());
    }
    List<List<Grading>> gradings = Arrays.asList(
        Arrays.asList(
            new Grading(Integer.MIN_VALUE, 1000, 60),
            new Grading(1000, Integer.MAX_VALUE, 100)
        ),
        Arrays.asList(
            new Grading(Integer.MIN_VALUE, 10, 60),
            new Grading(10, Integer.MAX_VALUE, 100)
        ),
        Arrays.asList(
            new Grading(Integer.MIN_VALUE, 1000, 10),
            new Grading(1000, 1500, 60),
            new Grading(1500, Integer.MAX_VALUE, 100)
        ),
        IntStream.rangeClosed(0, 100)
                 .mapToObj(v -> new Grading(v, v + 1, v))
                 .collect(Collectors.toList())
    );
    for (int i = 0; i < subjects.size(); i++) {
      Subject subject = subjects.get(i);
      List<Grading> grading = gradings.get(i);
      // add subject
      mockMvc.perform(post("/subject").contentType(MediaType.APPLICATION_JSON)
                                      .content(asJsonString(subject)))
             .andExpect(status().isCreated())
             .andDo(result -> {
               String[] args = result.getResponse().getHeader("Location").split("/");
               subject.setId(Integer.parseInt(args[args.length - 1]));
             });
      // set gradings
      mockMvc.perform(put("/subject/{id}/grading", subject.getId()).contentType(MediaType.APPLICATION_JSON)
                                                                   .content(asJsonString(grading)))
             .andExpect(status().isNoContent());
    }
    // take class
    Course course = courses.get(0);
    for (Student student : students) {
      mockMvc.perform(post("/course/{courseId}/student", course.getId())
                          .param("studentId", student.getStudentId()))
             .andExpect(status().isNoContent());
    }
    // set subject
    for (Subject subject : subjects) {
      mockMvc.perform(post("/course/{courseId}/weight", course.getId())
                          .param("subjectId", String.valueOf(subject.getId()))
                          .param("weight", String.valueOf(25)))
             .andExpect(status().isNoContent());
    }
    List<Weight> weights = weightRepository.findAll();
    List<Integer> scores = Arrays.asList(1000, 10, 1500, 100);
    Student student = students.get(0);
    System.out.println("Course: " + courseRepository.findAll());
    System.out.println("student: " + studentRepository.findAll()
                                                      .stream()
                                                      .map(Student::getCourses)
                                                      .map(Object::toString)
                                                      .collect(Collectors.joining(",")));
    for (int i = 0; i < weights.size(); i++) {
      mockMvc.perform(put("/course/{courseId}/student/{studentId}/score", course.getId(), student.getStudentId())
                          .param("weightId", String.valueOf(weights.get(i).getId()))
                          .param("score", String.valueOf(scores.get(i))))
             .andExpect(status().isNoContent());
    }
    System.out.println(scoreRepository.findAll());
    mockMvc.perform(get("/student/{studentId}/score/201601", student.getStudentId()))
           .andDo(print());
    // "/score/B140406"
    // /course/{courseId}/score

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
