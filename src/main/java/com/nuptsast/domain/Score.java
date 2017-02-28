package com.nuptsast.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;

/**
 * Created by zheng on 2016/11/16.
 * For fit-jpa.
 */
@Entity
public class Score {
  @Id
  @GeneratedValue
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Weight weight;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Course course;

  @OneToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Subject subject;

  @Column()
  private Integer score;

  public Score() {
  }

  public Score(Weight weight, Student student, Course course, Integer score, Subject subject) {
    this.weight = weight;
    this.student = student;
    this.score = score;
    this.course = course;
    this.subject = subject;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Student getStudent() {
    return student;
  }

  public void setStudent(Student student) {
    this.student = student;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
               .append(getId())
               .append(getScore())
               .toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Score score1 = (Score) o;

    return new EqualsBuilder()
               .append(getId(), score1.getId())
               .append(getScore(), score1.getScore())
               .isEquals();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("id", id)
               .append("score", score)
               .toString();
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  public Weight getWeight() {
    return weight;
  }

  public void setWeight(Weight weight) {
    this.weight = weight;
  }

  public Subject getSubject() {
    return subject;
  }

  public void setSubject(Subject subject) {
    this.subject = subject;
  }
}
