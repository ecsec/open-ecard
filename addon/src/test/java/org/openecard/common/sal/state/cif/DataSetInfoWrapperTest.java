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

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.NamedDataServiceActionName;
import org.openecard.common.ClientEnv;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.util.StringUtils;
import org.openecard.recognition.CardRecognitionImpl;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Dirk Petrautzki
 */
public class DataSetInfoWrapperTest {

    private static final byte[] rootApplication = StringUtils.toByteArray("3F00");

    /**
     * Simple test for DataSetInfoWrapper-class. After getting the DataSetInfoWrapper for the EF.DIR data set in the
     * root applicaton of the npa we check if the get-methods return the expected values.
     *
     * @throws Exception when something in this test went unexpectedly wrong
     */
    @Test
    public void test() throws Exception {
	Environment env = new ClientEnv();
	CardRecognitionImpl recognition = new CardRecognitionImpl(env);
	CardInfoType cardInfo = recognition.getCardInfo("http://bsi.bund.de/cif/npa.xml");
	CardInfoWrapper cardInfoWrapper = new CardInfoWrapper(cardInfo, null);

	CardApplicationWrapper cardApplicationWrapper = cardInfoWrapper.getCardApplication(rootApplication);

	DataSetInfoWrapper dataSetInfoWrapper = cardApplicationWrapper.getDataSetInfo("EF.DIR");
	assertSame(dataSetInfoWrapper, cardApplicationWrapper.getDataSetInfo("EF.DIR"));
	assertNotNull(dataSetInfoWrapper.getDataSetInfo());
	assertNotNull(dataSetInfoWrapper.getSecurityCondition(NamedDataServiceActionName.DSI_READ));
    }

}
