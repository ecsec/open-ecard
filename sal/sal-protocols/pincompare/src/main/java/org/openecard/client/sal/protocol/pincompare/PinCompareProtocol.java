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

import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.FunctionType;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PinCompareProtocol extends org.openecard.client.common.sal.Protocol {

    public PinCompareProtocol(Dispatcher dispatcher) {
	this.steps.add(new DIDAuthenticateStep(dispatcher));
	this.steps.add(new EnchipherStep());
	this.steps.add(new DIDGetStep());
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
	return "PinCompare";
    }

}
