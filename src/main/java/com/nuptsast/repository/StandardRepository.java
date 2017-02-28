package com.nuptsast.repository;

import com.nuptsast.domain.Standard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by zheng on 2016/11/23.
 * For fit-jpa.
 */
public interface StandardRepository extends JpaRepository<Standard, Integer> {
  Optional<Standard> findById(Integer id);
  Optional<Standard> findByName(String name);
}
