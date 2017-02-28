package com.nuptsast.repository;

import com.nuptsast.domain.Grading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Created by zheng on 2016/11/18.
 * For fit-jpa.
 */
public interface GradingRepository extends JpaRepository<Grading, Integer> {
  @Query("select g from Grading g where :score >= g.lowerBound " +
             "and :score < g.upperBound " +
             "and g.standard.id = :standardId")
  Optional<Grading> findByScore(@Param("score") Integer score, @Param("standardId") Integer standardId);
}
