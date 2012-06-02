/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.connector.activation;

import org.openecard.client.connector.Connector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.openecard.client.connector.handler.ConnectorHandler;


/**
 *
 * @author John
 */
public class ConnectorTest {

    public ConnectorTest() {
    }

    public static void main(String[] arg) {
	try {
	    Connector result = Connector.getInstance();
	} catch (Exception ex) {
	    Logger.getLogger(ConnectorTest.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testGetInstance() throws Exception {
	System.out.println("getInstance");
	Connector expResult = null;

//	assertEquals(expResult, result);
//	fail("The test case is a prototype.");
    }
}
