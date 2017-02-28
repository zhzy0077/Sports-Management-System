package com.nuptsast.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

/**
 * Created by zheng on 2016/11/18.
 * For fit-jpa.
 */
@Entity
public class Grading {
  @Id
  @GeneratedValue
  private Integer Id;

  @Column(nullable = false)
  private Integer lowerBound = Integer.MIN_VALUE;
  @Column(nullable = false)
  private Integer upperBound = Integer.MAX_VALUE;
  @Column(nullable = false)
  private Integer score;
  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Standard standard;

  public Grading() {
  }

  public Grading(Integer lowerBound, Integer upperBound, Integer score) {
    validate(lowerBound, upperBound);
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.score = score;
  }

  public Integer getId() {
    return Id;
  }

  public void setId(Integer id) {
    Id = id;
  }

  public Integer getLowerBound() {
    return lowerBound;
  }

  public void setLowerBound(Integer lowerBound) {
    validate(lowerBound, upperBound);
    this.lowerBound = lowerBound;
  }

  public Integer getUpperBound() {
    return upperBound;
  }

  public void setUpperBound(Integer upperBound) {
    validate(lowerBound, upperBound);
    this.upperBound = upperBound;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  private void validate(Integer lowerBound, Integer upperBound) {
    if (lowerBound >= upperBound) {
      throw new IllegalArgumentException("lowerBound must small than upperBound");
    }
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
               .append(getLowerBound())
               .append(getUpperBound())
               .append(getScore())
               .toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    Grading grading = (Grading) o;

    return new EqualsBuilder()
               .append(getLowerBound(), grading.getLowerBound())
               .append(getUpperBound(), grading.getUpperBound())
               .append(getScore(), grading.getScore())
               .isEquals();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("Id", Id)
               .append("lowerBound", lowerBound)
               .append("upperBound", upperBound)
               .append("score", score)
               .toString();
  }

  public Standard getStandard() {
    return standard;
  }

  public void setStandard(Standard standard) {
    this.standard = standard;
  }
}
