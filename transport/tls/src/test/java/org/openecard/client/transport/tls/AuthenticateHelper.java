package org.openecard.client.transport.tls;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.state.CardStateEntry;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AuthenticateHelper {

    public static void authenticateApplication(CardStateEntry entry, Enum<?> action, Dispatcher dispatcher) throws Exception {
	SecurityConditionType securityCondition = entry.getCurrentCardApplication().getSecurityCondition(action);
	authenticate(entry.handleCopy(), dispatcher, securityCondition);
    }

    public static void authenticateDID(CardStateEntry entry, String didName, Enum<?> action, Dispatcher dispatcher) throws Exception {
	SecurityConditionType securityCondition = entry.getCurrentCardApplication().getDIDInfo(didName).getSecurityCondition(action);
	authenticate(entry.handleCopy(), dispatcher, securityCondition);
    }

    public static void authenticateDataSet(CardStateEntry entry, String dataSetName, Enum<?> action, Dispatcher dispatcher) throws Exception {
	SecurityConditionType securityCondition = entry.getCurrentCardApplication().getDataSetInfo(dataSetName).getSecurityCondition(action);
	authenticate(entry.handleCopy(), dispatcher, securityCondition);
    }

    private static boolean authenticate(ConnectionHandleType connectionHandle, Dispatcher dispatcher, SecurityConditionType securityCondition) {
	try {
	    String didName = null;
	    DIDScopeType didScope = DIDScopeType.GLOBAL;

	    if (securityCondition.isAlways() == null) {
		didName = securityCondition.getDIDAuthentication().getDIDName();
		if (securityCondition.getDIDAuthentication().getDIDScope() != null) {
		    didScope = securityCondition.getDIDAuthentication().getDIDScope();
		}
		// else default is global
	    } else {
		return true;
	    }

	    DIDGet didGet = new DIDGet();
	    didGet.setConnectionHandle(connectionHandle);
	    didGet.setDIDScope(didScope);
	    didGet.setDIDName(didName);
	    DIDGetResponse didGetResponse = (DIDGetResponse) dispatcher.deliver(didGet);
	    WSHelper.checkResult(didGetResponse);

	    DIDAuthenticate didAuthenticate = new DIDAuthenticate();
	    didAuthenticate.setConnectionHandle(connectionHandle);
	    didAuthenticate.setDIDName(didName);
	    didAuthenticate.setDIDScope(didScope);
	    didAuthenticate.setAuthenticationProtocolData(new DIDAuthenticationDataType());
	    didAuthenticate.getAuthenticationProtocolData().setProtocol(didGetResponse.getDIDStructure().getDIDMarker().getProtocol());
	    DIDAuthenticateResponse didAuthenticateResponse = (DIDAuthenticateResponse) dispatcher.deliver(didAuthenticate);
	    WSHelper.checkResult(didAuthenticateResponse);
	    return true;
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }

}
