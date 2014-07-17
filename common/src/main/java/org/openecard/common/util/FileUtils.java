/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

package org.openecard.common.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.openecard.common.io.LimitedInputStream;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class FileUtils {

    /**
     * Finds and returns the user specific config directory.
     * The config directory is defined as '$HOME/.openecard'. This function evaluates the system property 'user.home'
     * and simply appends '.openecard'. <br/>
     * The config directory can be used to save logging configs, logs, certificates and anything else one might think
     * of.
     *
     * @return File object pointing to the config directory.
     * @throws IOException In case the directory can not be found.
     * @throws SecurityException In case the directory and/or one of its parents could not be created.
     */
    public static File getHomeConfigDir() throws IOException, SecurityException {
	final String dirName = "openecard";
	final String home = System.getProperty("user.home");

	if (home != null) {
	    String pathname = home + File.separator + "." + dirName;
	    File path = new File(pathname);
	    path.mkdirs();
	    return path;
	} else {
	    throw new IOException("Home path can not be determined.");
	}
    }

    /**
     * Finds and returns the user specific addons directory.
     * The addons directory is defined as '$HOME/.openecard/addons'. This function evaluates the system property 'user.home'
     * and simply appends '.openecard/addons'. <br/>
     * The addons directory can be used to save addons, theire configuration and related stuff.
     *
     * @return File object pointing to the addons directory.
     * @throws IOException In case the directory can not be found.
     * @throws SecurityException In case the directory and/or one of its parents could not be created.
     */
    public static File getAddonsDir() throws IOException, SecurityException {
	File path = new File(getHomeConfigDir(), "addons");
	path.mkdirs();
	return path;
    }

    /**
     * Finds and returns the user specific addons configuration directory.
     * The addons directory is defined as '$HOME/.openecard/addons/conf'. This function evaluates the system property
     * 'user.home' and simply appends '.openecard/addons/conf'. <br/>
     * The addons conf directory can be used to save the configuration of the specific addon.
     *
     * @return File object pointing to the addons directory.
     * @throws IOException In case the directory can not be found.
     * @throws SecurityException In case the directory and/or one of its parents could not be created.
     */
    public static File getAddonsConfDir() throws IOException, SecurityException {
	File path = new File(getAddonsDir(), "conf");
	path.mkdirs();
	return path;
    }

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
    public static Map<String,URL> getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
	URL dirURL = clazz.getClassLoader().getResource(path);
	if (dirURL != null && dirURL.getProtocol().equals("file")) {
	    File dirFile = new File(dirURL.toURI());
	    return getSubdirFileListing(dirFile, dirURL.toExternalForm());
	}

	// TODO: I think this code is not needed (at least on linux), revise on windows and remove if possible
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
	    TreeMap<String,URL> result = new TreeMap<String,URL>(); //avoid duplicates in case it is a subdirectory
	    while (entries.hasMoreElements()) {
		JarEntry nextEntry = entries.nextElement();
		// skip directory entries
		if (! nextEntry.isDirectory()) {
		    String name = nextEntry.getName();
		    if (name.startsWith(path)) { //filter according to the path
			String entryPath = jarUrl + "!/" + name;
			String prefix = "/" + name.substring(path.length());
			result.put(prefix, new URL(entryPath));
		    }
		}
	    }
	    return result;
	}

	throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    private static TreeMap<String,URL> getSubdirFileListing(File dir, String base) throws MalformedURLException {
	TreeMap<String,URL> resultList = new TreeMap<String,URL>();
	for (File next : dir.listFiles()) {
	    if (next.canRead() && next.isDirectory()) {
		resultList.putAll(getSubdirFileListing(next, base));
	    } else if (next.canRead() && next.isFile()) {
		// generate prefix
		URL fileURL = next.toURI().toURL();
		String prefix = fileURL.toExternalForm().substring(base.length()-1);
		resultList.put(prefix, fileURL);
	    }
	}
	return resultList;
    }


    /**
     * Map list of files to resource URLs.
     * The list file must itself be present in the classpath and contain unix style path values separated by colons (:).
     * These path values must be relative to the classpath. The map key is path without the given prefix.<br/>
     * E.g.
     * <code>/www/index.html</code> becomes <code>/index.html</code> -> <code>some-url-to-the-file</code>.
     *
     * @param clazz Base for the {@link java.lang.Class#getResource()} operation.
     * @param prefix Prefix common to all path entries.
     * @param listFile File with the path entries.
     * @return Mapping of all files without the classpath prefix to their respective URLs.
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static Map<String,URL> getResourceFileListing(Class clazz, String prefix, String listFile) throws UnsupportedEncodingException, IOException {
	InputStream fileStream = resolveResourceAsStream(clazz, listFile);
	String fileValue = toString(fileStream);
	String[] files = fileValue.split(":");

	TreeMap<String,URL> result = new TreeMap<String,URL>();
	for (String file : files) {
	    URL fileUrl = resolveResourceAsURL(clazz, file);
	    if (fileUrl != null) {
		result.put(file.substring(prefix.length()), fileUrl);
	    }
	}

	return result;
    }


    /**
     * Same as {@link java.lang.Class#getResourceAsStream()} but works with and without jars reliably.
     * In fact the resource is tried to be loaded with and without / in front of the path.
     *
     * @param clazz Base for the <code>getResource()</code> operation.
     * @param name Name of the resource.
     * @return Open stream to the resource or null if none found.
     * @throws IOException
     */
    public static InputStream resolveResourceAsStream(Class clazz, String name) throws IOException {
	URL url = resolveResourceAsURL(clazz, name);
	if (url != null) {
	    return url.openStream();
	}
	return null;
    }
    /**
     * Same as {@link java.lang.Class#getResource()} but works with and without jars reliably.
     * In fact the resource is tried to be loaded with and without / in front of the path.
     *
     * @param clazz Base for the <code>getResource()</code> operation.
     * @param name name of the resource.
     * @return URL to the resource or null if none found.
     * @throws IOException
     */
    public static URL resolveResourceAsURL(Class clazz, String name) {
	URL url = clazz.getResource(name);
	if (url == null) {
	    name = name.startsWith("/") ? name.substring(1) : "/" + name;
	    url = clazz.getResource(name);
	}
	return url;
    }

}
