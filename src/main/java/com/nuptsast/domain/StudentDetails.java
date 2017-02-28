package com.nuptsast.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by zheng on 2016/11/20.
 * For fit-jpa.
 */
public class StudentDetails implements UserDetails {
  private final SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ONLY");
  private Student student;

  public StudentDetails(Student student) {
    this.student = student;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Arrays.asList(new SimpleGrantedAuthority("ROLE_" + student.getStudentId()), authority);
  }

  @Override
  public String getPassword() {
    return student.getPassword();
  }

  @Override
  public String getUsername() {
    return student.getStudentId();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
