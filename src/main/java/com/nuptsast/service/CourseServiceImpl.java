package com.nuptsast.service;

import com.nuptsast.domain.Course;
import com.nuptsast.domain.Score;
import com.nuptsast.domain.Standard;
import com.nuptsast.domain.Student;
import com.nuptsast.domain.Subject;
import com.nuptsast.domain.Teacher;
import com.nuptsast.domain.Weight;
import com.nuptsast.exception.ConflictException;
import com.nuptsast.exception.InternalException;
import com.nuptsast.exception.NotFoundException;
import com.nuptsast.repository.CourseRepository;
import com.nuptsast.repository.ScoreRepository;
import com.nuptsast.repository.StandardRepository;
import com.nuptsast.repository.StudentRepository;
import com.nuptsast.repository.SubjectRepository;
import com.nuptsast.repository.TeacherRepository;
import com.nuptsast.repository.WeightRepository;
import com.nuptsast.util.Utilities;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by zheng on 2016/11/18.
 * For fit-jpa.
 */
@Service
public class CourseServiceImpl implements CourseService {
  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;
  private final StudentRepository studentRepository;
  private final WeightRepository weightRepository;
  private final SubjectRepository subjectRepository;
  private final StandardRepository standardRepository;
  private final ScoreRepository scoreRepository;

  private final Log log = LogFactory.getLog(this.getClass());

  @Autowired
  public CourseServiceImpl(CourseRepository courseRepository,
                           TeacherRepository teacherRepository,
                           StudentRepository studentRepository,
                           WeightRepository weightRepository,
                           SubjectRepository subjectRepository,
                           StandardRepository standardRepository,
                           ScoreRepository scoreRepository) {
    this.courseRepository = courseRepository;
    this.teacherRepository = teacherRepository;
    this.studentRepository = studentRepository;
    this.weightRepository = weightRepository;
    this.subjectRepository = subjectRepository;
    this.standardRepository = standardRepository;
    this.scoreRepository = scoreRepository;
  }

  @Override
  @Secured("ROLE_ADMIN")
  public Course addCourse(Course course) {
    return courseRepository.save(course);
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public List<Course> fetch(String name, Integer semester) {
    if (name == null) {
      return courseRepository.findBySemester(semester)
                             .stream()
                             .sorted(Comparator.comparing((Function<Course, Boolean>) cou -> cou.getTeacher() == null)
                                               .thenComparing(cou -> cou.getTeacher() == null ? "" : cou.getTeacher().getName())
                                               .thenComparing(Course::getSemester)
//                                 .thenComparing(cou -> cou.getSemester())
                             )
                             .collect(Collectors.toList());
    }
    return Collections.singletonList(
        Utilities.fetch(() -> courseRepository.findByNameAndSemester(name, semester))
    );
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public List<Course> fetchByTeacher(String name, Integer semester, String employeeId) {
    return fetch(name, semester)
        .stream()
        .filter(course ->
                    course.getTeacher() != null &&
                        course.getTeacher().getEmployeeId().equals(employeeId)
        )
        .sorted(Comparator.comparing((Function<Course, Boolean>) cou -> cou.getTeacher() == null)
                          .thenComparing(cou -> cou.getTeacher() == null ? "" : cou.getTeacher().getName())
                          .thenComparing(Course::getSemester)
//                                 .thenComparing(cou -> cou.getSemester())
        )
        .collect(Collectors.toList());
  }

  @Override
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public Course fetch(Integer id) {
    return Utilities.fetch(() -> courseRepository.findById(id));
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void update(Course course) {
    courseRepository.findById(course.getId())
                    .ifPresent(cou -> Utilities.copyProperties(course, cou));
  }

  @Override
  @Secured("ROLE_ADMIN")
  public void delete(Integer courseId) {
    courseRepository.delete(courseId);
  }

  @Override
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public Teacher fetchTeacher(Integer courseId) {
    return courseRepository.findById(courseId)
                           .map(Course::getTeacher)
                           .orElseThrow(NotFoundException::new);
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void setTeacher(Integer courseId, String employeeId) {
    Course course = fetch(courseId);
    Teacher teacher = Utilities.fetch(() -> teacherRepository.findByEmployeeId(employeeId));
    course.setTeacher(teacher);
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void addStudent(Integer courseId, String studentId) {
    Course course = fetch(courseId);
    Student student = Utilities.fetch(() -> studentRepository.findByStudentId(studentId));
//    if (student.getCourses()
//               .stream()
//               .anyMatch(cou -> cou.getSemester()
//                                   .equals(course.getSemester()))
//        ) {
//      throw new ConflictException();
//    }
    student.getCourses()
           .stream()
           .filter(cou -> cou.getSemester()
                             .equals(course.getSemester()))
           .findAny()
           .ifPresent(c -> {
             c.getStudents().remove(student);
             student.getCourses().remove(c);
           });
    course.getStudents().add(student);
    student.getCourses().add(course);
  }

  @Override
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public Collection<Student> getStudent(Integer courseId) {
    return fetch(courseId).getStudents();
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void deleteStudent(Integer courseId, String studentId) {
    Student student = Utilities.fetch(() -> studentRepository.findByStudentId(studentId));
    Course course = fetch(courseId);
    course.getStudents().remove(student);
    student.getCourses().remove(course);
  }

  @Override
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  @Transactional
  public int uploadStudent(InputStream inputStream) {
    try {
      List<List<String>> students =
          Utilities.readExcel(inputStream, row -> Arrays.asList(row.get(0), row.get(1), row.get(2)));
      for (int i = 0; i < students.size(); i++) {
        List<String> strings = students.get(i);
        try {
          Student student = Utilities.fetch(() -> studentRepository.findByStudentId(strings.get(0)));
          Course course = Utilities.fetch(() -> courseRepository.findByNameAndSemester(strings.get(1),
                                                                                       Integer.parseInt(strings.get(2))));
          log.info(student + " takes " + course);
          course.getStudents().add(student);
          student.getCourses().add(course);
        } catch (NotFoundException e) {
          throw new IllegalArgumentException("Exception happened on line: " + (i + 1));
        }
      }
      return students.size();
    } catch (IOException e) {
      throw new InternalException();
    } catch (InvalidFormatException e) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void addSubject(Integer courseId, Integer subjectId, Integer weight, Integer standardId) {
    Course course = Utilities.fetch(() -> courseRepository.findById(courseId));
    if (course.getWeights().stream().anyMatch(w -> w.getSubject().getId().equals(subjectId))) {
      throw new ConflictException();
    }
    Subject subject = Utilities.fetch(() -> subjectRepository.findById(subjectId));
    Standard standard = Utilities.fetch(() -> standardRepository.findById(standardId));
    Weight w = new Weight(subject, course, weight);
    w.setStandard(standard);
    w = weightRepository.save(w);
    course.getWeights().add(w);
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public Collection<Weight> getSubject(Integer courseId) {
    Course course = Utilities.fetch(() -> courseRepository.findById(courseId));
    return course.getWeights();
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public Weight getSubject(Integer courseId, Integer weightId) {
    Course course = Utilities.fetch(() -> courseRepository.findById(courseId));
    return course.getWeights()
                 .stream()
                 .filter(v -> v.getId().equals(weightId))
                 .findAny()
                 .orElseThrow(NotFoundException::new);
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void updateWeight(Integer courseId, Integer weightId, Integer newWeight) {
    Course course = Utilities.fetch(() -> courseRepository.findById(courseId));
    course.getWeights()
          .stream()
          .filter(v -> v.getId().equals(weightId))
          .forEach(v -> v.setWeight(newWeight));
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void updateStandard(Integer courseId, Integer weightId, Integer standardId) {
    Course course = Utilities.fetch(() -> courseRepository.findById(courseId));
    Standard standard = Utilities.fetch(() -> standardRepository.findById(standardId));
    course.getWeights()
          .stream()
          .filter(v -> v.getId().equals(weightId))
          .forEach(v -> v.setStandard(standard));
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void deleteSubject(Integer courseId, Integer weightId) {
    Course course = Utilities.fetch(() -> courseRepository.findById(courseId));
    Weight weight = Utilities.fetch(() -> weightRepository.findById(weightId));
    weightRepository.delete(weight);
    course.getWeights().remove(weight);
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void writeTemplate(Integer courseId, OutputStream outputStream) {
    Course course = Utilities.fetch(() -> courseRepository.findById(courseId));
    List<String> list = new ArrayList<>();
    list.add("学号");
    try {
      list.addAll(course.getWeights()
                        .stream()
                        .map(c -> c.getSubject().getName())
                        .filter(name -> !name.contains("BMI"))
                        .collect(Collectors.toList()));
      Utilities.writeExcel(outputStream, list, course.getStudents()
                                                     .stream()
                                                     .map(Student::getStudentId)
                                                     .sorted()
                                                     .collect(Collectors.toList()));
    } catch (IOException e) {
      e.printStackTrace();
      throw new InternalException();
    }
  }

  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void uploadCourse(InputStream inputStream) {
    try {
      List<Course> courses = Utilities.readExcel(inputStream,
                                                 row -> new Course(row.get(0),
                                                                   Integer.parseInt(row.get(1))));
      for (int i = 0; i < courses.size(); i++) {
        try {
          addCourse(courses.get(i));
        } catch (DataIntegrityViolationException e) {
          throw new IllegalArgumentException("Exception happened on line: " + (i + 1));
        }
      }

    } catch (IOException e) {
      throw new InternalException();
    } catch (InvalidFormatException e) {
      throw new IllegalArgumentException();
    }
  }


  @Override
  @Transactional
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public List<Map<String, Object>> generateCourseInformation(Integer courseId) {
    Course course = Utilities.fetch(() -> courseRepository.findById(courseId));
    Collection<Student> students = course.getStudents();
    Collection<Weight> weights = course.getWeights();
    Map<Student, Triple<Course, List<Triple<String, String, String>>, String>> totalScore = calculateScore(Collections.singletonList(course),
                                                                                   2);
    return students.stream().map(student -> {
      Map<String, Object> map = new HashMap<>();
      map.put("studentId", student.getStudentId());
      map.put("name", student.getName());
      map.put("total", totalScore.get(student).getRight());
      Map<String, Score> scores = student.getScores().stream()
                                         .filter(score -> score.getCourse().getId().equals(courseId))
                                         .collect(Collectors.toMap(score -> score.getSubject().getName(),
                                                                   Function.identity()));
      List<Map<String, String>> subjects =
          weights.stream()
                 .filter(weight -> !weight.getSubject().getName().contains("BMI"))
                 .map(weight -> {
                   Subject s = weight.getSubject();
                   String name = s.getName();
                   Map<String, String> subject = new HashMap<>();
                   subject.put("name", name);
                   subject.put("id", String.valueOf(weight.getId()));
                   if (scores.containsKey(name)) {
                     Score score = scores.get(name);
                     subject.put("score", Utilities.formatScore(score.getScore(),
                                                                s.getSuffix()));
                   }
                   return subject;
                 }).collect(Collectors.toList());
      map.put("subject", subjects);
      return map;
    }).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void exportExcelBySemester(Integer semester, OutputStream stream) {
    if (semester % 100 == 0) {
      writeScoreByCourses(courseRepository.findBySemester(semester), stream);
    } else {
      writeSumByCourses(courseRepository.findBySemester(semester), stream);
    }
  }

  @Override
  @Transactional(readOnly = true)
  @Secured({ "ROLE_TEACHER", "ROLE_ADMIN" })
  public void exportExcelByCourse(Integer courseId, OutputStream stream) {
    writeScoreByCourses(Collections.singletonList(Utilities.fetch(() -> courseRepository.findById(courseId))),
                        stream);
  }

  @Secured({ "ROLE_ADMIN" })
  private void writeSumByCourses(List<Course> courses, OutputStream outputStream) {
    Map<Student, Triple<Course, List<Triple<String, String, String>>, String>> scoreCalculated
        = calculateScore(courses, 0);

    SXSSFWorkbook workbook = new SXSSFWorkbook(100);
    Sheet sheet = workbook.createSheet();

    Row firstRow = sheet.createRow(0);
    firstRow.createCell(0, CellType.STRING).setCellValue("学号");
    firstRow.createCell(1, CellType.STRING).setCellValue("姓名");
    firstRow.createCell(2, CellType.STRING).setCellValue("总分");
    firstRow.createCell(3, CellType.STRING).setCellValue("授课教师");
    firstRow.createCell(4, CellType.STRING).setCellValue("课程名字");

    int rowNumber = 1;
    for (Map.Entry<Student, Triple<Course, List<Triple<String, String, String>>, String>> information
        : scoreCalculated.entrySet()) {
      Row currentRow = sheet.createRow(rowNumber++);
      int cellNumber = 0;
      currentRow.createCell(cellNumber++, CellType.STRING).setCellValue(information.getKey().getStudentId());
      currentRow.createCell(cellNumber++, CellType.STRING).setCellValue(information.getKey().getName());
      currentRow.createCell(cellNumber++, CellType.STRING).setCellValue(information.getValue().getRight());
      currentRow.createCell(cellNumber++, CellType.STRING).setCellValue(information.getValue().getLeft().getName());
      currentRow.createCell(cellNumber, CellType.STRING).setCellValue(information.getValue().getLeft().getTeacher().getName());
    }

    try {
      workbook.write(outputStream);
    } catch (IOException e) {
      throw new InternalException("Error Happens", e);
    }

    workbook.dispose();
  }

  @Secured({ "ROLE_ADMIN" })
  private void writeScoreByCourses(List<Course> courses, OutputStream outputStream) {
    List<Subject> subjects = courses.get(0).getWeights()
                                    .stream().map(Weight::getSubject)
                                    .sorted(Comparator.comparing(Subject::getName))
                                    .collect(Collectors.toList());

    Map<Student, Triple<Course, List<Triple<String, String, String>>, String>> scoreCalculated =
        calculateScore(courses, 2);

//    Workbook workbook = new XSSFWorkbook();
    SXSSFWorkbook workbook = new SXSSFWorkbook(100);
    Sheet sheet = workbook.createSheet();

    Row firstRow = sheet.createRow(0);
    firstRow.createCell(0, CellType.STRING).setCellValue("学号");
    firstRow.createCell(1, CellType.STRING).setCellValue("姓名");
    for (int i = 0; i < subjects.size(); i++) {
      Cell currentCell = firstRow.createCell(i * 2 + 2, CellType.STRING);
      currentCell.setCellValue(subjects.get(i).getName());
      currentCell = firstRow.createCell(i * 2 + 3, CellType.STRING);
      currentCell.setCellValue(subjects.get(i).getName() + " 得分");
    }

    int rowNumber = 1;
    for (Map.Entry<Student, Triple<Course, List<Triple<String, String, String>>, String>> student
        : scoreCalculated.entrySet()) {
      Row currentRow = sheet.createRow(rowNumber++);
      currentRow.createCell(0, CellType.STRING).setCellValue(student.getKey().getStudentId());
      int cellNumber = 1;
      currentRow.createCell(cellNumber++, CellType.STRING).setCellValue(student.getKey().getName());
      for (Triple<String, String, String> score : student.getValue().getMiddle()) {
        currentRow.createCell(cellNumber++, CellType.STRING).setCellValue(score.getMiddle());
        currentRow.createCell(cellNumber++, CellType.STRING).setCellValue(score.getRight());
      }
      currentRow.createCell(cellNumber, CellType.STRING).setCellValue(student.getValue().getRight());
    }

    try {
      workbook.write(outputStream);
    } catch (IOException e) {
      throw new InternalException("Error Happens", e);
    }

    workbook.dispose();
  }

  private static int pow10(int n) {
    int res = 1;
    for (int i = 0; i < n; i++) {
      res *= 10;
    }
    return res;
  }
  // StudentId, Triple<Course, Triple<SubjectName, Score, ScoreCalculated>, sum>
  private Map<Student, Triple<Course, List<Triple<String, String, String>>, String>> calculateScore(List<Course> courses,
                                                                                                    int precision) {
    log.info("Start Processing");
    Map<Student, Triple<Course, List<Triple<String, String, String>>, String>> studentInformation
        = new TreeMap<>(Comparator.comparing(Student::getStudentId));
    courses.forEach((Course currentCourse) -> {
      List<Score> s = scoreRepository.findByCourse_Id(currentCourse.getId());
      Map<String, List<Score>> findByStudent = s.stream()
                                                .collect(Collectors.groupingBy(
                                                    score -> score.getStudent().getStudentId()
                                                ));
//      log.info(findByStudent);
      for (Student student : currentCourse.getStudents()) {
        List<Triple<String, String, String>> scores = new ArrayList<>();
        int sum = 0;
        int max = 10000;
        Weight bmi = null;
        int height = 0;
        int weight = 0;
        int absolute = 0;
        String special = "";
        int bmiIndex = -1;
        Map<Weight, List<Score>> findByWeight = findByStudent.getOrDefault(student.getStudentId(),
                                                                           Collections.emptyList())
                                                             .stream()
                                                             .collect(Collectors.groupingBy(Score::getWeight));
//        log.info("Process " + student.getStudentId());
        for (Weight currentWeight : currentCourse.getWeights()) {
          Subject subject = currentWeight.getSubject();
          if (subject.getName().contains("BMI")) {
            bmiIndex = scores.size();
            scores.add(new MutableTriple<>());
            bmi = currentWeight;
            continue;
          }
          Score score = findByWeight.getOrDefault(currentWeight, Collections.emptyList())
                                    .stream().findAny().orElse(null);
          MutableTriple<String, String, String> triple = new MutableTriple<>();
          triple.setLeft(subject.getName());
          triple.setRight(String.valueOf(0));
          if (score != null) {
            triple.setMiddle(Utilities.formatScore(score.getScore(),
                                                   score.getSubject().getSuffix()));
            if (subject.getName().contains("体重")) {
              weight = score.getScore();
            } else if (subject.getName().contains("身高")) {
              height = score.getScore();
            } else {
              Integer finalScore = Utilities.findScore(score.getScore(), currentWeight.getStandard().getGradings());
              triple.setRight(String.valueOf(finalScore));
              if (subject.getName().contains("晨跑次数")
                  && finalScore == -1) {
                max = 5900;
                finalScore = 0;
              } else if (subject.getName().contains("缓考")
                  && finalScore == 1) {
                special = "缓考";
                finalScore = 0;
              } else if (subject.getName().contains("旷考")
                  && finalScore == 1) {
                special = "旷考";
                finalScore = 0;
              } else if (subject.getName().contains("免测")
                  && finalScore != 0) {
                absolute = finalScore;
                finalScore = 0;
              }
              sum += finalScore * currentWeight.getWeight();
            }
          }
          scores.add(triple);
        }

        if (bmi != null) {
          if (height != 0) {
            int bmiScore = (int) (weight * (long) Math.pow(10, 6) / (height * height));
            int finalScore = Utilities.findScore(bmiScore, bmi.getStandard().getGradings());
            bmiScore = bmiScore * 100;
            Triple<String, String, String> bmiTriple = new MutableTriple<>(bmi.getSubject().getName(),
                                                                           Utilities.formatScore(bmiScore,
                                                                                                 bmi.getSubject().getSuffix()),
                                                                           String.valueOf(finalScore));
            scores.set(bmiIndex, bmiTriple);
            sum += finalScore * bmi.getWeight();
          } else {
            Triple<String, String, String> bmiTriple = new MutableTriple<>(bmi.getSubject().getName(),
                                                                           null,
                                                                           String.valueOf(0));
            scores.set(bmiIndex, bmiTriple);
          }
        }

        String totalScore;
        if (absolute != 0) {
          totalScore = String.valueOf(absolute);
        } else if (special.equals("")) {
          sum = Integer.min(sum, max);
          double doubleSum = (double) sum / 100.0;
          totalScore = new BigDecimal(doubleSum).setScale(precision, RoundingMode.HALF_UP).toString();
        } else {
          totalScore = special;
        }
        scores.sort(Comparator.comparing(Triple::getLeft));
        Triple<Course, List<Triple<String, String, String>>, String> information =
            new ImmutableTriple<>(currentCourse, scores, totalScore);

        studentInformation.put(student, information);
      }
    });
    log.info("Process Finished");
    return studentInformation;
  }
}
