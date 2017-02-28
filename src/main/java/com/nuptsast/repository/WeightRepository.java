package com.nuptsast.repository;

import com.nuptsast.domain.Subject;
import com.nuptsast.domain.Weight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by zheng on 2016/11/18.
 * For fit-jpa.
 */
public interface WeightRepository extends JpaRepository<Weight, Integer> {
  Optional<Weight> findBySubject(Subject subject);

  Optional<Weight> findById(Integer id);
}
