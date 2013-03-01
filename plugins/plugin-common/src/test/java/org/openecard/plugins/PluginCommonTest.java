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

package org.openecard.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.Policy;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.gui.UserConsent;
import org.openecard.plugins.wrapper.PluginDispatcher;
import org.openecard.plugins.wrapper.PluginUserConsent;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests for the common plugin classes.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PluginCommonTest {

    private static final String TESTPLUGIN_MAIN_CLASS = "org.openecard.plugins.testplugin.TestPlugin";
    private static final String RESOURCE_TEST_PLUGIN_JAR = "TestPlugin.jar";
    private static final int INDEX_GOOD_ACTION = 0;
    private static final int INDEX_REFLECTION_ACTION = 1;
    private static final int INDEX_CLASSLOADER_ACTION = 2;
    private static final int INDEX_EXISTING_CLASS_ACTION = 3;

    private TestClient client;

    /**
     * Sets up the Client needed for testing.
     */
    @BeforeClass
    public void setup() {
	client = new TestClient();
	try {
	    // give the testclient some time to initialize
	    Thread.sleep(2000);
	} catch (InterruptedException ignore) { }
    }

    /**
     * This test checks if the PluginPolicy is successfully blocking the reflection in the evil action but let the good
     * action pass.
     * 
     * @throws ClassNotFoundException if the plugin main class couldn't be found.
     * @throws InstantiationException if the plugin main class couldn't be instantiated.
     * @throws IllegalAccessException if the plugin class or its nullary constructor is not accessible.
     * @throws InvocationTargetException in case a dispatched method in an action throws an exception.
     * @throws DispatcherException in case a reflection error in the dispatcher occurs.
     * @throws NoSuchMethodException if we coudn't get the findloadedclass method of the classloader by reflection.
     */
    @Test
    public void testPluginPolicy() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException, DispatcherException, NoSuchMethodException {
	// get the path to the test plugin jar
	URL url = getClass().getClassLoader().getResource(RESOURCE_TEST_PLUGIN_JAR);
	String path = url.getPath();

	// set PluginPolicy with the given path
	PluginPolicy policy = new PluginPolicy(path);
	Policy.setPolicy(policy);
	System.setSecurityManager(new SecurityManager());

	// load plugin main class
	ClassLoader cl = new URLClassLoader(new URL[]{url});
	Class<?> cls = cl.loadClass(TESTPLUGIN_MAIN_CLASS);
	PluginInterface plugin = (PluginInterface) cls.newInstance();

	Assert.assertEquals(plugin.getName(), plugin.toString());

	Dispatcher dispatcher = client.getDispatcher();
	UserConsent gui = client.getGUI();
	plugin.initialize(new PluginDispatcher(dispatcher), new PluginUserConsent(gui), null, null);

	goodActionTest(plugin);
	reflectionActionTest(plugin);
	classloaderActionTest(plugin);
	existingClassActionTest(plugin);
    }

    private void existingClassActionTest(PluginInterface plugin) throws NoSuchMethodException, IllegalAccessException,
	    InvocationTargetException, DispatcherException, InstantiationException {
	Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[] { String.class });
	m.setAccessible(true);
	ClassLoader systemCL = ClassLoader.getSystemClassLoader();

	// ensure class is not already loaded
	Class<?> clazz = (Class<?>) m.invoke(systemCL, "org.openecard.common.tlv.TLV");
	boolean classIsLoaded = (clazz != null);
	Assert.assertFalse(classIsLoaded);

	// perform the action loading the class
	PluginAction action = plugin.getActions().get(INDEX_EXISTING_CLASS_ACTION);
	action.perform();

	// ensure class is now loaded
	clazz = (Class<?>) m.invoke(systemCL, "org.openecard.common.tlv.TLV");
	classIsLoaded = (clazz != null);
	Assert.assertTrue(classIsLoaded);

	// ensure it is not the class from the jar
	String classPath = clazz.newInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	boolean isFromJar = classPath.contains(RESOURCE_TEST_PLUGIN_JAR);
	Assert.assertFalse(isFromJar);
    }

    private void goodActionTest(PluginInterface plugin) throws DispatcherException, InvocationTargetException {
	// test the 'good' action
	PluginAction action = plugin.getActions().get(INDEX_GOOD_ACTION);
	action.perform();
	// ok if we come here
    }

    private void classloaderActionTest(PluginInterface plugin) throws DispatcherException,
	    InvocationTargetException {
	// test the action trying to create a classloader
	PluginAction action = plugin.getActions().get(INDEX_CLASSLOADER_ACTION);
	try {
	    action.perform();
	    Assert.fail("An AccessControlException should have been thrown.");
	} catch (AccessControlException e) {
	    // expected
	}
    }

    private void reflectionActionTest(PluginInterface plugin) throws DispatcherException, InvocationTargetException {
	// test the action using reflections
	PluginAction action = plugin.getActions().get(INDEX_REFLECTION_ACTION);
	try {
	    action.perform();
	    Assert.fail("An AccessControlException should have been thrown.");
	} catch (AccessControlException e) {
	    // expected
	}
    }

}
