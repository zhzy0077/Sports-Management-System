package com.nuptsast.service;

import com.nuptsast.domain.Grading;
import com.nuptsast.domain.Standard;
import com.nuptsast.domain.Subject;
import com.nuptsast.exception.NotFoundException;
import com.nuptsast.repository.GradingRepository;
import com.nuptsast.repository.StandardRepository;
import com.nuptsast.repository.SubjectRepository;
import com.nuptsast.util.Utilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zheng on 2016/11/19.
 * For fit-jpa.
 */
@Service
public class SubjectServiceImpl implements SubjectService {

  private final SubjectRepository subjectRepository;
  private final GradingRepository gradingRepository;
  private final StandardRepository standardRepository;

  private final Log log = LogFactory.getLog(this.getClass());

  @Autowired
  public SubjectServiceImpl(GradingRepository gradingRepository, SubjectRepository subjectRepository, StandardRepository standardRepository) {
    this.gradingRepository = gradingRepository;
    this.subjectRepository = subjectRepository;
    this.standardRepository = standardRepository;
  }


  @Override
  public Subject fetch(Integer subjectId) {
    return subjectRepository.findById(subjectId)
                            .orElseThrow(NotFoundException::new);
  }

  @Override
  @Secured("ROLE_ADMIN")
  public Subject addSubject(Subject subject) {
    return subjectRepository.save(subject);
  }

  @Override
  @Secured("ROLE_ADMIN")
  public List<Subject> allSubject() {
    return subjectRepository.findAll();
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void updateSubject(Subject subject) {
    subjectRepository.findById(subject.getId())
                     .ifPresent(sub -> Utilities.copyProperties(subject, sub));
  }

  @Override
  @Secured("ROLE_ADMIN")
  public void deleteSubject(Integer subjectId) {
    subjectRepository.delete(subjectId);
  }

  @Override
  @Secured("ROLE_ADMIN")
  public Collection<Standard> fetchStandards(Integer subjectId) {
    Subject subject = subjectRepository.findById(subjectId)
                                       .orElseThrow(NotFoundException::new);
    log.info(subject.getStandards());
    return subject.getStandards();
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void addStandard(Integer subjectId, Collection<Grading> gradings, String name) {
    Subject subject = subjectRepository.findById(subjectId)
                                       .orElseThrow(NotFoundException::new);
    gradings.forEach(grading -> {
      Grading g = gradingRepository.save(grading);
      grading.setId(g.getId());
    });
    Standard standard = new Standard(gradings, name);
    standard.setSubject(subject);
    standard = standardRepository.save(standard);
    subject.getStandards().add(standard);
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void deleteStandard(Integer subjectId, Integer standardId) {
    Subject subject = subjectRepository.findById(subjectId)
                                       .orElseThrow(NotFoundException::new);
    standardRepository.delete(subject.getStandards().stream()
                                     .filter(standard -> standard.getId().equals(standardId))
                                     .findAny().orElseThrow(IllegalArgumentException::new));

  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public Collection<Grading> fetchGradings(Integer subjectId, Integer standardId) {
    Standard standard = standardRepository.findById(standardId).orElseThrow(NotFoundException::new);
//    log.info(standardRepository.findAll());
    return standard.getGradings()
                   .stream()
                   .sorted(Comparator.comparing(Grading::getScore))
                   .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @Secured("ROLE_ADMIN")
  public void putGradings(Integer subjectId, Integer standardId, Collection<Grading> gradings) {
    Standard standard = standardRepository.findById(standardId).orElseThrow(NotFoundException::new);
    standard.getGradings().forEach(gradingRepository::delete);
    standard.setGradings(gradings.stream()
                                 .map(grading -> {
                                   grading.setStandard(standard);
                                   return grading;
                                 })
                                 .map(gradingRepository::save)
                                 .peek(log::info)
                                 .collect(Collectors.toList()));
  }
}
