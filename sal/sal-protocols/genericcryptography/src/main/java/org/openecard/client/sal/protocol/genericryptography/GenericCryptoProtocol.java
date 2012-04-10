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

package org.openecard.client.sal.protocol.genericryptography;

import org.openecard.client.common.sal.FunctionType;
import org.openecard.ws.IFD;
import org.openecard.ws.SAL;


public class GenericCryptoProtocol extends org.openecard.client.common.sal.Protocol {

    public GenericCryptoProtocol(SAL sal, IFD ifd) {
	this.steps.add(new SignStep(sal, ifd));
	this.steps.add(new DIDGetStep(sal));
    }

    @Override
    public boolean hasNextStep(FunctionType functionName) {
	for(int i = 0;i<steps.size();i++){
	    if(steps.get(i).getFunctionType().equals(functionName)){
		super.curStep = i;
	    }
	}
	return true;
    }

    @Override
    public String toString() {
	return "Generic cryptography";
    }

}
