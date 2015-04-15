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
 ***************************************************************************/

package org.openecard.crypto.tls.verify;

import java.util.regex.Pattern;
import org.openecard.crypto.tls.proxy.ProxySettings;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Tobias Wich
 */
public class HostTest {

    @Test
    public void testPatternCreation() {
	Assert.assertEquals(ProxySettings.parseExclusionHosts("").size(), 0);
	Assert.assertEquals(ProxySettings.parseExclusionHosts("foo;").size(), 1);
	Assert.assertEquals(ProxySettings.parseExclusionHosts("foo;bar;").size(), 2);
	Assert.assertEquals(ProxySettings.parseExclusionHosts("foo;bar;;").size(), 2);
	Assert.assertEquals(ProxySettings.parseExclusionHosts(";foo;bar;").size(), 2);
    }

    @Test
    public void testPatternMatch() {
	Pattern p = ProxySettings.parseExclusionHosts("*.example.com").get(0);
	Assert.assertTrue(p.matcher("foo.example.com").matches());
	Assert.assertTrue(p.matcher("foo.example.com:80").matches());
	Assert.assertFalse(p.matcher("example.com").matches());
	Assert.assertFalse(p.matcher("example.com:80").matches());
	p = ProxySettings.parseExclusionHosts("*.example.com:80").get(0);
	Assert.assertFalse(p.matcher("foo.example.com").matches());
	Assert.assertFalse(p.matcher("foo.example.com:443").matches());
	Assert.assertTrue(p.matcher("foo.example.com:80").matches());
	p = ProxySettings.parseExclusionHosts("*.example.com:*").get(0);
	Assert.assertTrue(p.matcher("foo.example.com:443").matches());
	Assert.assertTrue(p.matcher("foo.example.com:80").matches());
	p = ProxySettings.parseExclusionHosts("*").get(0);
	Assert.assertTrue(p.matcher("foo.example.com:443").matches());
    }

}
