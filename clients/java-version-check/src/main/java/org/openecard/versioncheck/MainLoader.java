/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

package org.openecard.versioncheck;

import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import java.util.Scanner;
import javax.swing.JOptionPane;


/**
 *
 * @author Tobias Wich
 */
public class MainLoader {

    private static final float VERSION_REFERENCE_VAL = 1.7f;

    public static void main(String[] args) throws IllegalArgumentException, InvocationTargetException,
            ClassNotFoundException, NoSuchMethodException {
	// check if java version is sufficient
	if (! checkJavaVersion(VERSION_REFERENCE_VAL)) {
	    String msg = getBundle().getString("MainLoader.invalid_java_version");
	    String title = getBundle().getString("MainLoader.invalid_java_version.title");
	    msg(title, msg, System.getProperty("java.version"));
	    System.exit(1);
	}

	// get main method
	Method m = null;
	try {
	    String className = getMainClassName();
	    Class<?> clazz = getMainClass(className);
	    m = getMainMethod(clazz);
	} catch (ClassNotFoundException ex) {
	    System.err.println("Main class not found.");
	} catch (NoSuchMethodException ex) {
	    System.err.println("Main method not found.");
	}

	// do we have a function?
	if (m == null) {
	    String msg = getBundle().getString("MainLoader.no_method");
	    String title = getBundle().getString("MainLoader.no_method.title");
	    msg(title, msg);
	    System.exit(1);
	}
	try {
	    m.invoke(null, (Object) args);
	} catch (IllegalArgumentException ex) {
	    String msg = getBundle().getString("MainLoader.wrong_args");
	    String title = getBundle().getString("MainLoader.wrong_args.title");
	    msg(title, msg);
	    System.exit(1);
	} catch (IllegalAccessException ex) {
	    String msg = getBundle().getString("MainLoader.missing_rights");
	    String title = getBundle().getString("MainLoader.missing_rights");
	    msg(title, msg);
	    System.exit(1);
	} catch (InvocationTargetException ex) {
	    String msg = getBundle().getString("MainLoader.unhandled_error");
	    String title = getBundle().getString("MainLoader.unhandled_error");
	    msg(title, msg);
	    System.exit(1);
	}
    }

    private static ResourceBundle getBundle() {
	return ResourceBundle.getBundle("org.openecard.versioncheck.Bundle");
    }

    private static String getMainClassName() throws ClassNotFoundException {
	// try to read main class as property
	String mainClass = System.getProperty("openecard.mainclass");
	if (mainClass != null) {
	    mainClass = mainClass.trim();
	    System.out.format("Main class defined in system property.%nmainclass=%s%n", mainClass);
	    return mainClass;
	}

	// try to read main class from config file
	String confFileName = "openecard/mainclass";
	InputStream is = MainLoader.class.getClassLoader().getResourceAsStream(confFileName);
	if (is == null) {
	    is = MainLoader.class.getClassLoader().getResourceAsStream("/" + confFileName);
	}
	if (is != null) {
	    Scanner s = new Scanner(is, "UTF-8");
	    mainClass = s.nextLine();
	    mainClass = mainClass.trim();
	    System.out.format("Main class defined in config file.%nmainclass=%s%n", mainClass);
	    return mainClass;
	}

	throw new ClassNotFoundException("No main class defined for deferred loading.");
    }

    private static Class<?> getMainClass(String className) throws ClassNotFoundException {
	return MainLoader.class.getClassLoader().loadClass(className);
    }

    private static Method getMainMethod(Class<?> clazz) throws NoSuchMethodException {
	for (Method m : clazz.getMethods()) {
	    if ("main".equals(m.getName())) {
		int mod = m.getModifiers();
		if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
		    Class<?> returnType = m.getReturnType();
		    // some JVMs may use Void.class, some may use Void.TYPE, so accept both
		    if (returnType.equals(Void.TYPE) || returnType.equals(Void.class)) {
			Class<?>[] params = m.getParameterTypes();
			if (params.length == 1 && params[0].equals(String[].class)) {
			    return m;
			}
		    }
		}
	    }
	}
	// no matching method found
	throw new NoSuchMethodException("Failed to find main method.");
    }

    public static boolean checkJavaVersion(float versionReference) {
	String fullVersion = System.getProperty("java.version");
	String[] javaVersionElements = fullVersion.split("\\.|_|-b|\\+");
	String major1 = javaVersionElements[0];
	String major2 = javaVersionElements.length < 2 ? "0" : javaVersionElements[1];
	float javaVersion = Float.valueOf(major1 + "." + major2 + "f");
	// check if version is bigger or equal than reference value
	return javaVersion >= versionReference;
    }

    private static void msg(String title, String msg, Object... obj) {
	String evalMsg = String.format(msg, obj);
	if (GraphicsEnvironment.isHeadless()) {
	    System.err.println(evalMsg);
	} else {
	    JOptionPane.showMessageDialog(null, evalMsg, title, JOptionPane.ERROR_MESSAGE);
	}
    }

}
