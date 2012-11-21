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

package org.openecard.clients.applet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.LogManager;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class LogProperties {

    private static final String dirName = "openecard";
    private static final String logConfName = "applet_log.properties";

    private static final String localAppData = System.getenv("LOCALAPPDATA");
    private static final String appData = System.getenv("LOCALAPPDATA");
    private static final String home = System.getProperty("user.home");

    private File actualPath;

    private LogProperties() { }

    /**
     *
     * @throws IOException If no config could be loaded.
     */
    public static void loadJavaUtilLogging() throws IOException {
	System.out.println("INFO: Loading java.util.logging configuration.");
	try {
	    LogProperties inst = new LogProperties();
	    Properties props = inst.getLocal();
	    LogManager manager = LogManager.getLogManager();
	    manager.readConfiguration(propertiesToStream(props));
	    System.out.println("INFO: Loading of java.util.logging configuration successful.");
	} catch (IOException ex) {
	    System.err.println("ERROR: Loading of java.util.logging configuration failed.");
	    throw ex;
	}
    }


    private static InputStream propertiesToStream(final Properties p) throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(baos);

	// do it by hand, else the default properties are omitted
	Enumeration<String> keys = (Enumeration<String>) p.propertyNames();
	while (keys.hasMoreElements()) {
	    String next = keys.nextElement();
	    String value = p.getProperty(next);
	    w.format("%s = %s%n", next, value);
	    w.flush();
	}

	return new ByteArrayInputStream(baos.toByteArray());
    }

    private static Properties getDefault() throws IOException {
	InputStream in = LogProperties.class.getResourceAsStream("/" + logConfName);
	if (in == null) {
	    in = LogProperties.class.getResourceAsStream(logConfName);
	}
	if (in == null) {
	    System.err.println("ERROR: No default log config found.");
	    throw new FileNotFoundException("Log config " + logConfName + " not found in path.");
	}
	// load props
	Properties p = new Properties();
	p.load(in);
	return p;
    }

    private Properties getLocal() throws IOException {
	InputStream in = null;
	Properties defaults = getDefault();

	// try windows vista style
	if (localAppData != null) {
	    String pathname = localAppData + File.separator + dirName;
	    if (makeDir(pathname)) {
		in = loadFile(pathname + File.separator + logConfName);
	    }
	}
	if (appData != null && in == null) {
	    String pathname = appData + File.separator + dirName;
	    if (makeDir(pathname)) {
		in = loadFile(pathname + File.separator + logConfName);
	    }
	}
	if (home != null && in == null) {
	    String pathname = home + File.separator + "." + dirName;
	    if (makeDir(pathname)) {
		in = loadFile(pathname + File.separator + logConfName);
	    }
	}

	if (in != null) {
	    Properties p = new Properties(defaults);
	    try {
		System.out.println("INFO: Loading config from path:\n  " + actualPath);
		p.load(in);
		correctFileHandlerPath(p);
		return p;
	    } catch (IOException ex) {
		System.err.println("WARNING: Failed to load and modify config from path:\n  " + actualPath);
	    }
	}
	return defaults;
    }

    private static boolean makeDir(String path) {
	File f = new File(path);

	if (f.exists()) {
	    return true;
	} else {
	    return f.mkdirs();
	}
    }

    /**
     * Return inputstream to given file, or null.
     * @param path
     * @return
     */
    private InputStream loadFile(String path) {
	try {
	    File f = new File(path);
	    if (f.isFile() && f.canRead()) {
		actualPath = f;
		FileInputStream fin = new FileInputStream(f);
		return fin;
	    }
	} catch (FileNotFoundException ex) {
	}
	return null;
    }

    private void correctFileHandlerPath(Properties p) throws IOException {
	String key = "java.util.logging.FileHandler.pattern";
	String value = p.getProperty(key);

	if (value != null) {
	    // %h == home, %t == temp
	    if (!value.startsWith("%h") && !value.startsWith("%t")) {
		File handlerPath = new File(value);
		if (!handlerPath.isAbsolute()) {
		    // this assumes, that the path should be relative to the conf directory
		    String base = actualPath.getParentFile().getCanonicalPath();
		    // java.util.logging.FileHandler.pattern expects / as pathname separator
		    value = (base + File.separator + value).replace("\\", "/");

		    // create non-existent directories
		    if (value.contains("/")) {
			int pos = value.lastIndexOf("/");
			String dir = value.substring(0, pos);
			makeDir(dir);
		    }

		    p.setProperty(key, value);
		}
	    } else {
		String var = null;
		if (value.startsWith("%h")) {
		    var = home;
		}
		if (value.startsWith("%t")) {
		    var = System.getProperty("java.io.tmpdir");
		}

		String temp = (var + value.substring(2)).replace("\\", "/");
		// create non-existent directories
		if (temp.contains("/")) {
		    int pos = temp.lastIndexOf("/");
		    String dir = temp.substring(0, pos);
		    makeDir(dir);
		}
	    }
	}
    }

}
