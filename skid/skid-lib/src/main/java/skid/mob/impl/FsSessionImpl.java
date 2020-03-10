/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl;

import java.net.URISyntaxException;
import org.openecard.common.util.UrlBuilder;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.EacControllerFactory;
import skid.mob.impl.client.InvalidServerData;
import skid.mob.impl.client.NetworkError;
import skid.mob.impl.client.ServerError;
import skid.mob.impl.client.SkidCApiClient;
import skid.mob.impl.client.model.SpMetadata;
import skid.mob.lib.ActivationType;
import skid.mob.lib.Cancellable;
import skid.mob.lib.FinishedCallback;
import skid.mob.lib.FsSession;
import skid.mob.lib.Info;
import skid.mob.lib.Option;
import skid.mob.lib.SkidErrorCodes;
import static skid.mob.impl.ThreadUtils.ifNotInterrupted;
import skid.mob.lib.SelectedOption;
import skid.mob.lib.AuthModuleCallback;
import skid.mob.lib.ProcessFailedCallback;


/**
 *
 * @author Tobias Wich
 */
public class FsSessionImpl implements FsSession {

    private final ActivationSource oecActivationSource;
    private final SkidCApiClient apiClient;

    private final String fsSessionId;
    private final FinishedCallback finishedCb;

    private InfoImpl infoImpl;

    FsSessionImpl(ActivationSource oecActivationSource, String fsSessionId, String skidBaseUri, FinishedCallback finishedCb) {
	this.oecActivationSource = oecActivationSource;
	this.apiClient = new SkidCApiClient(skidBaseUri);
	this.fsSessionId = fsSessionId;
	this.finishedCb = finishedCb;
    }

    void load() throws NetworkError, ServerError, InvalidServerData {
	// load SP infos and options
	Object spMdObj = apiClient.broker().getOptions(fsSessionId);
	SpMetadata spMdModel = new SpMetadata(spMdObj);
	spMdModel.load();

	infoImpl = new InfoImpl(spMdModel);
	infoImpl.load();
    }

    @Override
    public Info getInfo() {
	return infoImpl;
    }

    @Override
    public Cancellable cancelSession(ProcessFailedCallback failedCb) {
	Runnable r = () -> {
	    try {
		String actUrl = apiClient.broker().cancelSession(fsSessionId);
		ifNotInterrupted(() -> finishedCb.finished(actUrl));
	    } catch (NetworkError ex) {
		ifNotInterrupted(() -> failedCb.processFailed(SkidErrorCodes.NETWORK_ERROR, ex.getMessage()));
	    } catch (ServerError ex) {
		ifNotInterrupted(() -> failedCb.processFailed(SkidErrorCodes.SERVER_ERROR, ex.getMessage()));
	    }
	};

	Thread t = new Thread(r, "FsSession-Cancel");
	t.start();
	return t::interrupt;
    }

    @Override
    public Cancellable select(SelectedOption o, ProcessFailedCallback failedCb, AuthModuleCallback authCb) {
	Runnable r = () -> {
	    try {
		if (isNpa(o.getOption())) {
		    // select option
		    String actUrl = sendSelect(o);
		    String localUrl = buildEidClientUrl(actUrl);
		    // start authentication
		    ifNotInterrupted(() -> {
			EacControllerFactory fact = oecActivationSource.eacFactory();
			EacAuthModule authMod = new EacAuthModule(fact, localUrl, finishedCb);
			authCb.doAuth(authMod);
		    });
		} else {
		    ifNotInterrupted(() -> failedCb.processFailed(SkidErrorCodes.UNSUPPORTED_FEATURE,
			    "The selected option is currently not supported."));
		}
	    } catch (NetworkError ex) {
		ifNotInterrupted(() -> failedCb.processFailed(SkidErrorCodes.NETWORK_ERROR, ex.getMessage()));
	    } catch (ServerError ex) {
		ifNotInterrupted(() -> failedCb.processFailed(SkidErrorCodes.SERVER_ERROR, ex.getMessage()));
	    }
	};

	Thread t = new Thread(r, "FsSession-Select");
	t.start();
	return t::interrupt;
    }

    private String sendSelect(SelectedOption o) throws NetworkError, ServerError {
	String optionId = o.getOption().optionId();
	// TODO: add selection
	String actUrl = apiClient.broker().selectOption(fsSessionId, optionId);
	return actUrl;
    }

    private boolean isNpa(Option o) {
	boolean eidClientActivation = o.activationType() == ActivationType.EID_CLIENT;
	boolean isNpa = "http://bsi.bund.de/cif/npa.xml".equals(o.type());
	boolean isTr03124 = "urn:oid:1.3.162.15480.3.0.14".equals(o.protocol());
	return eidClientActivation && isNpa && isTr03124;
    }

    private String buildEidClientUrl(String authUrl) {
	try {
	    String localUrl = UrlBuilder.fromUrl("http://localhost:24727/eID-Client")
		    .queryParam("tcTokenURL", authUrl)
		    .build().toString();
	    return localUrl;
	} catch (URISyntaxException ex) {
	    throw new IllegalStateException("Unexpected error while building localhost URL.");
	}
    }

}
