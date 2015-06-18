/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.addon;

import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.addon.bind.AppExtensionActionProxy;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.AppPluginActionProxy;
import org.openecard.addon.ifd.IFDProtocol;
import org.openecard.addon.ifd.IFDProtocolProxy;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.sal.SALProtocol;
import org.openecard.addon.sal.SALProtocolProxy;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test the functionality of the Cache class.
 *
 * @author Hans-Martin Haase
 */
public class CacheTest {

    /**
     * Cache object which manages the data to cache.
     */
    Cache cache = new Cache();

    @Test
    public void addSALProtocol() {
	SALProtocolProxy proxy = new SALProtocolProxy(null, null);
	int initialHash = proxy.hashCode();
	AddonSpecification spec = new AddonSpecification();
	String id = "test";
	spec.setId(id);
	spec.setVersion("1.0.0");
	cache.addSALProtocol(spec, id, proxy);
	// now get it back from the cache
	SALProtocol proto = cache.getSALProtocol(spec, id);
	Assert.assertEquals(proto.hashCode(), initialHash);
    }

    @Test
    public void addIFDProtocol() {
	IFDProtocolProxy proxy = new IFDProtocolProxy(null, null);
	int initialHash = proxy.hashCode();
	AddonSpecification spec = new AddonSpecification();
	String id = "test";
	spec.setId(id);
	spec.setVersion("1.0.0");
	cache.addIFDProtocol(spec, id, proxy);
	// now get it back from the cache
	IFDProtocol proto = cache.getIFDProtocol(spec, id);
	Assert.assertEquals(proto.hashCode(), initialHash);
    }

    @Test
    public void addAppExtAction() {
	AppExtensionActionProxy proxy = new AppExtensionActionProxy(null, null);
	int initialHash = proxy.hashCode();
	AddonSpecification spec = new AddonSpecification();
	String id = "test";
	spec.setId(id);
	spec.setVersion("1.0.0");
	cache.addAppExtensionAction(spec, id, proxy);
	// now get it back from the cache
	AppExtensionAction proto = cache.getAppExtensionAction(spec, id);
	Assert.assertEquals(proto.hashCode(), initialHash);
    }

    @Test
    public void addAppPluginAction() {
	AppPluginActionProxy proxy = new AppPluginActionProxy(null, null);
	int initialHash = proxy.hashCode();
	AddonSpecification spec = new AddonSpecification();
	String id = "test";
	spec.setId(id);
	spec.setVersion("1.0.0");
	cache.addAppPluginAction(spec, id, proxy);
	// now get it back from the cache
	AppPluginAction proto = cache.getAppPluginAction(spec, id);
	Assert.assertEquals(proto.hashCode(), initialHash);
    }

    @Test
    public void addWithFilledCache() {
	SALProtocolProxy proxy4 = new SALProtocolProxy(null, null);
	AddonSpecification spec4 = new AddonSpecification();
	String id4 = "test4";
	spec4.setId(id4);
	spec4.setVersion("1.0.0");
	cache.addSALProtocol(spec4, id4, proxy4);
	IFDProtocolProxy proxy3 = new IFDProtocolProxy(null, null);
	AddonSpecification spec2 = new AddonSpecification();
	String id1 = "test1";
	spec2.setId(id1);
	spec2.setVersion("1.0.0");
	cache.addIFDProtocol(spec2, id1, proxy3);
	AppPluginActionProxy proxy = new AppPluginActionProxy(null, null);
	AddonSpecification spec1 = new AddonSpecification();
	String id0 = "test2";
	spec1.setId(id0);
	spec1.setVersion("1.0.0");
	cache.addAppPluginAction(spec1, id0, proxy);
	AppExtensionActionProxy proxy2 = new AppExtensionActionProxy(null, null);
	int initialHash = proxy2.hashCode();
	AddonSpecification spec = new AddonSpecification();
	String id = "test";
	spec.setId(id);
	spec.setVersion("1.0.0");
	cache.addAppExtensionAction(spec, id, proxy2);
	// now get it back from the cache
	AppExtensionAction proto = cache.getAppExtensionAction(spec, id);
	Assert.assertEquals(proto.hashCode(), initialHash);
    }

    @Test
    public void removeSingle() {
	AppPluginActionProxy proxy = new AppPluginActionProxy(null, null);
	AddonSpecification spec = new AddonSpecification();
	String id = "test";
	spec.setId(id);
	spec.setVersion("1.0.0");
	cache.addAppPluginAction(spec, id, proxy);
	// now get it back from the cache
	cache.removeCacheEntry(spec, id);
	AppPluginAction action = cache.getAppPluginAction(spec, id);
	Assert.assertNull(action);
    }

    @Test
    public void removeMulti() {
	SALProtocolProxy proxy4 = new SALProtocolProxy(null, null);
	AddonSpecification spec4 = new AddonSpecification();
	String id4 = "test4";
	spec4.setId(id4);
	spec4.setVersion("1.0.0");
	cache.addSALProtocol(spec4, id4, proxy4);
	IFDProtocolProxy proxy3 = new IFDProtocolProxy(null, null);
	AddonSpecification spec2 = new AddonSpecification();
	String id1 = "test1";
	spec2.setId(id1);
	spec2.setVersion("1.0.0");
	cache.addIFDProtocol(spec2, id1, proxy3);
	AppPluginActionProxy proxy = new AppPluginActionProxy(null, null);
	AddonSpecification spec1 = new AddonSpecification();
	String id0 = "test2";
	spec1.setId(id0);
	spec1.setVersion("1.0.0");
	cache.addAppPluginAction(spec1, id0, proxy);
	AppExtensionActionProxy proxy2 = new AppExtensionActionProxy(null, null);
	AddonSpecification spec = new AddonSpecification();
	String id = "test";
	spec.setId(id);
	spec.setVersion("1.0.0");
	cache.addAppExtensionAction(spec, id, proxy2);
	cache.removeCacheEntry(spec, id);
	cache.removeCacheEntry(spec1, id0);

	// now get it back from the cache
	AppExtensionAction proto = cache.getAppExtensionAction(spec, id);
	AppPluginAction action = cache.getAppPluginAction(spec1, id0);
	Assert.assertNull(proto);
	Assert.assertNull(action);
    }

    @Test
    public void wrongGetOperation() {
	AppPluginActionProxy proxy = new AppPluginActionProxy(null, null);
	AddonSpecification spec = new AddonSpecification();
	String id = "test";
	spec.setId(id);
	spec.setVersion("1.0.0");
	cache.addAppPluginAction(spec, id, proxy);
	// now get it back from the cache
	AppExtensionAction proto = cache.getAppExtensionAction(spec, id);
	Assert.assertNull(proto);
    }
}
