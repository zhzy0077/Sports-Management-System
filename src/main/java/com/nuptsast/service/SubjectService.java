package com.nuptsast.service;

import com.nuptsast.domain.Grading;
import com.nuptsast.domain.Standard;
import com.nuptsast.domain.Subject;

import java.util.Collection;
import java.util.List;

/**
 * Created by zheng on 2016/11/18.
 * For fit-jpa.
 */
public interface SubjectService {
  Subject fetch(Integer subjectId);

  Subject addSubject(Subject subject);

  List<Subject> allSubject();

  void updateSubject(Subject subject);

  void deleteSubject(Integer subjectId);

  Collection<Standard> fetchStandards(Integer subjectId);

  void addStandard(Integer subjectId, Collection<Grading> gradings, String name);

  void deleteStandard(Integer subjectId, Integer standardId);

  Collection<Grading> fetchGradings(Integer subjectId, Integer standardId);

  void putGradings(Integer subjectId, Integer standardId, Collection<Grading> gradings);
}
