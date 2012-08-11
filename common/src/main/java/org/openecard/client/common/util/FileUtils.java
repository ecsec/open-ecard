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

package org.openecard.client.common.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.openecard.client.common.io.LimitedInputStream;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class FileUtils {

    /**
     * Reads a file.
     *
     * @param file File
     * @return File content as a byte array
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static byte[] toByteArray(File file) throws FileNotFoundException, IOException {
	InputStream is = new BufferedInputStream(new FileInputStream(file));
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	int b;
	while ((b = is.read()) != -1) {
	    baos.write(b);
	}

	return baos.toByteArray();
    }

    /**
     * Reads a file.
     *
     * @param file File
     * @param limit Limit of bytes to be read
     * @return File content as a byte array
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static byte[] toByteArray(File file, int limit) throws FileNotFoundException, IOException {
	InputStream is = new BufferedInputStream(new LimitedInputStream(new FileInputStream(file)), limit);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	int b;
	while ((b = is.read()) != -1) {
	    baos.write(b);
	}

	return baos.toByteArray();
    }

    /**
     * Reads a file.
     *
     * @param file File
     * @return File content as a String
     * @throws FileNotFoundException
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static String toString(File file) throws FileNotFoundException, IOException, UnsupportedEncodingException {
	return new String(toByteArray(file), "UTF-8");
    }

    /**
     * Reads a file.
     *
     * @param file File
     * @param charset Charset
     * @return File content as a String
     * @throws FileNotFoundException
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static String toString(File file, String charset) throws FileNotFoundException, IOException, UnsupportedEncodingException {
	return new String(toByteArray(file), charset);
    }

}
