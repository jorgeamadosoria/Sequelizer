package org.jasr.sequelizer.entities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.utils.URIBuilder;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.ISVNConflictHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNConflictChoice;
import org.tmatesoft.svn.core.wc.SVNConflictDescription;
import org.tmatesoft.svn.core.wc.SVNConflictResult;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import org.tmatesoft.svn.core.wc2.SvnUpdate;

public class SVNHandlerImpl implements SVNHandler {

    private SVNClientManager clientManager;
    private String           username;
    private String           password;
    private String           csvFolder;
    private String           svnBase;
    private File             localFile;
    private File             localFolder;
    private SVNURL           urlFile;
    private SVNURL           urlFolder;

    public SVNHandlerImpl(SqlJob job, String csvFolder, String svnBase, String username, String password)
            throws URISyntaxException, SVNException, MalformedURLException {
        this.username = username;
        this.password = password;
        this.csvFolder = csvFolder;
        this.svnBase = svnBase;
        this.localFile = buildLocalFile(job);
        this.localFolder = buildLocalFolder(job);
        this.urlFile = buildFileUrl(job);
        this.urlFolder = buildFolderUrl(job);

        DefaultSVNOptions options = new DefaultSVNOptions();
        options.setConflictHandler(new ConflictResolverHandler());
        clientManager = SVNClientManager.newInstance(options, username, password);
    }

    private SVNURL buildFileUrl(SqlJob job) throws URISyntaxException, SVNException, MalformedURLException {
        return SVNURL.parseURIEncoded(new URIBuilder(svnBase).setPath(job.getCsvName()).toString());
    }

    private SVNURL buildFolderUrl(SqlJob job) throws URISyntaxException, SVNException, MalformedURLException {
        return SVNURL.parseURIEncoded(new URIBuilder(svnBase)
                .setPath(job.getCsvName().substring(0, job.getCsvName().lastIndexOf(job.csvFileNameOnly()))).toString());
    }

    private File buildLocalFile(SqlJob job) {
        return Paths.get(csvFolder, job.getProject(), job.csvFileNameOnly()).toFile();
    }

    private File buildLocalFolder(SqlJob job) {
        return Paths.get(csvFolder, job.getProject()).toFile();
    }

    private static class ConflictResolverHandler implements ISVNConflictHandler {
        public SVNConflictResult handleConflict(SVNConflictDescription conflictDescription) throws SVNException {
            return new SVNConflictResult(SVNConflictChoice.THEIRS_FULL, conflictDescription.getMergeFiles().getResultFile());
        }
    }

    private void checkout(SqlJob job) throws SVNException, URISyntaxException, MalformedURLException {
        clientManager.getUpdateClient().doCheckout(buildFolderUrl(job), localFolder, SVNRevision.HEAD, SVNRevision.HEAD,
                SVNDepth.FILES, false);
    }

    private void update(SqlJob job) throws SVNException, URISyntaxException, MalformedURLException {
        clientManager.getUpdateClient().doUpdate(localFolder, SVNRevision.HEAD, SVNDepth.FILES, false, false);
    }

    public void commitFile(SqlJob job) throws SVNException, URISyntaxException {
        clientManager.getCommitClient().doCommit(new File[] { localFile }, false,
                "committing Sequelizer changes to " + job.csvFileNameOnly(), null, null, false, false, SVNDepth.INFINITY);
    }

    public void addFile(SqlJob job) throws SVNException, URISyntaxException {
        clientManager.getCommitClient().doImport(localFile, urlFile, "committing Sequelizer changes to " + job.csvFileNameOnly(),
                null, false, true, SVNDepth.INFINITY);
    }
    
    public void addFolder(SqlJob job) throws SVNException, URISyntaxException {
        clientManager.getCommitClient().doImport(localFolder, urlFolder, "committing Sequelizer changes to " + job.csvFileNameOnly(),
                null, false, true, SVNDepth.INFINITY);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private boolean localFileIsVersioned(SqlJob job) throws SVNException, URISyntaxException {
        if (!localFile.exists())
            return false;
        try {
            return clientManager.getStatusClient().doStatus(localFile, false).isVersioned();
        }
        catch (SVNException e) {
            return false;
        }
    }
    
    private boolean localFolderIsVersioned(SqlJob job) throws SVNException, URISyntaxException {
        try {
            return clientManager.getStatusClient().doStatus(localFolder, false).isVersioned();
        }
        catch (SVNException e) {
            return false;
        }
    }

    private boolean svnFileExists(SqlJob job) throws SVNException, URISyntaxException {
        SVNRepository repos = clientManager.createRepository(urlFile, false);
        SVNNodeKind nodeKind = repos.checkPath("", SVNRevision.HEAD.getNumber());
        return nodeKind == SVNNodeKind.FILE;
    }

    @Override
    public void pre(SqlJob job) throws SVNException, URISyntaxException, MalformedURLException {

        if (!localFileIsVersioned(job))
            FileUtils.deleteQuietly(localFile);

        if (localFolderIsVersioned(job))
            update(job);
        else {
            if (svnFileExists(job))
                checkout(job);
        }
    }

    @Override
    public void post(SqlJob job) throws SVNException, URISyntaxException {

        if (localFileIsVersioned(job))
            commitFile(job);
        else {
            if (localFolderIsVersioned(job))
                addFile(job);
            else
                addFolder(job);
        }
    }
}
