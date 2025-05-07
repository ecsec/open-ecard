/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
package org.openecard.common.util

import org.openecard.common.util.HttpRequestLineUtils.transformRaw
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Test for the HttpRequestLineUtils class.
 *
 * @author Hans-Martin Haase
 */
class HttpRequestLineUtilsTest {
    /**
     * Test to check whether the transformRaw method works correct in case there is just a key in the query.
     */
    @Test
    fun transformRawTestKeyNoValue() {
        val query = "testkey"
        val map = transformRaw(query)
        Assert.assertEquals(map.size, 1)
        for (key in map.keys) {
            Assert.assertEquals(key, "testkey")
            Assert.assertEquals(map[key], "")
        }
    }

    /**
     * Test to check whether the transformRaw method works correct in case there is a key with an empty value in the query.
     */
    @Test
    fun transformRawTestKeyEmptyValue() {
        val query = "testkey="
        val map = transformRaw(query)
        Assert.assertEquals(map.size, 1)
        for (key in map.keys) {
            Assert.assertEquals(key, "testkey")
            Assert.assertEquals(map[key], "")
        }
    }

    /**
     * Test to check whether the transformRaw method works correct in case there is a key with a value in the query.
     */
    @Test
    fun transformRawTestKeyWithValue() {
        val query = "testkey=blablub"
        val map = transformRaw(query)
        Assert.assertEquals(map.size, 1)
        for (key in map.keys) {
            Assert.assertEquals(key, "testkey")
            Assert.assertEquals(map[key], "blablub")
        }
    }

    /**
     * Test to check whether the transformRaw method works correct in case there is a key with a value while the value
     * contains an equal sign.
     */
    @Test
    fun transformRawTestKeyWithValueContainingEqualsSign() {
        val query = "testkey=blablub=test"
        val map = transformRaw(query)
        Assert.assertEquals(map.size, 1)
        for (key in map.keys) {
            Assert.assertEquals(key, "testkey")
            Assert.assertEquals(map[key], "blablub=test")
        }
    }
}
