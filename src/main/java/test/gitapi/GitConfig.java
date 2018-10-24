/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */
package test.gitapi;

import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Created by gaurav.gandhi on 04-10-2018.
 */
public class GitConfig {

  private String repoDir;
  private String branch;
  private String remote;
  private String httpUrl;
  private UsernamePasswordCredentialsProvider credentials;
  private String commitMessage;

  private GitConfig() {
  }

  public String getRepoDir() {
    return repoDir;
  }

  public String getBranch() {
    return branch;
  }

  public String getRemote() {
    return remote;
  }

  public String getHttpUrl() {
    return httpUrl;
  }

  public UsernamePasswordCredentialsProvider getCredentials() {
    return credentials;
  }

  public String getCommitMessage() {
    return commitMessage;
  }

  public static GitConfigBuilder builder() {
    return new GitConfigBuilder();
  }

  public static class GitConfigBuilder {

    private GitConfig gitConfig = null;

    public GitConfigBuilder() {
      this.gitConfig = new GitConfig();
    }

    public GitConfigBuilder setHttpUrl(String httpUrl) {
      gitConfig.httpUrl = httpUrl;
      return this;
    }

    public GitConfigBuilder setRepoDir(String repoDir) {
      gitConfig.repoDir = repoDir;
      return this;
    }

    public GitConfigBuilder setBranch(String branch) {
      gitConfig.branch = branch;
      return this;
    }

    public GitConfigBuilder setRemote(String remote) {
      gitConfig.remote = remote;
      return this;
    }

    public GitConfigBuilder setCommitMessage(String commitMessage) {
      gitConfig.commitMessage = commitMessage;
      return this;
    }

    public GitConfig build() throws InvalidConfigurationException {

      String missingVariable;
      if ((missingVariable = validateConfig()) != null) {
        throw new InvalidConfigurationException(
            "Please provide a value of " + missingVariable + " in git config");
      }
      return gitConfig;
    }

    private String validateConfig() {

      if (gitConfig.getHttpUrl() == null) {
        return "Http Url";
      } else if (gitConfig.getRepoDir() == null) {
        return "Repo Directory";
      } else if (gitConfig.getBranch() == null) {
        return "Branch";
      } else if (gitConfig.getRemote() == null) {
        return "Remote";
      }
      return null;
    }

    public GitConfigBuilder setCredentials(
        UsernamePasswordCredentialsProvider credentials) {
      gitConfig.credentials = credentials;
      return this;
    }
  }
}
