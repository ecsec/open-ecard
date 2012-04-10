/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.sal.protocol.pincompare;

import iso.std.iso_iec._24727.tech.schema.Encipher;
import iso.std.iso_iec._24727.tech.schema.EncipherResponse;
import java.util.Map;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class EnchipherStep implements ProtocolStep<Encipher, EncipherResponse> {

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.Encipher;
    }

    @Override
    public EncipherResponse perform(Encipher request, Map internalData) {
	return WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION, "The function '" + getFunctionType() + "' is not supported with this protocol."));
    }

}
