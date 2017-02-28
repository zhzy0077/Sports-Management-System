package com.nuptsast.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by zheng on 2016/11/23.
 * For fit-jpa.
 */
@Entity
public class Standard {
  @Id
  @GeneratedValue
  private Integer id;

  @OneToMany(mappedBy = "standard", fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE, CascadeType.PERSIST })
  @JsonIgnore
  private Collection<Grading> gradings = new HashSet<>();

  @Column(nullable = false, length = 15)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Subject subject;

  public Standard() {
  }


  public Standard(Collection<Grading> gradings, String name) {
    this.gradings = gradings;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Collection<Grading> getGradings() {
    return gradings;
  }

  public void setGradings(Collection<Grading> gradings) {
    this.gradings = gradings;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("id", id)
               .append("gradings", gradings)
               .append("name", name)
               .toString();
  }

  public Subject getSubject() {
    return subject;
  }

  public void setSubject(Subject subject) {
    this.subject = subject;
  }
}
