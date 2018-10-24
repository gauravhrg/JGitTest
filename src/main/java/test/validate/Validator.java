/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
*/

package test.validate;

import java.io.File;
import org.apache.commons.io.FilenameUtils;


/**
 * Created by gaurav.gandhi on 22-10-2018.
 */
public class Validator {

  private Validator() {
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
