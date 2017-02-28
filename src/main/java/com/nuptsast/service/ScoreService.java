package com.nuptsast.service;

import java.io.File;
import java.util.Map;

/**
 * Created by zheng on 2016/11/19.
 * For fit-jpa.
 */
public interface ScoreService {
  void setScore(String studentId,
                Integer courseId,
                Integer weightId,
                String score);
  void uploadScore(File file, Integer courseId);

  Map<String, Map<String, String>> getScore(String studentId, Integer courseId);

  Map<String, Map<String, String>> getScoreBySemester(String studentId, Integer semester);

  void deleteScore(String studentId, Integer id, Integer weightId);
}
