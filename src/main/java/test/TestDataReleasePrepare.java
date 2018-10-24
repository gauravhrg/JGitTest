/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */
package test;

import static test.RelativePathConstants.DOT;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import test.gitapi.GitConfig;
import test.gitapi.GitHandler;

/**
 * Created by gaurav.gandhi on 19-10-2018.
 */
public class TestDataReleasePrepare {


  private String majorVersion;
  private String minorVersion;
  private File releaseDirectory;
  private File updatesDirectory;
  private List<File> sqlsToCommit;
  private GitHandler gitHandler;
  private String version;

  private TestDataReleasePrepare(String[] args) throws Exception {
    initialize(args);
    validate();
  }

  public static void main(String[] args) throws Exception {

    validateArgs(args);
    TestDataReleasePrepare testDataReleasePrepare = new TestDataReleasePrepare(args);
    testDataReleasePrepare.moveFilesFromUpdatesToReleasedDir();
    testDataReleasePrepare.commitFiles();
  }

  private static void validateArgs(String[] args) throws Exception {

    System.out.println(Arrays.toString(args));

    int expectedArgs = 8;

    if (args.length != expectedArgs) {
      throw new Exception("Arguments count mismatch. " + expectedArgs + " != " + args.length);
    }

    for (String arg : args) {
      if (StringUtils.isEmpty(arg)) {
        throw new Exception("Invalid arguments");
      }
    }
  }

  private void initialize(String[] args) throws InvalidConfigurationException {

    version = getVersion(args[0]);
    System.err.println("Copying files from updates to " + version + " version");

    String refDir = args[1] + File.separator;
    this.updatesDirectory = new File(refDir + "Updates");
    this.releaseDirectory = new File(refDir + "Released" + File.separator + version);
    GitConfig gitConfig = GitConfig.
        builder().setHttpUrl(args[2])
        .setRepoDir(args[3])
        .setBranch(args[4])
        .setRemote(args[5])
        .setCredentials(new UsernamePasswordCredentialsProvider(args[6], args[7]))
        .setCommitMessage("Committed testdata scripts for release: " + version)
        .build();
    this.gitHandler = new GitHandler(gitConfig);
  }

  private String getVersion(String version) {

    if (version.endsWith("-SNAPSHOT")) {
      version = version.substring(0, version.indexOf("-SNAPSHOT"));
    }

    int majorVersionIndex = version.indexOf(DOT, 0);
    majorVersion = version.substring(0, majorVersionIndex);

    majorVersionIndex++;

    int minorVersionIndex = version.indexOf(DOT, majorVersionIndex);
    if (minorVersionIndex != -1) {
      minorVersion = version.substring(majorVersionIndex, minorVersionIndex);
    } else {
      minorVersion = version.substring(majorVersionIndex);
    }
    return majorVersion + DOT + minorVersion;
  }

  private void validate() throws Exception {

    if (majorVersion == null || minorVersion == null) {
      throw new Exception("Provided version is invalid");
    }
    if (!updatesDirectory.exists()) {
      throw new Exception("Update directory path is invalid");
    }
    if (!releaseDirectory.exists() && !releaseDirectory.mkdirs()) {
      throw new Exception("Release directory path is invalid");
    }
  }


  private void moveFilesFromUpdatesToReleasedDir() throws IOException {

    Iterator<File> files = getFiles(updatesDirectory, new String[]{"html", "txt", "robot"});
    sqlsToCommit = new ArrayList<>();

    while (files.hasNext()) {
      File fileToUpdate = files.next();
      File updatedFile = updateFile(fileToUpdate);
      boolean deleteInvalidFiles = false;
      validateFile(updatedFile, deleteInvalidFiles);
//      sqlsToCommit.add(updatedFile);
    }
    FileUtils.cleanDirectory(updatesDirectory);
    sqlsToCommit.add(releaseDirectory);
    sqlsToCommit.add(updatesDirectory);
  }

  private File updateFile(File fileToUpdate) throws IOException {

    UpdateRelativePath updateRelativePath;
    updateRelativePath = new UpdateRelativePath(fileToUpdate, updatesDirectory, releaseDirectory);
    return updateRelativePath.updateFile();
  }

  private void validateFile(File updatedFile, boolean deleteIfInvalid) throws IOException {

    ValidateRelativePath validator = new ValidateRelativePath(updatedFile);
    boolean isFileValid = validator.validate();
    if (!isFileValid && deleteIfInvalid) {
      FileUtils.forceDelete(updatedFile);
    }
  }

  private Iterator<File> getFiles(File iteratorDir, String[] fileTypes) {

    List<IOFileFilter> dirNameFilters = new ArrayList<>();
    dirNameFilters.add(new NameFileFilter("Updates", IOCase.INSENSITIVE));
    dirNameFilters.add(new NameFileFilter("Released", IOCase.INSENSITIVE));

    return FileUtils
        .iterateFiles(iteratorDir,
            new SuffixFileFilter(fileTypes, IOCase.INSENSITIVE),
            new NotFileFilter(new OrFileFilter(dirNameFilters)));
  }

  private boolean commitFiles() throws IOException, GitAPIException, URISyntaxException {

    if (sqlsToCommit.isEmpty()) {
      System.out.println("No changes to commit");
      return true;
    }
    System.out.println("Committing files... ");
    boolean filesCommitted = gitHandler.commitFiles(sqlsToCommit);
    if (filesCommitted) {
      System.out.println("Files committed successfully");
    }
    return filesCommitted;
  }
}