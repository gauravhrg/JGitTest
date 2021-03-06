/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */
package com.tpt.release.testdata.validate;

import com.tpt.release.testdata.constants.RelativePathConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaurav.gandhi on 24-10-2018.
 */
abstract class AbstractValidator implements ValidatorI {

  File fileToValidate;
  final List<String> errorMessages = new ArrayList<>();

  void appendErrorMessage(String str, String absPath, String attributeId) {
    errorMessages
        .add("Error: " + str + fileToValidate.getName() + " --> " + absPath + " --> " + attributeId);
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
