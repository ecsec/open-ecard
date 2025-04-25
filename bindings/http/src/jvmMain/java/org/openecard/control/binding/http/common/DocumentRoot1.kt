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
 */
package org.openecard.control.binding.http.common

import org.openecard.common.util.FileUtils.getResourceFileListing
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL

/**
 * @author Moritz Horsch
 */
class DocumentRoot(rootPath: String, listFile: String) {
    private var files: Map<String, URL>? = null

    /**
     * Creates a new DocumentRoot.
     *
     * @param rootPath Path of the document root.
     * @throws IOException
     */
    init {
        // strip leading / for the listing code
        try {
            // load all paths
            files = getResourceFileListing(DocumentRoot::class.java, rootPath, listFile)
        } catch (ex: IOException) {
            _logger.error("Invalid path {}", rootPath)
            throw FileNotFoundException(ex.message)
        }
    }

    /**
     * Returns true if the document root contains the file, otherwise false.
     *
     * @param file File
     * @return True if the document root contains the file, otherwise false
     */
    fun contains(file: String): Boolean {
        return files!!.containsKey(file)
    }

    /**
     * Returns files and directories in the document root.
     * The list is an immutable copy of the internal file list.
     *
     * @return Files and directories in the document root
     */
    fun getFiles(): List<URL> {
        return ArrayList(files!!.values)
    }

    /**
     * Returns File or directory in the document root.
     *
     * @param fileName File name
     * @return File or directory in the document root
     */
    fun getFile(fileName: String): URL? {
        val file = files!![fileName]
        if (file == null) {
            _logger.error("Cannot load file: {} ", fileName)
        }
        return file
    }

    companion object {
        private val _logger: Logger = LoggerFactory.getLogger(DocumentRoot::class.java)
    }
}
