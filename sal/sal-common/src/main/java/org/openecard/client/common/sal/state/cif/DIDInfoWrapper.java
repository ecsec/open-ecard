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

package org.openecard.client.common.sal.state.cif;

import iso.std.iso_iec._24727.tech.schema.AccessRuleType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DIDInfoWrapper {

    private DIDInfoType didInfo;
    private Map<Enum<?>, SecurityConditionType> securityConditions = new HashMap<Enum<?>, SecurityConditionType>();

    public  DIDInfoWrapper(DIDInfoType didInfo) {
	this.didInfo = didInfo;
    }

    public DIDInfoType getDIDInfo() {
	return didInfo;
    }

    public SecurityConditionType getSecurityCondition(Enum<?> serviceAction) {
	if(securityConditions.isEmpty()){
	    for(AccessRuleType accessRule : this.didInfo.getDIDACL().getAccessRule()){
		if (accessRule.getAction().getConnectionServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getConnectionServiceAction(), accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getAuthorizationServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getAuthorizationServiceAction(), accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getDifferentialIdentityServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getDifferentialIdentityServiceAction(), accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getNamedDataServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getNamedDataServiceAction(), accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getCryptographicServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getCryptographicServiceAction(), accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getCardApplicationServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getCardApplicationServiceAction(), accessRule.getSecurityCondition());
		}
	    }
	}
	return securityConditions.get(serviceAction);
    }

}
