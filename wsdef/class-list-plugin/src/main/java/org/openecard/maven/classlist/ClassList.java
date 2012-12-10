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

package org.openecard.maven.classlist;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;


/**
 * Implementation of the {@code class-list} goal.
 * The plugin creates a file named in the {@code fileName} parameter and places it into the the directory named by the
 * parameter {@code outputDirectory}. The contents of the file are entries of fully qualified class names with all JAXB
 * element classes found below the directory named by the {@code classDirectory} parameter. The {@code excludes} list
 * can contain fully qualified class names which should not occur in the list.
 * <p>
 * The plugin is executed in the {@code process-classes} phase. The execution goal is named {@code class-list}.
 *
 * @goal class-list
 * @phase process-classes
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ClassList extends AbstractMojo {

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}/classes"
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
			justification = "Written by maven plugin.")
    private File outputDirectory;
    /**
     * Name of the file.
     *
     * @parameter expression="classes.lst"
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
			justification = "Written by maven plugin.")
    private String fileName;
    /**
     * List of excluded classes.
     *
     * @parameter
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
			justification = "Written by maven plugin.")
    private List excludes;

    /**
     * List of directories to look at.
     * 
     * @parameter default-value="${project.build.directory}/classes"
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
			justification = "Written by maven plugin.")
    private File classDirectory;

    @Override
    public void execute() throws MojoExecutionException {
	File f = outputDirectory;

	if (! f.exists()) {
	    boolean created = f.mkdirs();
	    if (! created) {
		throw new MojoExecutionException("Directory could not be created: " + outputDirectory);
	    }
	}
	File touch = new File(f, fileName);

	PrintWriter pw = null;
	try {
	    pw = new PrintWriter(touch, "UTF-8");

	    // get list of all class files
	    for (String next : getClassFiles()) {
		pw.println(next);
	    }

	} catch (IOException e) {
	    throw new MojoExecutionException("Error creating file " + touch, e);
	} finally {
	    if (pw != null) {
		pw.close();
	    }
	}
    }

    private Set<String> getClassFiles() {
	Set<String> result = new TreeSet<String>();

	getLog().info("Checking dir: " + classDirectory.getPath());
	result.addAll(getClassFiles(classDirectory));

	return result;
    }

    private Set<String> getClassFiles(File dir) {
	Set<String> result = new TreeSet<String>();

	File[] files = dir.listFiles();

	for (File f : files) {
	    if (f.isDirectory()) {
		result.addAll(getClassFiles(f));
	    } else { // is a file
		String className = getClassName(f);
		if (className != null) {
		    result.add(className);
		}
	    }
	}

	return result;
    }

    private String getClassName(File classFile) {
	String name = null;
	try {
	    String next = classFile.getCanonicalPath();
	    if (next.endsWith(".class")) {
		next = next.substring(0, next.length() - 6);
		next = next.substring(classDirectory.getCanonicalPath().length() + 1);
		next = next.replace(File.separator, ".");
		// consult excludes list
		if (excludes.contains(next)) {
		    getLog().info("Excluding class: " + next);
		} else {
		    getLog().debug("Adding class to list: " + next);
		    name = next;
		}
	    }
	} catch (IOException ex) {
	    getLog().warn(ex);
	}
	return name;
    }

}
