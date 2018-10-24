package test;/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */

import java.util.StringJoiner;

/**
 * Created by gaurav.gandhi on 24-10-2018.
 */
class RelativePathConstants {

  public static final String SINGLE_DOT_APPENDER = "./";

  public static final String DOUBLE_DOT_APPENDER = "../";

  //   value = "./
  public static final String SINGLE_DOT_APPENDER_WITH_PREFIX =
      prefixDoubleQuote(SINGLE_DOT_APPENDER);

  //   value = "../
  public static final String DOUBLE_DOT_APPENDER_WITH_PREFIX =
      prefixDoubleQuote(DOUBLE_DOT_APPENDER);

  public static final String PROTOCOL_HTTP = "http";

  public static final String PROTOCOL_FILE = "file";

  public static final String ATTRIBUTE_HREF = "href";

  public static final String ATTRIBUTE_SRC = "src";

  public static final String QUERY_HREF =
      new StringJoiner("", "[", "]").add(ATTRIBUTE_HREF).toString();

  public static final String QUERY_SRC =
      new StringJoiner("", "[", "]").add(ATTRIBUTE_SRC).toString();

  public static final String ENCODING_UTF_8 = "UTF-8";

  public static final String COLON = ":";
  public static final String DOT = ".";

  private static String prefixDoubleQuote(String str) {
    return addPrefix(str, "\"");
  }

  private static String addPrefix(String str, String prefix) {
    return prefix + str;
  }

  @SuppressWarnings("unused")
  private static String addSuffix(String str, String suffix) {
    return str + suffix;
  }
}