package com.nuptsast.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by zheng on 2016/11/16.
 * For fit-jpa.
 */
@Entity
public class Teacher {
  @Id
  @Column(nullable = false, length = 31)
  private String employeeId;
  @Column(nullable = false, length = 15)
  private String name;
  @Column(nullable = false, length = 127)
  @JsonIgnore
  private String password;
  @Column(nullable = false)
  private Integer authority;
  @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
  @JsonIgnore
  private Collection<Course> teachCourse = new HashSet<>();


  public Teacher() {

  }

  public Teacher(String employeeId, String name, Integer authority) {
    this.employeeId = employeeId;
    this.name = name;
    this.authority = authority;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Integer getAuthority() {
    return authority;
  }

  public void setAuthority(Integer authority) {
    this.authority = authority;
  }

  public Collection<Course> getTeachCourse() {
    return teachCourse;
  }

  public void setTeachCourse(Collection<Course> teachCourse) {
    this.teachCourse = teachCourse;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
               .append(getEmployeeId())
               .append(getName())
               .append(getAuthority())
               .toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Teacher teacher = (Teacher) o;

    return new EqualsBuilder()
               .append(getEmployeeId(), teacher.getEmployeeId())
               .append(getName(), teacher.getName())
               .append(getAuthority(), teacher.getAuthority())
               .isEquals();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("employeeId", employeeId)
               .append("name", name)
               .append("password", password)
               .append("authority", authority)
               .toString();
  }
}

