package com.nuptsast.service;

import com.nuptsast.domain.*;
import com.nuptsast.exception.InternalException;
import com.nuptsast.exception.NotFoundException;
import com.nuptsast.repository.*;
import com.nuptsast.util.Utilities;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import static com.nuptsast.util.Utilities.fetch;
import static java.util.stream.Collectors.toList;

/**
 * Created by zheng on 2016/11/19.
 * For fit-jpa.
 */
@Service
public class ScoreServiceImpl implements ScoreService {

  private final StudentRepository studentRepository;
  private final ScoreRepository scoreRepository;
  private final GradingRepository gradingRepository;

  @PersistenceContext
  private EntityManager entityManager;


  private final Log logger = LogFactory.getLog(this.getClass());

  private final CourseRepository courseRepository;
  private final DataFormatter formatter = new DataFormatter();


  @Autowired
  public ScoreServiceImpl(StudentRepository studentRepository, ScoreRepository scoreRepository, GradingRepository gradingRepository, StandardRepository standardRepository, CourseRepository courseRepository) {
    this.studentRepository = studentRepository;
    this.scoreRepository = scoreRepository;
    this.gradingRepository = gradingRepository;
    this.courseRepository = courseRepository;
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void setScore(String studentId, Integer courseId, Integer weightId, String score) {
    Score sco = scoreRepository.findByStudent_StudentIdAndCourse_IdAndWeight_Id(studentId, courseId, weightId);
    if (sco != null) {
      sco.setScore(
          (int) (Math.round(Double.parseDouble(score) * Math.pow(10, sco.getSubject().getSuffix()))));
    } else {
      Student student = fetch(() -> studentRepository.findByStudentId(studentId));
      Course course = student.getCourses().stream()
                             .filter(c -> c.getId().equals(courseId)).findAny()
                             .orElseThrow(() -> new NotFoundException("course: " + courseId + " not found"));
      Weight weight = course.getWeights().stream()
                            .filter(v -> v.getId().equals(weightId))
                            .findAny()
                            .orElseThrow(() -> new NotFoundException("weight: " + weightId + " not found"));
      Subject subject = weight.getSubject();
      Integer s = (int) (Math.round(Double.parseDouble(score) * Math.pow(10, subject.getSuffix())));
      if (s > subject.getMax() || s < subject.getMin()) {
        throw new IllegalArgumentException(String.valueOf(s));
      }
      Score newScore = new Score(weight, student, course, s, weight.getSubject());
      newScore = scoreRepository.save(newScore);
      student.getScores().add(newScore);
    }
  }

  @Override
  @Transactional()
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void uploadScore(File file, Integer courseId) {
    Course templateCourse = Utilities.fetch(() -> courseRepository.findById(courseId));
    entityManager.setFlushMode(FlushModeType.COMMIT);
    try (Workbook workbook = WorkbookFactory.create(file)) {
      Sheet workSheet = workbook.getSheetAt(0);
      Row row0 = workSheet.getRow(0);

      logger.info("process xls");
      List<String> weightNames = new ArrayList<>();
      for (int i = row0.getFirstCellNum() + 1; i < row0.getLastCellNum(); i++) {
        String name = row0.getCell(i).getStringCellValue();
        weightNames.add(name);
      }
      List<String> studentIds = new ArrayList<>();
      List<Future<Student>> students = new ArrayList<>();
      Map<String, List<String>> studentScores = new HashMap<>();
      for (int i = 1; i <= workSheet.getLastRowNum(); i++) {
        Row row = workSheet.getRow(i);
        String studentId = formatter.formatCellValue(row.getCell(0));
        if ("".equals(studentId.trim())) {
          continue;
        } else if (studentScores.containsKey(studentId)) {
          continue;
        }
        List<String> scores = new ArrayList<>();
        for (int i1 = 0; i1 < weightNames.size(); i1++) {
          String score = formatter.formatCellValue(row.getCell(i1 + 1));
          scores.add(score);
        }
        studentIds.add(studentId);
        Future<Student> student = studentRepository.findByStudentIdAsyc(studentId);
        students.add(student);
        studentScores.put(studentId, scores);
      }
      logger.info("xls finished");
      Map<String, Future<List<Score>>> scoresHold = new HashMap<>();
      for (int i = 0; i < studentIds.size(); i++) {
        try {
          String studentId = studentIds.get(i);
          Student student = students.get(i).get();
          if (student == null) {
            throw new IllegalArgumentException("学生: " + studentId + "未找到");
          }
          scoresHold.put(studentId, scoreRepository.findByStudentIdAndSemester(studentId, templateCourse.getSemester()));
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } catch (ExecutionException e) {
          throw new InternalException(e);
        }
      }
      logger.info("future sent");
      for (int i = 0; i < studentIds.size(); i++) {
        try {
          String studentId = studentIds.get(i);
          Student student = students.get(i).get();
          Course course = courseRepository.findByStudentIdAndSemester(student, templateCourse.getSemester())
                                 .stream().findAny()
                                 .orElseThrow(() -> new IllegalArgumentException("学生" + studentId + "未找到对应的教学班级"));
          List<Score> scoreHold = scoresHold.get(studentId).get();
          List<String> scoreInput = studentScores.get(studentId);
          Collection<Weight> weights = course.getWeights();
          for (int j = 0; j < weightNames.size(); j++) {
            String weightName = weightNames.get(j);
            Weight weight = null;
            for (Weight w : weights) {
              if (w.getSubject().getName().equals(weightName)) {
                weight = w;
                break;
              }
            }
            if (weight == null) {
              continue;
            }
            Subject subject = weight.getSubject();
            String scoreString = scoreInput.get(j);
            if ("".equals(scoreString.trim())) { continue; }
            double scoreDouble;
            try {
              scoreDouble = Double.parseDouble(scoreInput.get(j));
            } catch (NumberFormatException e) {
              throw new IllegalArgumentException("数据: " + scoreString + "有误", e);
            }
            int scoreInt = (int) (Math.round(scoreDouble * Math.pow(10, subject.getSuffix())));
            if (scoreInt > subject.getMax() || scoreInt < subject.getMin()) {
                throw new IllegalArgumentException("数据: " + scoreString + "有误");
            }
            Score scoreHas = null;
            for (Score score : scoreHold) {
              if (getIdDirect(score.getSubject()).equals(subject.getId())) {
                scoreHas = score;
                break;
              }
            }
            if (scoreHas == null) {
              scoreRepository.save(new Score(weight, student, course, scoreInt, subject));
            } else {
              scoreHas.setScore(scoreInt);
            }
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } catch (ExecutionException e) {
          throw new InternalException(e);
        }
      }
      logger.info("update finished");
    } catch (IOException e) {
      e.printStackTrace();
      throw new InternalException();
    } catch (InvalidFormatException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("invalid input");
    }
  }

  @Override
  @Transactional
  @PreAuthorize("hasAnyRole('ALL', 'ADMIN', #studentId)")
  public Map<String, Map<String, String>> getScore(String studentId, Integer courseId) {
    Student student = fetch(() -> studentRepository.findByStudentId(studentId));
    return calc(student, course -> course.getId().equals(courseId));
  }

  @Override
  @Transactional
  @PreAuthorize("hasAnyRole('ALL', 'ADMIN', #studentId)")
  public Map<String, Map<String, String>> getScoreBySemester(String studentId, Integer semester) {
    Validate.notNull(semester);
    Student student = fetch(() -> studentRepository.findByStudentId(studentId));
    return calc(student, course -> course.getSemester().equals(semester));
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void deleteScore(String studentId, Integer id, Integer weightId) {
    Score sco = scoreRepository.findByStudent_StudentIdAndCourse_IdAndWeight_Id(studentId, id, weightId);
    if (sco != null) {
      scoreRepository.delete(sco);
    }
  }

  private Map<String, Map<String, String>> calc(Student student, Predicate<Course> predicate) {
    List<Score> scores = student.getScores()
                                .stream()
                                .filter(score -> predicate.test(score.getCourse()))
                                .collect(toList());
    Map<String, Map<String, String>> ans = new HashMap<>();
    int height = 0;
    int weight = 0;
    int sum = 0;
    int max = 10000;
    int absolute = 0;
    String special = "";
    for (Score score : scores) {
      Map<String, String> cur = new HashMap<>();
      logger.info(score);
      if (score.getWeight().getSubject().getName().contains("体重")) {
        weight = score.getScore();
      } else if (score.getWeight().getSubject().getName().contains("身高")) {
        height = score.getScore();
      } else {
        int s = gradingRepository.findByScore(score.getScore(),
                                              score.getWeight().getStandard().getId())
                                 .orElseThrow(IllegalArgumentException::new)
                                 .getScore();
        cur.put("mark", String.valueOf(s));
        if (score.getWeight().getSubject().getName().contains("晨跑次数")
            && s == -1) {
          max = 5900;
          s = 0;
        } else if (score.getWeight().getSubject().getName().contains("缓考")
            && s == 1) {
          special = "缓考";
          s = 0;
        } else if (score.getWeight().getSubject().getName().contains("旷考")
            && s == 1) {
          special = "旷考";
          s = 0;
        } else if (score.getWeight().getSubject().getName().contains("免测")
            && s != 0) {
          absolute = s;
          s = 0;
        }
        sum += s * score.getWeight().getWeight();
      }
      cur.put("score", Utilities.formatScore(score.getScore(), score.getSubject().getSuffix()));
      ans.put(score.getSubject().getName(), cur);
    }
    if (height != 0) {
      Weight bmi = student.getCourses()
                          .stream()
                          .filter(predicate)
                          .flatMap(course -> course.getWeights().stream())
                          .filter(w -> w.getSubject().getName().contains("BMI"))
                          .findAny().orElseThrow(IllegalArgumentException::new);

      int b = (int) (weight * (long) Math.pow(10, 6) / (height * height));
      int s = gradingRepository.findByScore(b, bmi.getStandard().getId())
                               .orElseThrow(IllegalArgumentException::new)
                               .getScore();
      logger.info("height: " + height + " weight: " + weight + " bmi: " + bmi + " bmi: " + b + " score: " + s);
      Map<String, String> cur = new HashMap<>();
      b = b * 100;
      cur.put("score", Utilities.formatScore(b, bmi.getSubject().getSuffix()));
      cur.put("mark", String.valueOf(s));
      sum += s * bmi.getWeight();
      ans.put(bmi.getSubject().getName(), cur);
    }
    if (absolute != 0) {
      ans.put("total", Collections.singletonMap("mark", String.valueOf(absolute)));
    } else if (Objects.equals(special, "")) {
      ans.put("total", Collections.singletonMap("mark", String.valueOf(((double) (Integer.min(max, sum) / 10)) / 10)));
    } else {
      ans.put("total", Collections.singletonMap("mark", special));
    }

    return ans;
  }
  private static Integer getIdDirect(Object entity) {
    if (entity instanceof HibernateProxy) {
      LazyInitializer lazyInitializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
      if (lazyInitializer.isUninitialized()) {
        return (Integer) lazyInitializer.getIdentifier();
      }
    }
    return null;
  }
}
