/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.common.util

import org.openecard.common.io.LimitedInputStream
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.net.URLDecoder
import java.util.Scanner
import java.util.TreeMap
import java.util.jar.JarFile

/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
object FileUtils {
	@JvmStatic
	@get:Throws(IOException::class, SecurityException::class)
	val homeConfigDir: File
		/**
		 * Finds and returns the user specific config directory.
		 * The config directory is defined as '$HOME/.openecard'. This function evaluates the system property 'user.home'
		 * and simply appends '.openecard'. <br></br>
		 * The config directory can be used to save logging configs, logs, certificates and anything else one might think
		 * of.
		 *
		 * @return File object pointing to the config directory.
		 * @throws IOException In case the directory can not be found.
		 * @throws SecurityException In case the directory and/or one of its parents could not be created.
		 */
		get() {
			val dirName = "openecard"
			val home = System.getProperty("user.home")

			if (home != null) {
				val pathname = home + File.separator + "." + dirName
				val path = File(pathname)
				path.mkdirs()
				return path
			} else {
				throw IOException("Home path can not be determined.")
			}
		}

	@JvmStatic
	@get:Throws(IOException::class, SecurityException::class)
	val addonsDir: File
		/**
		 * Finds and returns the user specific addons directory.
		 * The addons directory is defined as '$HOME/.openecard/addons'. This function evaluates the system property 'user.home'
		 * and simply appends '.openecard/addons'. <br></br>
		 * The addons directory can be used to save addons, their configuration and related stuff.
		 *
		 * @return File object pointing to the addons directory.
		 * @throws IOException In case the directory can not be found.
		 * @throws SecurityException In case the directory and/or one of its parents could not be created.
		 */
		get() {
			val path = File(homeConfigDir, "addons")
			path.mkdirs()
			return path
		}

	@JvmStatic
	@get:Throws(IOException::class, SecurityException::class)
	val addonsConfDir: File
		/**
		 * Finds and returns the user specific addons configuration directory.
		 * The addons directory is defined as '$HOME/.openecard/addons/conf'. This function evaluates the system property
		 * 'user.home' and simply appends '.openecard/addons/conf'. <br></br>
		 * The addons conf directory can be used to save the configuration of the specific addon.
		 *
		 * @return File object pointing to the addons directory.
		 * @throws IOException In case the directory can not be found.
		 * @throws SecurityException In case the directory and/or one of its parents could not be created.
		 */
		get() {
			val path = File(addonsDir, "conf")
			path.mkdirs()
			return path
		}

	/**
	 * Reads a file.
	 *
	 * @param file File
	 * @return File content as a byte array
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@JvmStatic
	@Throws(FileNotFoundException::class, IOException::class)
	fun toByteArray(file: File): ByteArray {
		val inStream = BufferedInputStream(FileInputStream(file))
		return toByteArray(inStream)
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
	@JvmStatic
	@Throws(FileNotFoundException::class, IOException::class)
	fun toByteArray(
		file: File,
		limit: Int,
	): ByteArray {
		val inStream = BufferedInputStream(LimitedInputStream(FileInputStream(file)), limit)
		return toByteArray(inStream)
	}

	@JvmStatic
	@Throws(IOException::class)
	fun toByteArray(inStream: InputStream): ByteArray {
		val baos = ByteArrayOutputStream()
		val buffer = ByteArray(4 * 1024) // disks use 4k nowadays
		var i: Int
		while ((inStream.read(buffer).also { i = it }) != -1) {
			baos.write(buffer, 0, i)
		}
		return baos.toByteArray()
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
	@JvmStatic
	@JvmOverloads
	@Throws(FileNotFoundException::class, IOException::class, UnsupportedEncodingException::class)
	fun toString(
		file: File,
		charset: String = "UTF-8",
	): String = toString(FileInputStream(file), charset)

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
	@JvmStatic
	@JvmOverloads
	@Throws(UnsupportedEncodingException::class, IOException::class)
	fun toString(
		inStream: InputStream,
		charset: String = "UTF-8",
	): String = String(toByteArray(inStream), charset(charset))

	/**
	 * List directory contents for a resource folder.
	 * This is basically a brute-force implementation. Works for regular files and also JARs.
	 *
	 * Taken from [
 * http://www.uofr.net/~greg/java/get-resource-listing.html](http://www.uofr.net/~greg/java/get-resource-listing.html) and modified for our needs.
	 *
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param path Should end with "/", but not start with one.
	 * @return List of URLs pointing to all subentries including the specified one.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@Throws(URISyntaxException::class, IOException::class)
	fun getResourceListing(
		clazz: Class<*>,
		path: String,
	): Map<String, URL> {
		var dirURL = clazz.classLoader.getResource(path)
		if (dirURL != null && dirURL.protocol == "file") {
			val dirFile = File(dirURL.toURI())
			return getSubdirFileListing(dirFile, dirURL.toExternalForm())
		}

		// TODO: I think this code is not needed (at least on linux), revise on windows and remove if possible
		if (dirURL == null) {
			// In case of a jar file, we can't actually find a directory.
			// Have to assume the same jar as clazz.
			val me = clazz.name.replace(".", "/") + ".class"
			dirURL = clazz.classLoader.getResource(me)
		}

		if (dirURL!!.protocol == "jar") {
			// a JAR path
			val jarPath = dirURL.path.substring(5, dirURL.path.indexOf("!")) // strip out only the JAR file
			val jarUrl = dirURL.toExternalForm().substring(0, dirURL.toExternalForm().indexOf("!"))
			val jar = JarFile(URLDecoder.decode(jarPath, "UTF-8"))
			val entries = jar.entries() // gives ALL entries in jar
			val result = TreeMap<String, URL>() // avoid duplicates in case it is a subdirectory
			while (entries.hasMoreElements()) {
				val nextEntry = entries.nextElement()
				// skip directory entries
				if (!nextEntry.isDirectory) {
					val name = nextEntry.name
					if (name.startsWith(path)) { // filter according to the path
						val entryPath = "$jarUrl!/$name"
						val prefix = "/" + name.substring(path.length)
						result[prefix] = URL(entryPath)
					}
				}
			}
			return result
		}

		throw UnsupportedOperationException("Cannot list files for URL $dirURL")
	}

	@Throws(MalformedURLException::class)
	private fun getSubdirFileListing(
		dir: File,
		base: String,
	): MutableMap<String, URL> {
		val resultList = mutableMapOf<String, URL>()
		val files = dir.listFiles()
		if (files.isNullOrEmpty()) {
			return resultList
		}
		for (next in files) {
			if (next.canRead() && next.isDirectory) {
				resultList.putAll(getSubdirFileListing(next, base))
			} else if (next.canRead() && next.isFile) {
				// generate prefix
				val fileURL = next.toURI().toURL()
				val prefix = fileURL.toExternalForm().substring(base.length - 1)
				resultList[prefix] = fileURL
			}
		}
		return resultList
	}

	/**
	 * Map list of files to resource URLs.
	 * The list file must itself be present in the classpath and contain unix style path values separated by colons (:).
	 * These path values must be relative to the classpath. The map key is path without the given prefix.<br></br>
	 * E.g.
	 * `/www/index.html` becomes `/index.html` -&gt; `some-url-to-the-file`.
	 *
	 * @param clazz Base for the [Class.getResource] operation.
	 * @param prefix Prefix common to all path entries.
	 * @param listFile File with the path entries.
	 * @return Mapping of all files without the classpath prefix to their respective URLs.
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	@JvmStatic
	@Throws(UnsupportedEncodingException::class, IOException::class)
	fun getResourceFileListing(
		clazz: Class<*>,
		prefix: String,
		listFile: String,
	): Map<String, URL> {
		val fileStream =
			resolveResourceAsStream(clazz, listFile)
				?: throw FileNotFoundException("Resource file not found: $listFile")
		val fileValue = toString(fileStream)
		val files = fileValue.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

		val result = mutableMapOf<String, URL>()
		for (file in files) {
			val fileUrl = resolveResourceAsURL(clazz, file)
			if (fileUrl != null) {
				result[file.substring(prefix.length)] = fileUrl
			}
		}

		return result
	}

	/**
	 * Same as [Class.getResourceAsStream] but works with and without jars reliably.
	 * In fact the resource is tried to be loaded with and without / in front of the path.
	 *
	 * @param clazz Base for the `getResource()` operation.
	 * @param name Name of the resource.
	 * @return Open stream to the resource or null if none found.
	 * @throws IOException
	 */
	@JvmStatic
	@Throws(IOException::class)
	fun resolveResourceAsStream(
		clazz: Class<*>,
		name: String,
	): InputStream? {
		val url = resolveResourceAsURL(clazz, name)
		if (url != null) {
			return url.openStream()
		}
		return null
	}

	/**
	 * Same as [Class.getResource] but works with and without jars reliably.
	 * In fact the resource is tried to be loaded with and without / in front of the path.
	 *
	 * @param clazz Base for the `getResource()` operation.
	 * @param name name of the resource.
	 * @return URL to the resource or null if none found.
	 */
	@JvmStatic
	fun resolveResourceAsURL(
		clazz: Class<*>,
		name: String,
	): URL? {
		var url = clazz.getResource(name)
		if (url == null) {
			val nextName = if (name.startsWith("/")) name.substring(1) else "/$name"
			url = clazz.getResource(nextName)
		}
		return url
	}

	/**
	 * Same as [Class.getResourceAsStream] but works with and without jars reliably.
	 * In fact the resource is tried to be loaded with and without / in front of the path. The method needs ClassLoader
	 * to be able to load the resource. This is mainly intended for the usage with an add-on jar file.
	 *
	 * @param loader ClassLoader to use for resolving the source.
	 * @param name Name of the resource.
	 * @return Open stream to the resource or null if none found.
	 * @throws IOException
	 */
	@JvmStatic
	@Throws(IOException::class)
	fun resolveResourceAsStream(
		loader: ClassLoader,
		name: String,
	): InputStream? {
		val url = resolveResourceAsURL(loader, name)
		if (url != null) {
			return url.openStream()
		}
		return null
	}

	/**
	 * Same as [Class.getResource] but works with and without jars reliably.
	 * In fact the resource is tried to be loaded with and without / in front of the path. The method needs ClassLoader
	 * to be able to resolve the resource as URL. This is mainly intended for the usage with add-on jar file.
	 *
	 * @param loader
	 * @param name
	 * @return URL to the resource, or `null` if the resource does not exist in the classpath.
	 */
	fun resolveResourceAsURL(
		loader: ClassLoader,
		name: String,
	): URL? {
		var url = loader.getResource(name)
		if (url == null) {
			val nextName = if (name.startsWith("/")) name.substring(1) else "/$name"
			url = loader.getResource(nextName)
		}
		return url
	}

	@JvmStatic
	@JvmOverloads
	fun readLinesFromConfig(
		inStream: InputStream,
		charset: String = "UTF-8",
	): List<String> {
		val result = mutableListOf<String>()

		val s = Scanner(inStream, charset)
		while (s.hasNextLine()) {
			val next = s.nextLine().trim { it <= ' ' }
			if (!next.isEmpty() && !next.startsWith("#")) {
				result.add(next)
			}
		}

		return result
	}
}
