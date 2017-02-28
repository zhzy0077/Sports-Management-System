package com.nuptsast.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by zheng on 2016/11/16.
 * For fit-jpa.
 */
@Entity
@BatchSize(size = 50)
public class Student {
  @Id
  @Column(nullable = false, length = 10, unique = true)
  private String studentId;
  @Column(nullable = false, length = 127)
  @JsonIgnore
  private String password;
  @Column(nullable = false, length = 127)
  private String name;
  @Column(nullable = false, length = 30)
  private String classId;
  @Column(nullable = false)
  private Boolean gender;
  @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
  @JsonIgnore
  @LazyCollection(LazyCollectionOption.EXTRA)
  private Collection<Score> scores = new HashSet<>();
  @ManyToMany(fetch = FetchType.LAZY)
  @JsonIgnore
  @LazyCollection(LazyCollectionOption.EXTRA)
  private Collection<Course> courses = new HashSet<>();

  public Student() {
  }

  public Student(String studentId, String name, String classId, Boolean gender) {
    this.studentId = studentId;
    this.name = name;
    this.classId = classId;
    this.gender = gender;
  }

  public String getStudentId() {
    return studentId;
  }

  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClassId() {
    return classId;
  }

  public void setClassId(String classId) {
    this.classId = classId;
  }

  public Boolean getGender() {
    return gender;
  }

  public void setGender(Boolean gender) {
    this.gender = gender;
  }

  public Collection<Score> getScores() {
    return scores;
  }

  public void setScores(Collection<Score> scores) {
    this.scores = scores;
  }

  public Collection<Course> getCourses() {
    return courses;
  }

  public void setCourses(Collection<Course> courses) {
    this.courses = courses;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
               .append(getStudentId())
               .append(getName())
               .append(getClassId())
               .append(getGender())
               .toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Student student = (Student) o;

    return new EqualsBuilder()
               .append(getStudentId(), student.getStudentId())
               .append(getName(), student.getName())
               .append(getClassId(), student.getClassId())
               .append(gender, student.getGender())
               .isEquals();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("studentId", studentId)
               .append("password", password)
               .append("name", name)
               .append("classId", classId)
               .append("gender", gender)
               .toString();
  }

}
