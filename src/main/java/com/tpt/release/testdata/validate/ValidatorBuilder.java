/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
*/

package com.tpt.release.testdata.validate;

import com.tpt.release.testdata.TestDataReleasePrepare;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FilenameUtils;


/**
 * Created by gaurav.gandhi on 22-10-2018.
 */
public class ValidatorBuilder {

  public static void main(String[] args) throws Exception {

    File updatesDirectory = new File(
//        "D:\\Git1\\r\\r\\tpt-cxl-robot-tests\\src\\test\\robotframework\\testdata\\reference_data\\Updates\\Swap Trade with negative market price when flown to HA");
        "D:\\Git1\\r\\r\\tpt-cxl-robot-tests\\src\\test\\robotframework\\testdata\\reference_data\\Released\\Pre 8.15");

    Iterator<File> files = TestDataReleasePrepare
        .getFiles(updatesDirectory, new String[]{"html", "txt", "robot"});

    while (files.hasNext()) {

      File file = files.next();
      List<String> errorMessages = getValidator(file).validate();
      if (errorMessages.isEmpty()) {
        continue;
      }

      StringBuilder builder = new StringBuilder(file.getPath() + "\n");
      boolean isValidErrorMessage = false;
      for (String errorMessage : errorMessages) {
        if (errorMessage.contains("acceptance.css")
            || errorMessage.contains("test_data_script.js")
            || errorMessage.contains("upDownImage.gif")) {
          continue;
        }
        isValidErrorMessage = true;
        builder.append(errorMessage + "\n");
      }
      builder.append("\n");
      if (isValidErrorMessage) {
        System.out.println(builder);
      }
    }
  }

  private ValidatorBuilder() {
  }

  public static ValidatorI getValidator(File fileToValidate) {

    String extension = FilenameUtils.getExtension(fileToValidate.getName());

    if ("html".equalsIgnoreCase(extension)) {
      return new HtmlValidator(fileToValidate);
    } else {
      return new TextValidator(fileToValidate);
    }
  }
}
