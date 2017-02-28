package com.nuptsast.repository;

import com.nuptsast.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
  Optional<Subject> findById(Integer id);

  Optional<Subject> findByName(String name);
}
