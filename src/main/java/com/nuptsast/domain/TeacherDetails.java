package com.nuptsast.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by zheng on 2016/11/20.
 * For fit-jpa.
 */
public class TeacherDetails implements UserDetails {
  private final Teacher teacher;
  private final List<SimpleGrantedAuthority> authorities = Collections.unmodifiableList(
      Arrays.asList(
          new SimpleGrantedAuthority("ROLE_ONLY"),
          new SimpleGrantedAuthority("ROLE_ALL"),
          new SimpleGrantedAuthority("ROLE_TEACHER"),
          new SimpleGrantedAuthority("ROLE_ADMIN")
      )
  );

  public TeacherDetails(Teacher teacher) {
    this.teacher = teacher;
  }


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authority = authorities.subList(0, teacher.getAuthority());
//    new SimpleGrantedAuthority("ROLE_" + teacher.getEmployeeId()));
    return authority;
  }

  @Override
  public String getPassword() {
    return teacher.getPassword();
  }

  @Override
  public String getUsername() {
    return String.valueOf(teacher.getEmployeeId());
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
