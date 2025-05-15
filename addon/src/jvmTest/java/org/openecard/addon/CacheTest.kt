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
 */
package org.openecard.addon

import org.openecard.addon.bind.AppExtensionActionProxy
import org.openecard.addon.bind.AppPluginActionProxy
import org.openecard.addon.ifd.IFDProtocolProxy
import org.openecard.addon.manifest.AddonSpecification
import org.openecard.addon.sal.SALProtocolProxy
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Test the functionality of the Cache class.
 *
 * @author Hans-Martin Haase
 */
class CacheTest {
	/**
	 * Cache object which manages the data to cache.
	 */
	var cache: Cache = Cache()

	var dummyClassName: String = "dummyClass"

	@Test
	fun addSALProtocol() {
		val proxy = SALProtocolProxy(dummyClassName, javaClass.getClassLoader())
		val initialHash = proxy.hashCode()
		val spec = AddonSpecification()
		val id = "test"
		spec.setId(id)
		spec.setVersion("1.0.0")
		cache.addSALProtocol(spec, id, proxy)
		// now get it back from the cache
		val proto = cache.getSALProtocol(spec, id)
		Assert.assertEquals(proto.hashCode(), initialHash)
	}

	@Test
	fun addIFDProtocol() {
		val proxy = IFDProtocolProxy(dummyClassName, javaClass.getClassLoader())
		val initialHash = proxy.hashCode()
		val spec = AddonSpecification()
		val id = "test"
		spec.setId(id)
		spec.setVersion("1.0.0")
		cache.addIFDProtocol(spec, id, proxy)
		// now get it back from the cache
		val proto = cache.getIFDProtocol(spec, id)
		Assert.assertEquals(proto.hashCode(), initialHash)
	}

	@Test
	fun addAppExtAction() {
		val proxy = AppExtensionActionProxy(dummyClassName, javaClass.getClassLoader())
		val initialHash = proxy.hashCode()
		val spec = AddonSpecification()
		val id = "test"
		spec.setId(id)
		spec.setVersion("1.0.0")
		cache.addAppExtensionAction(spec, id, proxy)
		// now get it back from the cache
		val proto = cache.getAppExtensionAction(spec, id)
		Assert.assertEquals(proto.hashCode(), initialHash)
	}

	@Test
	fun addAppPluginAction() {
		val proxy = AppPluginActionProxy(dummyClassName, javaClass.getClassLoader())
		val initialHash = proxy.hashCode()
		val spec = AddonSpecification()
		val id = "test"
		spec.setId(id)
		spec.setVersion("1.0.0")
		cache.addAppPluginAction(spec, id, proxy)
		// now get it back from the cache
		val proto = cache.getAppPluginAction(spec, id)
		Assert.assertEquals(proto.hashCode(), initialHash)
	}

	@Test
	fun addWithFilledCache() {
		val proxy4 = SALProtocolProxy(dummyClassName, javaClass.getClassLoader())
		val spec4 = AddonSpecification()
		val id4 = "test4"
		spec4.setId(id4)
		spec4.setVersion("1.0.0")
		cache.addSALProtocol(spec4, id4, proxy4)
		val proxy3 = IFDProtocolProxy(dummyClassName, javaClass.getClassLoader())
		val spec2 = AddonSpecification()
		val id1 = "test1"
		spec2.setId(id1)
		spec2.setVersion("1.0.0")
		cache.addIFDProtocol(spec2, id1, proxy3)
		val proxy = AppPluginActionProxy(dummyClassName, javaClass.getClassLoader())
		val spec1 = AddonSpecification()
		val id0 = "test2"
		spec1.setId(id0)
		spec1.setVersion("1.0.0")
		cache.addAppPluginAction(spec1, id0, proxy)
		val proxy2 = AppExtensionActionProxy(dummyClassName, javaClass.getClassLoader())
		val initialHash = proxy2.hashCode()
		val spec = AddonSpecification()
		val id = "test"
		spec.setId(id)
		spec.setVersion("1.0.0")
		cache.addAppExtensionAction(spec, id, proxy2)
		// now get it back from the cache
		val proto = cache.getAppExtensionAction(spec, id)
		Assert.assertEquals(proto.hashCode(), initialHash)
	}

	@Test
	fun removeSingle() {
		val proxy = AppPluginActionProxy(dummyClassName, javaClass.getClassLoader())
		val spec = AddonSpecification()
		val id = "test"
		spec.setId(id)
		spec.setVersion("1.0.0")
		cache.addAppPluginAction(spec, id, proxy)
		// now get it back from the cache
		cache.removeCacheEntry(spec, id)
		val action = cache.getAppPluginAction(spec, id)
		Assert.assertNull(action)
	}

	@Test
	fun removeMulti() {
		val proxy4 = SALProtocolProxy(dummyClassName, javaClass.getClassLoader())
		val spec4 = AddonSpecification()
		val id4 = "test4"
		spec4.setId(id4)
		spec4.setVersion("1.0.0")
		cache.addSALProtocol(spec4, id4, proxy4)
		val proxy3 = IFDProtocolProxy(dummyClassName, javaClass.getClassLoader())
		val spec2 = AddonSpecification()
		val id1 = "test1"
		spec2.setId(id1)
		spec2.setVersion("1.0.0")
		cache.addIFDProtocol(spec2, id1, proxy3)
		val proxy = AppPluginActionProxy(dummyClassName, javaClass.getClassLoader())
		val spec1 = AddonSpecification()
		val id0 = "test2"
		spec1.setId(id0)
		spec1.setVersion("1.0.0")
		cache.addAppPluginAction(spec1, id0, proxy)
		val proxy2 = AppExtensionActionProxy(dummyClassName, javaClass.getClassLoader())
		val spec = AddonSpecification()
		val id = "test"
		spec.setId(id)
		spec.setVersion("1.0.0")
		cache.addAppExtensionAction(spec, id, proxy2)
		cache.removeCacheEntry(spec, id)
		cache.removeCacheEntry(spec1, id0)

		// now get it back from the cache
		val proto = cache.getAppExtensionAction(spec, id)
		val action = cache.getAppPluginAction(spec1, id0)
		Assert.assertNull(proto)
		Assert.assertNull(action)
	}

	@Test
	fun wrongGetOperation() {
		val proxy = AppPluginActionProxy(dummyClassName, javaClass.getClassLoader())
		val spec = AddonSpecification()
		val id = "test"
		spec.setId(id)
		spec.setVersion("1.0.0")
		cache.addAppPluginAction(spec, id, proxy)
		// now get it back from the cache
		val proto = cache.getAppExtensionAction(spec, id)
		Assert.assertNull(proto)
	}
}
