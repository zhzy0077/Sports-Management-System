package com.nuptsast;

import com.nuptsast.domain.Teacher;
import com.nuptsast.repository.TeacherRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;

@EnableAsync
@SpringBootApplication
public class FitJpaApplication {

  public static void main(String[] args) {
    SpringApplication.run(FitJpaApplication.class, args);
  }

  @Bean
  @Profile("test")
  public CommandLineRunner setRoot(TeacherRepository teacherRepository, PasswordEncoder encoder) {
    return (args) -> {
      Teacher teacher = new Teacher("12345678", "root", 4);
//      System.out.println(encoder.encode("root"));
//      System.in.read();
      teacher.setPassword(encoder.encode("root"));
      teacherRepository.save(teacher);
    };
  }
  @Bean
  @Profile( "dev" )
  public CommandLineRunner addFile() {
    return (args) -> {
      try {
        Files.createFile(Paths.get("score.xlsx"));
      } catch (FileAlreadyExistsException ignore) {

      }
    };
  }
}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorsFilter implements Filter {
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletResponse response = (HttpServletResponse) res;
    HttpServletRequest request = (HttpServletRequest) req;
    response.setHeader("Access-Control-Allow-Origin", "http://180.209.64.5");
    response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    response.setHeader("Access-Control-Max-Age", "3600");
    if (!Objects.equals(request.getMethod(), "OPTIONS")) {
      chain.doFilter(req, res);
    }

  }

  public void init(FilterConfig filterConfig) {}

  public void destroy() {}

}

@Component
class ChangeFilter implements Filter {
  private static final String[] DISALLOW_METHOD =
      new String[] {"PUT", "DELETE"};

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    try (InputStream access = new FileInputStream("access.txt")) {
      int permit = access.read();
      if (permit == '1') {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String method = request.getMethod();
        for (String disallow : DISALLOW_METHOD) {
          if (disallow.equals(method)) {
            response.sendError(403);
            return; 
          }
        }
      }
      chain.doFilter(req, res);
    }
  }

  public void init(FilterConfig filterConfig) {

  }

  public void destroy() {}

}
