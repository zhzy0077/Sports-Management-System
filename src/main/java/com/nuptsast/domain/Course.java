package com.nuptsast.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by zheng on 2016/11/16.
 * For fit-jpa.
 */
@Entity
public class Course {
  @Id
  @GeneratedValue
  private Integer id;

  @Column(nullable = false, length = 127)
  private String name;
  @Column(nullable = false)
  private Integer semester;

  @ManyToOne(fetch = FetchType.EAGER)
  private Teacher teacher;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "course", cascade = CascadeType.REMOVE)
  @JsonIgnore
  private Collection<Weight> weights = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "courses")
  @JsonIgnore
  private Collection<Student> students = new HashSet<>();


  public Course() {
  }

  public Course(String name, Integer semester) {
    this.name = name;
    this.semester = semester;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getSemester() {
    return semester;
  }

  public void setSemester(Integer semester) {
    this.semester = semester;
  }

  public Teacher getTeacher() {
    return teacher;
  }

  public void setTeacher(Teacher teacher) {
    this.teacher = teacher;
  }

  public Collection<Weight> getWeights() {
    return weights;
  }

  public void setWeights(Collection<Weight> weights) {
    this.weights = weights;
  }

  public Collection<Student> getStudents() {
    return students;
  }

  public void setStudents(Collection<Student> students) {
    this.students = students;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
               .append(getId())
               .append(getName())
               .append(getSemester())
               .append(getTeacher())
               .toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Course course = (Course) o;

    return new EqualsBuilder()
               .append(getId(), course.getId())
               .append(getName(), course.getName())
               .append(getSemester(), course.getSemester())
               .append(getTeacher(), course.getTeacher())
               .isEquals();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("id", id)
               .append("name", name)
               .append("semester", semester)
               .append("teacher", teacher)
               .toString();
  }
}
