package com.nuptsast.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuptsast.domain.*;
import com.nuptsast.exception.InternalException;
import com.nuptsast.exception.NotFoundException;
import com.nuptsast.repository.GradingRepository;
import com.nuptsast.repository.ScoreRepository;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by zheng on 2016/11/16.
 * For fit-jpa.
 */
public class Utilities {
  private static final DataFormatter formatter = new DataFormatter();

  public static String formatScore(Integer score, Integer suffix) {
    StringBuilder sb = new StringBuilder(String.valueOf(score));
    if (suffix != 0) {
      while (sb.length() - suffix <= 0) {
        sb.insert(0, "0");
      }
      sb.insert(sb.length() - suffix, ".");
    }
    return sb.toString();
  }



  public static Integer findScore(Integer score, Collection<Grading> gradings) {
    return gradings.stream().filter(grading -> grading.getLowerBound() <= score && grading.getUpperBound() > score)
                   .findAny().map(Grading::getScore).orElse(0);
  }

  private static String[] getNullPropertyNames(Object source) {
    final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
    return Stream.of(wrappedSource.getPropertyDescriptors())
                 .map(FeatureDescriptor::getName)
                 .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                 .toArray(String[]::new);
  }

  public static void copyProperties(Object src, Object target) {
    BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
  }

  public static <T> T fetch(Supplier<? extends Optional<T>> supplier) {
    return supplier.get().orElseThrow(NotFoundException::new);
  }

  public static String generateJson(Map<String, Map<String, String>> score) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(score);
    } catch (JsonProcessingException e) {
      e.printStackTrace(); // won't happen
    }
    return null;
  }

  public static <T> List<T> readExcel(InputStream stream, com.nuptsast.util.ObjectMapper<? extends T> mapper) throws
      IOException, InvalidFormatException {
    Workbook workbook = WorkbookFactory.create(stream);
    Sheet workSheet = workbook.getSheetAt(0);
    return StreamSupport.stream(workSheet.spliterator(), false)
                        .skip(1) // column
                        .map(row -> StreamSupport.stream(row.spliterator(), false)
                                                 .map(formatter::formatCellValue)
                                                 .collect(Collectors.toList())
                        ).map(mapper::map)
                        .collect(Collectors.toList());
  }

  public static void writeExcel(OutputStream stream, List<String> header, List<String> firstColumn) throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet workSheet = workbook.createSheet();
    Row row = workSheet.createRow(0);
    for (int i = 0; i < header.size(); i++) {
      Cell cell = row.createCell(i);
      cell.setCellType(CellType.STRING);
      cell.setCellValue(header.get(i));
    }
    for (int i = 0; i < firstColumn.size(); i++) {
      Row r = workSheet.createRow(i + 1);
      Cell cell = r.createCell(0, CellType.STRING);
      cell.setCellValue(firstColumn.get(i));
    }
    workbook.write(stream);
  }
}
