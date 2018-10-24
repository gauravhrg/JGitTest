/**
 * Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */
package test.validate;

import java.io.File;
import test.constants.RelativePathConstants;

/**
 * Created by gaurav.gandhi on 24-10-2018.
 */
abstract class AbstractValidator implements ValidatorI {

  File fileToValidate;

  void printMessage(String str, String absPath, String attributeId) {
    System.err.println(
        "Error: " + str + fileToValidate.getName() + " --> " + absPath + "-->" + attributeId);
  }

  String getAbsolutePath(String line) {

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
}
