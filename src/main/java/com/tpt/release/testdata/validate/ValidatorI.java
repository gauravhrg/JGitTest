package com.tpt.release.testdata.validate;

import java.io.IOException;
import java.util.List;

/**
 * Created by gaurav.gandhi on 24-10-2018.
 */
public interface ValidatorI {

  List<String> validate() throws IOException;
}
