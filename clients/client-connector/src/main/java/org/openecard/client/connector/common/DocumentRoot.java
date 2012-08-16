/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.connector.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.openecard.client.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class DocumentRoot {

    private static final Logger _logger = LoggerFactory.getLogger(DocumentRoot.class);

    private URL path;
    private List<URL> files;

    /**
     * Creates a new DocumentRoot.
     *
     * @param rootPath Path of the document root.
     * @throws IOException
     */
    public DocumentRoot(String rootPath) throws IOException {
	// strip leading / for the listing code
	if (rootPath.startsWith("/")) {
	    rootPath = rootPath.substring(1);
	}
	try {
	    path = DocumentRoot.class.getResource(rootPath);
	    if (path == null) {
		path = DocumentRoot.class.getResource("/" + rootPath);
	    }
	    if (path == null) {
		FileNotFoundException ex = new FileNotFoundException("Path denoted by '" + rootPath + "' does not exist in the classpath.");
		_logger.error(ex.getMessage(), ex);
		throw ex;
	    }
	    // load all paths
	    files = FileUtils.getResourceListing(DocumentRoot.class, rootPath);
	} catch (URISyntaxException ex) {
	    _logger.error("Invalid path {}", rootPath);
	    throw new FileNotFoundException(ex.getMessage());
	}
    }

    /**
     * Returns true if the document root contains the file, otherwise false.
     *
     * @param file File
     * @return True if the document root contains the file, otherwise false
     */
    public boolean contains(URL file) {
	return files.contains(file);
    }

    /**
     * Returns files and directories in the document root.
     * The list is an immutable copy of the internal file list.
     *
     * @return Files and directories in the document root
     */
    public List<URL> getFiles() {
	return Collections.unmodifiableList(files);
    }

    /**
     * Returns File or directory in the document root.
     *
     * @param fileName File name
     * @return File or directory in the document root
     */
    public URL getFile(String fileName) {
	for (URL f : files) {
	    String t = path.toString() + fileName;
	    if (f.toString().equals(t)) {
		return f;
	    }
	}
	_logger.error("Cannot load file: {} ", path.toString() + fileName);
	return null;
    }

    /**
     * Return the document root path.
     *
     * @return Document root path
     */
    public URL getPath() {
	return path;
    }

}
