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

package org.openecard.client.control.binding.http.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openecard.client.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class DocumentRoot {

    private static final Logger _logger = LoggerFactory.getLogger(DocumentRoot.class);

    private Map<String,URL> files;

    /**
     * Creates a new DocumentRoot.
     *
     * @param rootPath Path of the document root.
     * @throws IOException
     */
    public DocumentRoot(String rootPath, String listFile) throws IOException {
	// strip leading / for the listing code
	try {
	    // load all paths
	    files = FileUtils.getResourceFileListing(DocumentRoot.class, rootPath, listFile);
	} catch (IOException ex) {
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
    public boolean contains(String file) {
	return files.containsKey(file);
    }

    /**
     * Returns files and directories in the document root.
     * The list is an immutable copy of the internal file list.
     *
     * @return Files and directories in the document root
     */
    public List<URL> getFiles() {
	return new ArrayList<URL>(files.values());
    }

    /**
     * Returns File or directory in the document root.
     *
     * @param fileName File name
     * @return File or directory in the document root
     */
    public URL getFile(String fileName) {
	URL file = files.get(fileName);
	if (file == null) {
	    _logger.error("Cannot load file: {} ", fileName);
	}
	return file;
    }

}
