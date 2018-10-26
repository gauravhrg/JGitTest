/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */
package com.tpt.release.testdata.validate;

import com.tpt.release.testdata.constants.RelativePathConstants;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * Created by gaurav.gandhi on 24-10-2018.
 */
public class TextValidator extends AbstractValidator {

  public TextValidator(File fileToValidate) {
    this.fileToValidate = fileToValidate;
  }

  @Override
  public List<String> validate() throws IOException {

    List<String> lines = FileUtils.readLines(fileToValidate, RelativePathConstants.ENCODING_UTF_8);

    for (String line : lines) {

      if (line.contains(RelativePathConstants.SINGLE_DOT_APPENDER)
          || line.toLowerCase().contains("resource")) {

        String resourcePath = getAbsolutePath(line);
        File resourceFile = new File(resourcePath);
        if (!resourceFile.exists() && !resourceFile.getParentFile().exists()) {
          appendErrorMessage("", resourcePath, null);
        }
      }
    }
    return errorMessages;
  }
}
