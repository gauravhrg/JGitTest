/*
  Copyright 2000-2018 Triple Point Technology. All rights reserved.
 */
package test.gitapi;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.eclipse.jgit.transport.URIish;

/**
 * Created by gaurav.gandhi on 03-10-2018.
 */

public class GitHandler {

  private Git git;
  private final GitConfig gitConfig;

  public GitHandler(GitConfig gitConfig) {

    this.gitConfig = gitConfig;
  }

  public boolean commitFiles()
      throws GitAPIException, URISyntaxException {

    RevCommit commit = commitFile();
    System.out.println(commit.getFullMessage());

    System.out.println("Pushing Files... ");
    return pushFiles();
  }

  public void checkOutRepo() throws GitAPIException, IOException {

    System.out.println("Checking out repo: " + gitConfig.getHttpUrl());
    File repoDir = new File(gitConfig.getRepoDir());
    if (repoDir.exists()) {
      System.out.println("Repo already exists. Pulling changes...");
      git = pullChanges(gitConfig);
    } else {
      System.out.println("Cloning repo...");
      git = cloneRepository(gitConfig);
    }
  }


  private Git cloneRepository(GitConfig gitConfig) throws GitAPIException {
    return Git.cloneRepository()
        .setURI(gitConfig.getHttpUrl())
        .setDirectory(new File(gitConfig.getRepoDir()))
        .setBranch(gitConfig.getBranch())
        .call();
  }

  private Repository getRepo(GitConfig gitConfig) throws IOException {
    return new FileRepository(gitConfig.getRepoDir() + File.separator + ".git");
  }

  private Git pullChanges(GitConfig gitConfig) throws GitAPIException, IOException {

    Git git = new Git(getRepo(gitConfig));
    PullCommand pullCmd = git.pull();
    PullResult result = pullCmd.call();
    FetchResult fetchResult = result.getFetchResult();
    System.out.println("Fetch result messages: " + fetchResult.getMessages());
    MergeResult mergeResult = result.getMergeResult();
    System.out.println("Merge status: " + mergeResult.getMergeStatus().isSuccessful());
    return git;
  }

  private RevCommit commitFile() throws URISyntaxException, GitAPIException {

    git.add().addFilepattern(".").call();
    RemoteAddCommand remoteAddCommand = git.remoteAdd();
    remoteAddCommand.setName(gitConfig.getRemote());
    remoteAddCommand.setUri(new URIish(gitConfig.getHttpUrl()));
    remoteAddCommand.call();

    CommitCommand commitCommand = git.commit();
    commitCommand.setAll(true);
    commitCommand.setMessage(gitConfig.getCommitMessage());
    return commitCommand.call();
  }

  private boolean pushFiles() throws GitAPIException {

    PushCommand pushCommand = git.push();
    pushCommand.setRemote(gitConfig.getRemote());
    pushCommand.setCredentialsProvider(gitConfig.getCredentials());
    Iterable<PushResult> pushResults = pushCommand.call();
    for (PushResult pushResult : pushResults) {
      for (RemoteRefUpdate remoteRefUpdate : pushResult.getRemoteUpdates()) {
        if (!remoteRefUpdate.getStatus().equals(Status.OK)) {
          return false;
        }
      }
    }
    return true;
  }
}
