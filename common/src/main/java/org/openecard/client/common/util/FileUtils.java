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

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.openecard.client.common.io.LimitedInputStream;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
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
	BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
	return toByteArray(is);
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
	BufferedInputStream is = new BufferedInputStream(new LimitedInputStream(new FileInputStream(file)), limit);
	return toByteArray(is);
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	byte[] buffer = new byte[4*1024]; // disks use 4k nowadays
	int i;
	while ((i = is.read(buffer)) != -1) {
	    baos.write(buffer, 0, i);
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
	return toString(new FileInputStream(file));
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
	return toString(new FileInputStream(file), charset);
    }

    public static String toString(InputStream in) throws UnsupportedEncodingException, IOException {
	return toString(in, "UTF-8");
    }

    public static String toString(InputStream in, String charset) throws UnsupportedEncodingException, IOException {
	return new String(toByteArray(in), charset);
    }


    /**
     * List directory contents for a resource folder. This is basically a brute-force implementation.
     * Works for regular files and also JARs. <p>Taken from
     * {@link http://www.uofr.net/~greg/java/get-resource-listing.html} and modified for our needs.</p>
     *
     * @author Greg Briggs
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path Should end with "/", but not start with one.
     * @return List of URLs pointing to all subentries including the specified one.
     * @throws URISyntaxException
     * @throws IOException
     */
    public static List<URL> getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
	URL dirURL = clazz.getClassLoader().getResource(path);
	if (dirURL != null && dirURL.getProtocol().equals("file")) {
	    LinkedList<URL> resultList = new LinkedList<URL>();
	    File dirFile = new File(dirURL.toURI());
	    resultList.add(dirURL);
	    // recurse on directory
	    if (dirFile.isDirectory()) {
		String[] subPaths = dirFile.list();
		for (String next : subPaths) {
		    List<URL> subdir = getResourceListing(clazz, next);
		    resultList.addAll(subdir);
		}
	    }
	    return resultList;
	}

	if (dirURL == null) {
	    // In case of a jar file, we can't actually find a directory.
	    // Have to assume the same jar as clazz.
	    String me = clazz.getName().replace(".", "/") + ".class";
	    dirURL = clazz.getClassLoader().getResource(me);
	}

	if (dirURL.getProtocol().equals("jar")) {
	    // a JAR path
	    String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
	    String jarUrl = dirURL.toExternalForm().substring(0, dirURL.toExternalForm().indexOf("!"));
	    JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
	    Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
	    HashSet<URL> result = new HashSet<URL>(); //avoid duplicates in case it is a subdirectory
	    while (entries.hasMoreElements()) {
		JarEntry nextEntry = entries.nextElement();
		String name = nextEntry.getName();
		if (name.startsWith(path)) { //filter according to the path
		    String entryPath = jarUrl + "!/" + name;
		    result.add(new URL(entryPath));
		}
	    }
	    return new LinkedList<URL>(result);
	}

	throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

}
