/**
 * Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */
package test.validate;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import test.constants.RelativePathConstants;

/**
 * Created by gaurav.gandhi on 24-10-2018.
 */
public class HtmlValidator extends AbstractValidator {

  HtmlValidator(File fileToValidate) {
    this.fileToValidate = fileToValidate;
  }

  @Override
  public boolean validate() throws IOException {

    String baseUri =
        RelativePathConstants.PROTOCOL_FILE
            + RelativePathConstants.COLON
            + fileToValidate.getPath();

    Document doc = Jsoup.parse(fileToValidate, RelativePathConstants.ENCODING_UTF_8, baseUri);
    boolean isValidHtml = true;

    if (isQueryInvalid(doc, RelativePathConstants.QUERY_HREF,
        RelativePathConstants.ATTRIBUTE_HREF)) {
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
}
