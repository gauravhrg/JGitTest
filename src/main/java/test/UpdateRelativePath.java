package test;/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import test.constants.RelativePathConstants;

/**
 * Created by gaurav.gandhi on 22-10-2018.
 */
class UpdateRelativePath {

  private final File updatesDirectory;
  private final File releaseDirectory;
  private final File fileToUpdate;
  private final int depthDifference;

  private File updatedFile;

  public UpdateRelativePath(File fileToUpdate, File updatesDirectory, File releaseDirectory) {

    this.fileToUpdate = fileToUpdate;
    this.updatesDirectory = updatesDirectory;
    this.releaseDirectory = releaseDirectory;
    this.depthDifference = calculateDepthDiff(this.releaseDirectory, this.updatesDirectory);
  }

  private int calculateDepthDiff(File releaseDirectory, File updatesDirectory) {
    return getDepth(releaseDirectory) - getDepth(updatesDirectory);
  }

  private int getDepth(File directory) {

    if (!directory.exists()) {
      return 0;
    }
    int levelCount = 1;
    int dirIndex;

    dirIndex = directory.getPath().indexOf(File.separator);
    while (dirIndex != -1) {
      dirIndex = directory.getPath().indexOf(File.separator, dirIndex + 1);
      levelCount++;
    }
    return levelCount;
  }


  File updateFile() throws IOException {

    boolean releaseFileCreated = createFileInReleaseFolder();

    if (releaseFileCreated) {
      updateRelativePathInReleaseFile();

    } else {
      System.err.println("File " + fileToUpdate + " could not be copied");
    }
    return updatedFile;
  }

  private boolean createFileInReleaseFolder() throws IOException {

    String path = getAbsolutePathForReleasedDir(fileToUpdate);

    if (StringUtils.isEmpty(path)) {
      return false;
    }

    File updatedFileDir = new File(path);
    boolean isDirCreated = updatedFileDir.exists();
    if (!isDirCreated) {
      isDirCreated = updatedFileDir.mkdirs();
    }

    if (!isDirCreated) {
      return false;
    }

    updatedFile = new File(updatedFileDir + File.separator + fileToUpdate.getName());

    return updatedFile.exists() || updatedFile.createNewFile();
  }

  private void updateRelativePathInReleaseFile() {

    try (FileOutputStream outputStream = new FileOutputStream(updatedFile)) {

      List<String> lines = FileUtils.readLines(fileToUpdate, "UTF-8");

      for (String line : lines) {

        int startIndex = 0;
        int endIndex;

        /*
          This index is fetched to incorporate below lines

           <href>../../test_data.robot</href>
           <href>./../test_data.robot</href>
         */
        endIndex = getIndex(
            line,
            startIndex,
            RelativePathConstants.SINGLE_DOT_APPENDER_WITH_PREFIX,
            RelativePathConstants.DOUBLE_DOT_APPENDER_WITH_PREFIX
        );

        if (endIndex == -1) {
          /*
            This index is fetched to incorporate below line

            Resource    ../../test_data.robot
           */
          endIndex = getIndex(
              line,
              startIndex,
              RelativePathConstants.SINGLE_DOT_APPENDER,
              RelativePathConstants.DOUBLE_DOT_APPENDER
          );
        }
        StringBuilder updatedLine;

        if (endIndex != -1) {
          line = line.replaceAll("//", "/");
          updatedLine = modifyResourcePath(line, startIndex, endIndex);
        } else {
          updatedLine = new StringBuilder(line);
        }

        outputStream.write(updatedLine.toString().getBytes());
        outputStream.write("\n".getBytes());
      }
      outputStream.flush();
      outputStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private StringBuilder modifyResourcePath(String line, int startIndex, int endIndex) {

    StringBuilder updatedLine = new StringBuilder();

    while (endIndex != -1) {
      updatedLine.append(line.substring(startIndex, endIndex));

      if (depthDifference > 0) {
        updatedLine.append(
            StringUtils.replaceOnce(
                RelativePathConstants.DOUBLE_DOT_APPENDER,
                RelativePathConstants.DOUBLE_DOT_APPENDER,
                StringUtils.repeat(RelativePathConstants.DOUBLE_DOT_APPENDER, depthDifference)));

      } else if (depthDifference < 0) {
        updatedLine.append(
            StringUtils.replaceOnce(
                RelativePathConstants.DOUBLE_DOT_APPENDER,
                RelativePathConstants.DOUBLE_DOT_APPENDER,
                StringUtils.repeat("", Math.abs(depthDifference))));
      }

      startIndex = endIndex;

      endIndex = getIndex(
          line,
          startIndex,
          RelativePathConstants.SINGLE_DOT_APPENDER_WITH_PREFIX,
          RelativePathConstants.DOUBLE_DOT_APPENDER_WITH_PREFIX
      );
    }
    updatedLine.append(line.substring(startIndex));
    return updatedLine;
  }

  private int getIndex(
      String line,
      int fromIndex,
      String singleDotAppender,
      String doubleDotAppender) {

    int index = line.indexOf(singleDotAppender, fromIndex);
    if (index != -1) {
      return index + singleDotAppender.length();
    }
    index = line.indexOf(doubleDotAppender, fromIndex);
    if (index != -1) {
      return index + doubleDotAppender.length();
    }
    return -1;
  }

  private String getAbsolutePathForReleasedDir(File file) {

    File parentFile = file.getParentFile();
    Stack<String> paths = new Stack<>();

    while (!updatesDirectory.getName().toLowerCase()
        .equalsIgnoreCase(parentFile.getName().toLowerCase())) {
      paths.add(parentFile.getName());
      parentFile = parentFile.getParentFile();
    }

    StringBuilder pathBuilder = new StringBuilder(releaseDirectory.getPath());

    if (paths.isEmpty()) {
      //File is at updates level. No need to add additional folder under release directory.
      return pathBuilder.toString();
    }

    pathBuilder.append(File.separator);

    while (!paths.isEmpty()) {
      pathBuilder.append(paths.pop());
      pathBuilder.append(File.separator);
    }
    return pathBuilder.toString();
  }
}
