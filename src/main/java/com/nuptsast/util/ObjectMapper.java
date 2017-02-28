package com.nuptsast.util;

import java.util.List;

/**
 * Created by zheng on 2016/11/22.
 * For fit-jpa.
 */
public interface ObjectMapper<T> {
  T map(List<String> row);
}
