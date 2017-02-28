package com.nuptsast.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zheng on 2016/11/18.
 * For fit-jpa.
 */
@Entity
public class Weight {
  @Id
  @GeneratedValue
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER)
  private Subject subject;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Course course;

  @OneToOne(fetch = FetchType.EAGER)
  private Standard standard;

  @Column(nullable = false)
  private Integer weight;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "weight", cascade = { CascadeType.REMOVE })
  private List<Score> scores = new ArrayList<>();

  public Weight() {
  }

  public Weight(Subject subject, Course course, Integer weight) {
    this.subject = subject;
    this.course = course;
    this.weight = weight;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Subject getSubject() {
    return subject;
  }

  public void setSubject(Subject subject) {
    this.subject = subject;
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  public Integer getWeight() {
    return weight;
  }

  public void setWeight(Integer weight) {
    this.weight = weight;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(getId())
        .toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Weight weight = (Weight) o;

    return new EqualsBuilder()
        .append(getId(), weight.getId())
        .isEquals();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("subject", subject)
        .append("course", course)
        .append("weight", weight)
        .toString();
  }

  public Standard getStandard() {
    return standard;
  }

  public void setStandard(Standard standard) {
    this.standard = standard;
  }
}
