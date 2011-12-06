package org.openecard.client.maven.classlist;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;


/**
 * Goal which appends an entry to a file when it is a jaxb class.
 *
 * @goal class-list
 * @phase process-classes
 */
public class ClassList extends AbstractMojo {

    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}/classes"
     */
    private File outputDirectory;
    /**
     * Name of the file.
     * @parameter expression="classes.lst"
     */
    private String fileName;

    /**
     * List of directories to look at.
     * @parameter default-value="${project.build.directory}/classes"
     */
    private File classDirectory;

    @Override
    public void execute() throws MojoExecutionException {
	File f = outputDirectory;

	if (!f.exists()) {
	    f.mkdirs();
	}
	File touch = new File(f, fileName);

	FileWriter w = null;
	try {
	    w = new FileWriter(touch, false);
	    PrintWriter pw = new PrintWriter(w);

	    // get list of all class files
	    for (String next : getClassFiles()) {
		pw.println(next);
	    }

	} catch (IOException e) {
	    throw new MojoExecutionException("Error creating file " + touch, e);
	} finally {
	    if (w != null) {
		try {
		    w.close();
		} catch (IOException e) {
		    // ignore
		}
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
		next = next.substring(0, next.length()-6);
		next = next.substring(classDirectory.getCanonicalPath().length()+1);
		next = next.replace(File.separator, ".");
		name = next;
	    }
	} catch (IOException ex) {
	    getLog().warn(ex);
	}
	return name;
    }

}
