/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl.fs;

import skid.mob.impl.client.JsonConfig;
import skid.mob.impl.client.UnknownInfrastructure;
import com.jayway.jsonpath.JsonPath;
import skid.mob.impl.client.ServerError;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import org.json.JSONException;
import org.openecard.common.util.FileUtils;
import org.openecard.mobile.activation.ActivationSource;
import skid.mob.impl.client.InvalidServerData;
import skid.mob.impl.client.NetworkError;
import skid.mob.lib.Cancellable;
import skid.mob.lib.SamlClient;
import skid.mob.lib.SkidErrorCodes;
import static skid.mob.impl.ThreadUtils.ifNotInterrupted;
import skid.mob.lib.ProcessFailedCallback;
import skid.mob.lib.FsSessionCallback;


/**
 *
 * @author Tobias Wich
 */
public class SamlClientImpl implements SamlClient {

    static {
	JsonConfig.assertInitialized();
    }

    private final ActivationSource oecActivationSource;

    public SamlClientImpl(ActivationSource oecActivationSource) {
	this.oecActivationSource = oecActivationSource;
    }

    @Override
    public Cancellable startSession(String startUrl, FsSessionCallback initCb, ProcessFailedCallback failCb) {
	Runnable r = () -> {
	    try {
		AuthReqResp samlFsResp = authnReq(startUrl);
		FsSessionImpl fsSess = new FsSessionImpl(oecActivationSource, samlFsResp.fsSessionId, samlFsResp.skidBaseUri);
		fsSess.load();
		// signal success
		ifNotInterrupted(() -> initCb.done(fsSess));
	    } catch (MalformedURLException | ClassCastException ex) {
		ifNotInterrupted(() -> failCb.processFailed(SkidErrorCodes.INVALID_INPUT, ex.getMessage()));
	    } catch (ServerError ex) {
		// TODO: error handling for unknown SP and all other cases
		ifNotInterrupted(() -> failCb.processFailed(SkidErrorCodes.SERVER_ERROR, ex.getMessage()));
	    } catch (InvalidServerData ex) {
		ifNotInterrupted(() -> failCb.processFailed(SkidErrorCodes.SERVER_ERROR, ex.getMessage()));
	    } catch (UnsupportedEncodingException | JSONException |  UnknownInfrastructure ex) {
		ifNotInterrupted(() -> failCb.processFailed(SkidErrorCodes.SERVER_ERROR, ex.getMessage()));
	    } catch (IOException | NetworkError ex) {
		ifNotInterrupted(() -> failCb.processFailed(SkidErrorCodes.NETWORK_ERROR, ex.getMessage()));
	    } catch (RuntimeException ex) {
		ifNotInterrupted(() -> failCb.processFailed(SkidErrorCodes.INTERNAL_ERROR, ex.getMessage()));
	    }
	};

	Thread t = new Thread(r, "SamlClient-Start");
	t.start();
	return t::interrupt;
    }

    private AuthReqResp authnReq(String startUrl) throws MalformedURLException, IOException, ClassCastException,
	    SocketTimeoutException, UnsupportedEncodingException, JSONException, UnknownInfrastructure, ServerError,
	    InvalidServerData {
	HttpURLConnection con = HttpURLConnection.class.cast(new URL(startUrl).openConnection());
	con.setInstanceFollowRedirects(true);
	con.setRequestProperty("X-XHR-Client", "true");
	con.setRequestProperty("Accept", "application/json");

	con.connect();
	int resCode = con.getResponseCode();
	if (resCode == HttpURLConnection.HTTP_OK) {
	    // get session id
	    InputStream objStream = con.getInputStream();
	    // TODO: read content encoding from header (Content-Type: application/json; charset=UTF-8
	    String objString = FileUtils.toString(objStream, "UTF-8");

	    String session = JsonPath.read(objString, "$.session");
	    if (session == null) {
		throw new InvalidServerData("No session ID returned from server.");
	    }

	    // get FS URL, so we can determine the SkIDentity base URL
	    String skidUri = getSkidUri(con.getURL().toString());

	    return new AuthReqResp(skidUri, session);
	} else {
	    throw new ServerError(resCode, "Failed to retrieve session from SAML FS.");
	}
    }

    private String getSkidUri(String uri) throws UnknownInfrastructure {
	int idx = uri.indexOf("/fs/saml/");
	if (idx > 0) {
	    return uri.substring(0, idx+1);
	} else {
	    String msg = String.format("The SkIDentity base URL could not be determined based on the SAML FS URL (%s).", uri);
	    throw new UnknownInfrastructure(msg);
	}
    }

    private final class AuthReqResp {
	final String skidBaseUri;
	final String fsSessionId;

	public AuthReqResp(String skidBaseUri, String fsSessionId) {
	    this.skidBaseUri = skidBaseUri;
	    this.fsSessionId = fsSessionId;
	}
    }

}
