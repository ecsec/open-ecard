/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import org.openecard.mdlw.sal.config.MiddlewareConfigLoader;
import org.openecard.mdlw.sal.config.MiddlewareSALConfig;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import org.testng.annotations.Test;


/**
 *
 * @author Mike Prechtl
 */
public class TestMiddlewareConfigLoader {

    @Test
    public void testMiddlewareConfigLoading() throws IOException, FileNotFoundException, JAXBException {
        List<MiddlewareSALConfig> mwConfigs = new MiddlewareConfigLoader().getMiddlewareSALConfigs();
        JAXB.marshal(mwConfigs.get(0).getMiddlewareSpec(), System.out);
    }

}
