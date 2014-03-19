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

package org.openecard.common.apdu.common;

import iso.std.iso_iec._24727.tech.schema.CardCallTemplateType;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CardCommandTemplateTest {

    private static final Map<String, Object> EMPTY_CTX = Collections.emptyMap();
    private Map<String, Object> CTX;

    @BeforeClass
    public void init() {
	CTX = new HashMap<String, Object>();
	CTX.put("val1", "00ff");
	CTX.put("val2", "1234");
	CTX.put("tlv", new TLVFunction());
    }

    @Test
    public void testNoExp() throws APDUTemplateException {
	CardCallTemplateType templateType = new CardCallTemplateType();
	templateType.setHeaderTemplate("00a4020c");
	CardCommandTemplate t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(EMPTY_CTX).toHexString(), "00A4020C");

	templateType.setDataTemplate("00ff");
	t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(EMPTY_CTX).toHexString(), "00A4020C0200FF");

	templateType.setExpectedLength(BigInteger.valueOf(0xff));
	t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(EMPTY_CTX).toHexString(), "00A4020C0200FFFF");
    }

    @Test
    public void testSymbolExp() throws APDUTemplateException {
	CardCallTemplateType templateType = new CardCallTemplateType();
	templateType.setHeaderTemplate("00a4020c");
	templateType.setDataTemplate("{val1}");
	CardCommandTemplate t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(CTX).toHexString(), "00A4020C0200FF");

	templateType.setDataTemplate("ab{val1}");
	t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(CTX).toHexString(), "00A4020C03AB00FF");

	templateType.setDataTemplate("{val1}ab");
	t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(CTX).toHexString(), "00A4020C0300FFAB");

	templateType.setDataTemplate("ba{val1}ab");
	t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(CTX).toHexString(), "00A4020C04BA00FFAB");

	templateType.setDataTemplate("ba{val1}ab{val2}cd");
	t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(CTX).toHexString(), "00A4020C07BA00FFAB1234CD");

	templateType.setDataTemplate("ba{val1}{val2}cd");
	t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(CTX).toHexString(), "00A4020C06BA00FF1234CD");
    }

    @Test
    public void testSymbolFun() throws APDUTemplateException {
	CardCallTemplateType templateType = new CardCallTemplateType();
	templateType.setHeaderTemplate("00a4020c");
	templateType.setDataTemplate("{tlv 0x01 val1}");
	CardCommandTemplate t = new CardCommandTemplate(templateType);
	assertEquals(t.evaluate(CTX).toHexString(), "00A4020C04010200FF");
    }

}
