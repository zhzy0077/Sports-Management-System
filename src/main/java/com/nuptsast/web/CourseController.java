package com.nuptsast.web;

import com.nuptsast.domain.*;
import com.nuptsast.exception.InternalException;
import com.nuptsast.service.CourseService;
import com.nuptsast.service.ScoreService;
import com.nuptsast.util.Utilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
@RestController
public class CourseController {
  private final CourseService courseService;
  private final ScoreService scoreService;
  private final Log log = LogFactory.getLog(this.getClass());

  @Autowired
  public CourseController(CourseService courseService, ScoreService scoreService) {
    this.courseService = courseService;
    this.scoreService = scoreService;
  }

  @RequestMapping(value = "/course", method = RequestMethod.POST)
  public ResponseEntity<?> addCourse(@RequestBody Course course,
                                     UriComponentsBuilder uriBuilder) {
    course = courseService.addCourse(course);
    return ResponseEntity.created(uriBuilder.path("/course/{id}")
                                            .buildAndExpand(course.getId())
                                            .toUri())
                         .build();
  }

  @RequestMapping(value = "/course/{id}", method = RequestMethod.GET)
  public Course findById(@PathVariable Integer id) {
    return courseService.fetch(id);
  }

  @RequestMapping(value = "/course/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateCourse(@PathVariable Integer id, @RequestBody Course course) {
    course.setId(id);
    courseService.update(course);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteCourse(@PathVariable Integer id) {
    courseService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course", method = RequestMethod.GET)
  public List<Course> findByNameAndSemester(@RequestParam(required = false) String name,
                                            @RequestParam Integer semester,
                                            Principal principal) {
    if (principal instanceof UsernamePasswordAuthenticationToken) {
      UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
      TeacherDetails teacherDetails = (TeacherDetails) token.getPrincipal();
      if (!teacherDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
        return courseService.fetchByTeacher(name, semester, principal.getName());
      } else {
        return courseService.fetch(name, semester);
      }
    }
    return courseService.fetch(name, semester);
//        return courseService.fetch(name, semester).stream()
//                            .filter(course -> course.getTeacher()
//                                                    .getEmployeeId()
//                                                    .equals(Integer.parseInt(teacherDetails.getUsername())))
//                            .collect(Collectors.toList());
//      } else {
//        return courseService.fetch(name, semester);
//      }
//    }


  }

  @RequestMapping(value = "/course/{id}/teacher", method = RequestMethod.GET)
  public Teacher findTeacher(@PathVariable Integer id) {
    return courseService.fetchTeacher(id);
  }

  @RequestMapping(value = "/course/{id}/teacher", method = RequestMethod.PUT)
  public ResponseEntity<?> setTeacher(@PathVariable Integer id,
                                      @RequestParam String employeeId) {
    courseService.setTeacher(id, employeeId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}/student", method = RequestMethod.POST)
  public ResponseEntity<?> addStudent(@PathVariable Integer id,
                                      @RequestParam String studentId) {
    courseService.addStudent(id, studentId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}/student/upload", method = RequestMethod.POST)
  public ResponseEntity<?> uploadStudent(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
    try (InputStream stream = file.getInputStream()) {
      courseService.uploadStudent(stream);
    } catch (IOException e) {
      throw new InternalException();
    }
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}/student", method = RequestMethod.GET)
  public Collection<Student> findStudent(@PathVariable Integer id) {
    return courseService.getStudent(id);
  }

  @RequestMapping(value = "/course/{id}/student/{studentId}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteStudent(@PathVariable Integer id,
                                         @PathVariable String studentId) {
    courseService.deleteStudent(id, studentId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}/student/{studentId}/score", method = RequestMethod.GET)
  public String getScore(@PathVariable Integer id,
                         @PathVariable String studentId) {
    Map<String, Map<String, String>> score = scoreService.getScore(studentId, id);
    return Utilities.generateJson(score);
  }

  @RequestMapping(value = "/course/{id}/student/{studentId}/score", method = RequestMethod.PUT)
  public ResponseEntity<?> setScore(@PathVariable Integer id,
                                    @PathVariable String studentId,
                                    @RequestParam Integer weightId,
                                    @RequestParam(required = false) String score) {
    if (score == null) {
      scoreService.deleteScore(studentId, id, weightId);
    }
    scoreService.setScore(studentId, id, weightId, score);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}/weight", method = RequestMethod.GET)
  public Collection<Weight> allSubject(@PathVariable Integer id) {
    return courseService.getSubject(id);
  }

  @RequestMapping(value = "/course/{id}/weight", method = RequestMethod.POST)
  public ResponseEntity<?> addSubject(@PathVariable Integer id,
                                      @RequestParam Integer subjectId,
                                      @RequestParam Integer weight,
                                      @RequestParam Integer standardId) {
    courseService.addSubject(id, subjectId, weight, standardId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}/weight/{weightId}", method = RequestMethod.GET)
  public Weight getSubject(@PathVariable Integer id,
                           @PathVariable Integer weightId) {
    return courseService.getSubject(id, weightId);
  }

  @RequestMapping(value = "/course/{id}/weight/{weightId}", method = RequestMethod.PUT)
  public ResponseEntity<?> getSubject(@PathVariable Integer id,
                                      @PathVariable Integer weightId,
                                      @RequestParam Integer weight) {
    courseService.updateWeight(id, weightId, weight);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}/weight/{weightId}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteSubject(@PathVariable Integer id,
                                         @PathVariable Integer weightId) {
    courseService.deleteSubject(id, weightId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}/score/template.xlsx", method = RequestMethod.GET)
  public void downloadTemplate(@PathVariable Integer id,
                               ServletResponse response) {
    try (OutputStream stream = response.getOutputStream()) {
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      courseService.writeTemplate(id, stream);
      stream.flush();
    } catch (IOException e) {
      throw new InternalException();
    }
  }

  @RequestMapping(value = "/course/{id}/score/upload", method = RequestMethod.POST)
  public ResponseEntity<?> uploadScore(@RequestParam("file") MultipartFile file,
                                       @PathVariable Integer id) {
    File score = new File("score.xlsx");
    try (InputStream stream = file.getInputStream()) {
      Files.copy(stream, Paths.get("score.xlsx"), StandardCopyOption.REPLACE_EXISTING);
      scoreService.uploadScore(score, id);
    } catch (IOException e) {
      throw new InternalException("Error Happens", e);
    }
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{id}/download", method = RequestMethod.GET)
  public void downLoadScore(@PathVariable Integer id,
                            ServletResponse response) {
    try (OutputStream stream = response.getOutputStream()) {
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      courseService.exportExcelByCourse(id, stream);
      stream.flush();
    } catch (IOException e) {
      throw new InternalException();
    }
  }

  @RequestMapping(value = "/course/upload", method = RequestMethod.POST)
  public ResponseEntity<?> uploadCourse(@RequestParam("file") MultipartFile file) {
    try (InputStream stream = file.getInputStream()) {
      courseService.uploadCourse(stream);
    } catch (IOException e) {
      throw new InternalException();
    }
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/course/{courseId}/student/score", method = RequestMethod.GET)
  public List<Map<String, Object>> getAllScore(@PathVariable Integer courseId) {
    return courseService.generateCourseInformation(courseId);
  }

  @RequestMapping(value = "/export/{semester}", method = RequestMethod.GET)
  public void writeAllScore(@PathVariable Integer semester, ServletResponse response) {
    try (OutputStream stream = response.getOutputStream()) {
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      courseService.exportExcelBySemester(semester, stream);
      stream.flush();
    } catch (IOException e) {
      throw new InternalException("Error Happens", e);
    }
  }
}
