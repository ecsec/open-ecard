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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    private final File path;
    private List<File> files = new ArrayList<File>();

    /**
     * Creates a new DocumentRoot.
     *
     * @param rootPath Path of the document root.
     * @throws IOException
     */
    public DocumentRoot(String rootPath) throws IOException {
	try {
	    rootPath = FileUtils.convertPath(rootPath);
	    if (!rootPath.startsWith(File.separator)) {
		rootPath = File.separator + rootPath;
	    }

	    path = new File(DocumentRoot.class.getClassLoader().getResource(rootPath).toURI());
	} catch (Exception e) {
	    _logger.error("Invalid path {}", rootPath);
	    throw new IOException(e);
	}
	// Get files and directories in the document root
	listFiles(path);
    }

    private void listFiles(File file) {
	if (file != null && file.canRead()) {
	    files.add(file);

	    _logger.debug("Add file: {} ", file.toString());

	    if (file.isDirectory()) {
		for (File f : file.listFiles()) {
		    listFiles(f);
		}
	    }
	}
    }

    /**
     * Returns true if the document root contains the file, otherwise false.
     *
     * @param file File
     * @return True if the document root contains the file, otherwise false
     */
    public boolean contains(File file) {
	return files.contains(file);
    }

    /**
     * Returns files and directories in the document root.
     *
     * @return Files and directories in the document root
     */
    public List<File> getFiles() {
	return files;
    }

    /**
     * Returns File or directory in the document root.
     *
     * @param fileName File name
     * @return File or directory in the document root
     */
    public File getFile(String fileName) {
	fileName = FileUtils.convertPath(fileName);
	for (File f : files) {
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
    public File getPath() {
	return path;
    }

}
