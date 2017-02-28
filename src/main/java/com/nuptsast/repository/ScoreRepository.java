package com.nuptsast.repository;

import com.nuptsast.domain.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.method.P;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
public interface ScoreRepository extends JpaRepository<Score, Integer> {
  @Query("select s from Score s where s.student.studentId = :studentId and s.course.id = :courseId and s.weight.id = " +
      ":weightId")
  Score findByStudent_StudentIdAndCourse_IdAndWeight_Id(@Param("studentId") String studentId,
                                                        @Param("courseId") Integer courseId,
                                                        @Param("weightId") Integer weightId);

  @Async
  @Query("select s from Score s where s.course.id = :courseId and s.student.studentId = :studentId")
  Future<List<Score>> findByStudent_StudentIdAndCourse_Id(@Param("studentId") String studentId,
                                                          @Param("courseId") Integer courseId);

  @Async
  @Query("select s from Score s where s.course.semester = :semester and s.student.studentId = :studentId")
  Future<List<Score>> findByStudentIdAndSemester(@Param("studentId") String studentId,
                                                 @Param("semester") Integer semester);

  @Query("select s from Score s where s.course.semester = :semester")
  List<Score> findByCourseSemester(@Param("semester") Integer semester);

  @Query("select s from Score s where s.course.id = :courseId")
  List<Score> findByCourse_Id(@Param("courseId") Integer courseId);

}
