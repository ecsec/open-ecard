/****************************************************************************
 * Copyright (C) 2012-2017 HS Coburg.
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

package org.openecard.common.sal.state.cif;

import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import org.openecard.common.util.StringUtils;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Dirk Petrautzki
 */
public class CardApplicationWrapperTest {

    private static final byte[] rootApplication = StringUtils.toByteArray("3F00");

    /**
     * Simple test for CardApplicationWrapper-class. After getting the CardApplicationWrapper for the root application
     * we check if the get-methods return the expected values.
     *
     * @throws Exception when something in this test went unexpectedly wrong
     */
    @Test
    public void test() throws Exception {
	CardInfoType cardInfo = new CifLoader().getNpaCif();
	CardInfoWrapper cardInfoWrapper = new CardInfoWrapper(cardInfo, null);

	CardApplicationWrapper cardAppWrapper = cardInfoWrapper.getCardApplication(rootApplication);

	assertEquals(cardAppWrapper.getApplicationIdentifier(), rootApplication);
	assertEquals(cardAppWrapper.getDataSetNameList().getDataSetName().size(), 5);
	assertEquals(cardAppWrapper.getDIDInfoList().size(), 9);
	assertEquals(cardAppWrapper.getCardApplicationACL().getAccessRule().size(), 40);
	assertNotNull(cardAppWrapper.getSecurityCondition(CardApplicationServiceActionName.CARD_APPLICATION_LIST));

    }

}
