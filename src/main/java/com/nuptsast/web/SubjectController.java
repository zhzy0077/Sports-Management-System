package com.nuptsast.web;

import com.nuptsast.domain.Grading;
import com.nuptsast.domain.Standard;
import com.nuptsast.domain.Subject;
import com.nuptsast.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by zheng on 2016/11/19.
 * For fit-jpa.
 */
@RestController
public class SubjectController {
  private final SubjectService subjectService;

  @Autowired
  public SubjectController(SubjectService subjectService) {
    this.subjectService = subjectService;
  }

  @RequestMapping(value = "/subject", method = POST)
  public ResponseEntity<?> addSubject(@RequestBody Subject subject, UriComponentsBuilder uriBuilder) {
    subject = subjectService.addSubject(subject);
    return ResponseEntity.created(uriBuilder.path("/subject/{id}")
                                            .buildAndExpand(subject.getId())
                                            .toUri())
                         .build();
  }

  @RequestMapping(value = "/subject", method = GET)
  public List<Subject> allSubject() {
    return subjectService.allSubject();
  }

  @RequestMapping(value = "/subject/{id}", method = GET)
  public Subject getSubject(@PathVariable Integer id) {
    return subjectService.fetch(id);
  }

  @RequestMapping(value = "/subject/{id}", method = PUT)
  public ResponseEntity<?> updateSubject(@PathVariable Integer id, @RequestBody Subject subject) {
    subject.setId(id);
    subjectService.updateSubject(subject);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/subject/{id}", method = DELETE)
  public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {
    subjectService.deleteSubject(id);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/subject/{id}/standard", method = GET)
  public Collection<Standard> getStandard(@PathVariable Integer id) {
    return subjectService.fetchStandards(id);
  }

  @RequestMapping(value = "/subject/{id}/standard", method = POST)
  public ResponseEntity<?> addStandard(@PathVariable Integer id, @RequestBody Standard standard) {
    subjectService.addStandard(id, standard.getGradings(), standard.getName());
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/subject/{id}/standard/{standardId}", method = DELETE)
  public ResponseEntity<?> deleteStandard(@PathVariable Integer id,
                                          @PathVariable Integer standardId) {
    subjectService.deleteStandard(id, standardId);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/subject/{id}/standard/{standardId}/grading", method = GET)
  public Collection<Grading> getGrading(@PathVariable Integer id,
                                        @PathVariable Integer standardId) {
    return subjectService.fetchGradings(id, standardId);
  }

  @RequestMapping(value = "/subject/{id}/standard/{standardId}/grading", method = PUT)
  public ResponseEntity<?> setGrading(@PathVariable Integer id,
                                      @PathVariable Integer standardId,
                                      @RequestBody Collection<Grading> gradings) {
    subjectService.putGradings(id, standardId, gradings);
    return ResponseEntity.noContent().build();
  }
}
