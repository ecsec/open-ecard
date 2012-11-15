/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.scio;

import android.content.Context;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * This class is used to unpack zipped resources (e.g. pcsc drivers) to make
 * them accessible for the app.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class ResourceUnpacker {

    /**
     * Unpacks a zipped resource to a specific folder.
     * 
     * @param ins
     *            an inputstream pointing to the resource to unpack
     * @param ctx
     *            the application context
     * @param file
     *            destination folder where the resource is unpacked to
     * @throws IOException if an io related error occurs while unpacking the resource
     */
    public static void unpackResources(InputStream ins, Context ctx, File file) throws IOException {
	// Open the ZipInputStream
	ZipInputStream inputStream = new ZipInputStream(ins);

	// Loop through all the files and folders
	for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {

	    String innerFileName = file + File.separator + entry.getName();
	    File innerFile = new File(innerFileName);
	    if (innerFile.exists()) {
		innerFile.delete();
	    }

	    // Check if it is a folder
	    if (entry.isDirectory()) {
		// Its a folder, create that folder
		innerFile.mkdirs();
	    } else {
		// Create a file output stream
		FileOutputStream outputStream = new FileOutputStream(innerFileName);
		final int BUFFER = 2048;

		// Buffer the ouput to the file
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, BUFFER);

		// Write the contents
		int count = 0;
		byte[] data = new byte[BUFFER];
		while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
		    bufferedOutputStream.write(data, 0, count);
		}

		// Flush and close the buffers
		bufferedOutputStream.flush();
		bufferedOutputStream.close();
	    }

	    // Close the current entry
	    inputStream.closeEntry();
	}
	inputStream.close();
    }

}