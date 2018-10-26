/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */
package com.tpt.release.testdata.validate;

import com.tpt.release.testdata.constants.RelativePathConstants;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by gaurav.gandhi on 24-10-2018.
 */
public class HtmlValidator extends AbstractValidator {

  HtmlValidator(File fileToValidate) {
    this.fileToValidate = fileToValidate;
  }

  @Override
  public List<String> validate() throws IOException {

    String baseUri =
        RelativePathConstants.PROTOCOL_FILE
            + RelativePathConstants.COLON
            + fileToValidate.getPath();

    Document doc = Jsoup.parse(fileToValidate, RelativePathConstants.ENCODING_UTF_8, baseUri);

    checkQuery(doc, RelativePathConstants.QUERY_HREF, RelativePathConstants.ATTRIBUTE_HREF);
    checkQuery(doc, RelativePathConstants.QUERY_SRC, RelativePathConstants.ATTRIBUTE_SRC);

    Elements columnTags = doc.select("td");

    for (Element columnTag : columnTags) {

      String line = columnTag.toString();

      if (line.contains(RelativePathConstants.SINGLE_DOT_APPENDER)
          && (line.contains(".robot") || line.contains(".txt"))) {

        String absPath = getAbsolutePath(line.replace("<td>", "").replace("</td>", "").trim());

        File resourceFile = new File(absPath);
        if (!resourceFile.exists() && !resourceFile.getParentFile().exists()) {
          appendErrorMessage("html td", absPath, columnTag.attr("id"));
        }
      }
    }
    return errorMessages;
  }

  private void checkQuery(Document doc, String query, String attribute) {

    Elements links = doc.select(query);

    for (Element link : links) {

      if (link.attr(attribute).startsWith(RelativePathConstants.PROTOCOL_HTTP)) {
        continue;
      }
      String absPath = link.absUrl(attribute).replace("file:/", "").replace("file:", "");

      if (absPath.equals("")) {
        continue;
      }

      File resourceFile = new File(absPath);

      if (!resourceFile.exists() && !resourceFile.getParentFile().exists()) {
        appendErrorMessage("Invalid " + attribute + " query. ", absPath, link.attr("id"));
      }
    }
  }
}
