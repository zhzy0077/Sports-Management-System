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
@BatchSize(size = 20)
public class Subject {
  @Id
  @GeneratedValue
  private Integer id;

  @Column(nullable = false, length = 15, unique = true)
  private String name;
  @Column(nullable = false)
  private Integer suffix;
  @Column(nullable = false)
  private Integer max = Integer.MAX_VALUE;
  @Column(nullable = false)
  private Integer min = Integer.MIN_VALUE;

  @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JsonIgnore
  private Collection<Standard> standards = new HashSet<>();

  public Subject() {
  }

  public Subject(String name, Integer suffix, Integer max, Integer min) {
    this.name = name;
    this.suffix = suffix;
    this.max = max;
    this.min = min;
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

  public Integer getSuffix() {
    return suffix;
  }

  public void setSuffix(Integer suffix) {
    this.suffix = suffix;
  }

  public Integer getMax() {
    return max;
  }

  public void setMax(Integer max) {
    this.max = max;
  }

  public Integer getMin() {
    return min;
  }

  public void setMin(Integer min) {
    this.min = min;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
               .append(getName())
               .append(getSuffix())
               .append(getMax())
               .append(getMin())
               .toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Subject subject = (Subject) o;

    return new EqualsBuilder()
               .append(getName(), subject.getName())
               .append(getSuffix(), subject.getSuffix())
               .append(getMax(), subject.getMax())
               .append(getMin(), subject.getMin())
               .isEquals();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("id", id)
               .append("name", name)
               .append("suffix", suffix)
               .append("max", max)
               .append("min", min)
               .toString();
  }

  public Collection<Standard> getStandards() {
    return standards;
  }

  public void setStandards(Collection<Standard> standards) {
    this.standards = standards;
  }
}

