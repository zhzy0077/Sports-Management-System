package com.nuptsast.config;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zheng on 2016/11/20.
 * For fit-jpa.
 */
@Configuration
public class RootConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new StandardPasswordEncoder();
  }
  @Bean
  @Profile("dev")
  public MBeanExporter jmxExporter(HibernateStatisticsFactoryBean factoryBean) throws Exception {
    MBeanExporter mBeanExporter = new MBeanExporter();
    Map<String, Object> map = new HashMap<>();
    map.put("Hibernate:application=Statistics", factoryBean.getObject());
    mBeanExporter.setBeans(map);
    return mBeanExporter;
  }
}

@Component
@Profile("dev")
class HibernateStatisticsFactoryBean implements FactoryBean<Statistics> {

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  @Override
  public Statistics getObject() throws Exception {
    SessionFactory sessionFactory = ((HibernateEntityManagerFactory) entityManagerFactory).getSessionFactory();
    return sessionFactory.getStatistics();
  }

  @Override
  public Class<?> getObjectType() {
    return Statistics.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}