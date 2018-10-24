package test;/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * Created by gaurav.gandhi on 22-10-2018.
 */
class ValidateRelativePath {

  private final File fileToValidate;

  public ValidateRelativePath(File fileToValidate) {
    this.fileToValidate = fileToValidate;
  }

  private boolean validateHtml() throws IOException {

    String baseUri = RelativePathConstants.PROTOCOL_FILE + RelativePathConstants.COLON + fileToValidate.getPath();

    Document doc = Jsoup.parse(fileToValidate, RelativePathConstants.ENCODING_UTF_8, baseUri);
    boolean isValidHtml = true;

    if (isQueryInvalid(doc, RelativePathConstants.QUERY_HREF, RelativePathConstants.ATTRIBUTE_HREF)) {
      isValidHtml = false;
    }

    if (isQueryInvalid(doc, RelativePathConstants.QUERY_SRC, RelativePathConstants.ATTRIBUTE_SRC)) {
      isValidHtml = false;
    }

    Elements columnTags = doc.select("td");

    for (Element columnTag : columnTags) {

      String line = columnTag.toString();

      if (line.contains(RelativePathConstants.SINGLE_DOT_APPENDER)
          && (line.contains(".robot") || line.contains(".txt"))) {

        String absPath = getAbsolutePath(line.replace("<td>", "").replace("</td>", "").trim());

        File resourceFile = new File(absPath);
        if (!resourceFile.exists() && !resourceFile.getParentFile().exists()) {
          printMessage("html td", absPath, columnTag.attr("id"));
          isValidHtml = false;
        }
      }
    }
    return isValidHtml;
  }

  private boolean isQueryInvalid(Document doc, String query, String attribute) {

    Elements links = doc.select(query);

    for (Element link : links) {

      if (link.attr(attribute).startsWith(RelativePathConstants.PROTOCOL_HTTP)) {
        continue;
      }
      String absPath = link.absUrl(attribute).replace("file:/", "");

      File resourceFile = new File(absPath);

      if (!resourceFile.exists() && !resourceFile.getParentFile().exists()) {
        printMessage("Invalid " + attribute + " query. ", absPath, link.attr("id"));
        return true;
      }
    }
    return false;
  }

  private void printMessage(String str, String absPath, String attributeId) {
    System.err.println(
        "Error: " + str + fileToValidate.getName() + " --> " + absPath + "-->" + attributeId);
  }

  private boolean validateText() throws IOException {

    List<String> lines = FileUtils.readLines(fileToValidate, RelativePathConstants.ENCODING_UTF_8);
    boolean flag = true;
    for (String line : lines) {
      if (line.contains(RelativePathConstants.SINGLE_DOT_APPENDER) || line.toLowerCase().contains("resource")) {
        String resourcePath = getAbsolutePath(line);
        File resourceFile = new File(resourcePath);
        if (!resourceFile.exists() && !resourceFile.getParentFile().exists()) {
          printMessage("Text file", resourcePath, null);
          flag = false;
        }
      }
    }
    return flag;
  }

  private String getAbsolutePath(String line) {

    int parentIndex = line.indexOf(RelativePathConstants.DOUBLE_DOT_APPENDER);
    int index = parentIndex;
    File parentFile = fileToValidate.getParentFile();

    while (parentIndex != -1) {
      parentFile = parentFile.getParentFile();
      index = parentIndex;
      parentIndex = line.indexOf(RelativePathConstants.DOUBLE_DOT_APPENDER, parentIndex + 1);
    }

    parentIndex = index + RelativePathConstants.DOUBLE_DOT_APPENDER.length();

    return parentFile.getPath() + File.separator + line.substring(parentIndex);
  }

  public boolean validate() throws IOException {

    String extension = FilenameUtils.getExtension(fileToValidate.getName());

    if ("html".equalsIgnoreCase(extension)) {
      return validateHtml();
    } else {
      return validateText();
    }
  }
}
