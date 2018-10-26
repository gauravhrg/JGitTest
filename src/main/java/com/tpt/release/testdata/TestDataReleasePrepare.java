/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */
package com.tpt.release.testdata;

import com.tpt.release.testdata.constants.RelativePathConstants;
import com.tpt.release.testdata.gitapi.GitConfig;
import com.tpt.release.testdata.gitapi.GitHandler;
import com.tpt.release.testdata.validate.ValidatorBuilder;
import com.tpt.release.testdata.validate.ValidatorI;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Created by gaurav.gandhi on 19-10-2018.
 */
public class TestDataReleasePrepare {

  public static void main(String[] args) throws Exception {
    new TestDataReleasePrepare(args).prepareRelease();
  }

  private File releaseDirectory;
  private File updatesDirectory;
  private String version;
  private String majorVersion;
  private String minorVersion;
  private GitHandler gitHandler;
  private final String[] args;
  private List<String> fileTypesToValidate;

  private TestDataReleasePrepare(String[] args) {
    this.args = args;
  }

  private void prepareRelease() throws Exception {

    this.validateInputArguments();
    this.initializeConfiguration();
    this.validateConfiguration();
    this.checkOutRepo();
    this.moveFilesToReleaseDir();

//    boolean isCommitSuccessful = this.commitAndPushFiles();
//    if (!isCommitSuccessful) {
//      throw new Exception("Commit is unsuccessful");
//    }
//    FileUtils.cleanDirectory(updatesDirectory);
  }

  private void validateInputArguments() throws Exception {

    System.out.println("Provided arguments are: " + Arrays.toString(args));

    int expectedArgs = 8;

    if (args.length != expectedArgs) {
      throw new Exception("Arguments count mismatch. " + expectedArgs + " != " + args.length);
    }

    for (String arg : args) {
      if (StringUtils.isEmpty(arg)) {
        throw new Exception("Invalid arguments");
      }
    }
    System.out.println();
    System.out.println(
        "---------------------------------------- Input Arguments  --------------------------------");
    System.out.println("Version                  : " + args[0]);
    System.out.println("Reference Data Directory : " + args[1]);
    System.out.println("Repository URL           : " + args[2]);
    System.out.println("Repository Directory     : " + args[3]);
    System.out.println("Branch                   : " + args[4]);
    System.out.println("Remote                   : " + args[5]);
    System.out.println("Username                 : " + args[6]);
    System.out.println("Password                 : " + args[7]);
    System.out.println(
        "---------------------------------------- Input Arguments  --------------------------------");
    System.out.println();
  }

  private void initializeConfiguration() throws InvalidConfigurationException {

    version = getVersion(args[0]);
    System.err.println("Copying files from updates to " + version + " version");

    String refDir = args[1] + File.separator;
    this.updatesDirectory = new File(refDir + "Updates");
    this.releaseDirectory = new File(refDir + "Released" + File.separator + version);

    File repoDir = new File(args[3]);
    System.out.println("Setting repo dir to: " + repoDir.getPath());

    GitConfig gitConfig = GitConfig.
        builder().setHttpUrl(args[2])
        .setRepoDir(repoDir.getPath())
        .setBranch(args[4])
        .setRemote(args[5])
        .setCredentials(new UsernamePasswordCredentialsProvider(args[6], args[7]))
        .setCommitMessage("Committed testdata scripts for release: " + version)
        .build();

    this.gitHandler = new GitHandler(gitConfig);
    this.fileTypesToValidate = Arrays.asList("html", "txt", "robot");
  }

  private String getVersion(String version) {

    if (version.endsWith("-SNAPSHOT")) {
      version = version.substring(0, version.indexOf("-SNAPSHOT"));
    }

    int majorVersionIndex = version.indexOf(RelativePathConstants.DOT, 0);
    majorVersion = version.substring(0, majorVersionIndex);

    majorVersionIndex++;

    int minorVersionIndex = version.indexOf(RelativePathConstants.DOT, majorVersionIndex);
    if (minorVersionIndex != -1) {
      minorVersion = version.substring(majorVersionIndex, minorVersionIndex);
    } else {
      minorVersion = version.substring(majorVersionIndex);
    }
    return majorVersion + RelativePathConstants.DOT + minorVersion;
  }

  private void validateConfiguration() throws Exception {

    if (StringUtils.isEmpty(version) ||
        StringUtils.isEmpty(majorVersion) ||
        StringUtils.isEmpty(minorVersion)) {
      throw new Exception("Release version is invalid");
    }
    if (!updatesDirectory.exists()) {
      throw new Exception("Update directory path is invalid");
    }
    if (!releaseDirectory.exists() && !releaseDirectory.mkdirs()) {
      throw new Exception("Error while creating release directory");
    }
    if (ArrayUtils.isEmpty(this.updatesDirectory.list())) {
      throw new Exception("Update directory is empty");
    }
  }

  private void checkOutRepo() throws GitAPIException, IOException {
    this.gitHandler.checkOutRepo();
  }

  private void moveFilesToReleaseDir() throws Exception {

//    Iterator<File> files = getFiles(updatesDirectory, new String[]{"html", "txt", "robot"});
//    while (files.hasNext()) {
//      File fileToUpdate = files.next();
//      File updatedFile = updateFile(fileToUpdate);
//      validateFile(updatedFile);
//    }

    Collection<File> files = FileUtils.listFiles(updatesDirectory, null, true);

    for (File fileToUpdate : files) {
      File updatedFile = updateFile(fileToUpdate);
      validateFile(updatedFile);
    }
  }

  private File updateFile(File fileToUpdate) throws IOException {
    return new UpdateRelativePath(fileToUpdate, updatesDirectory, releaseDirectory).updateFile();
  }

  private void validateFile(File fileToValidate) throws Exception {

    if (!fileTypesToValidate.contains(FilenameUtils.getExtension(fileToValidate.getName()))) {
      return;
    }
    ValidatorI validator = ValidatorBuilder.getValidator(fileToValidate);
    List<String> errorMessages = validator.validate();

    if (!errorMessages.isEmpty()) {
      System.out.println();
      StringBuilder errorBuilder = new StringBuilder();
      for (String errorMessage : errorMessages) {
        errorBuilder.append(errorMessage).append("\n");
      }
      System.out.println(errorBuilder);
      throw new Exception("Invalid resource path(s) in file: " + fileToValidate);
    }
  }

  @SuppressWarnings("unused")
  public static Iterator<File> getFiles(File iteratorDir, String[] fileTypes) {

    List<IOFileFilter> dirNameFilters = new ArrayList<>();
    dirNameFilters.add(new NameFileFilter("Updates", IOCase.INSENSITIVE));
    dirNameFilters.add(new NameFileFilter("Released", IOCase.INSENSITIVE));

    return FileUtils
        .iterateFiles(iteratorDir,
            new SuffixFileFilter(fileTypes, IOCase.INSENSITIVE),
            new NotFileFilter(new OrFileFilter(dirNameFilters)));
  }

  private boolean commitAndPushFiles() throws GitAPIException, URISyntaxException {

    System.out.println("Committing files... ");
    boolean filesCommitted = gitHandler.commitFiles();
    if (filesCommitted) {
      System.out.println("Files committed successfully");
    }
    return filesCommitted;
  }
}