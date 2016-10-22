package org.jasr.sequelizer.entities;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.tmatesoft.svn.core.SVNException;

public interface SVNHandler {

    public void pre(SqlJob job) throws SVNException, URISyntaxException, MalformedURLException;

    public void post(SqlJob job) throws SVNException, URISyntaxException, MalformedURLException;

}
