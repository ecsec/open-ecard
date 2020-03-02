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

import skid.mob.client.InvalidServerData;
import skid.mob.client.NetworkError;
import skid.mob.client.ServerError;
import skid.mob.client.SkidCApiClient;
import skid.mob.client.model.SpMetadata;
import skid.mob.lib.AuthModule;
import skid.mob.lib.FinishedCallback;
import skid.mob.lib.FsSession;
import skid.mob.lib.Info;
import skid.mob.lib.Option;


/**
 *
 * @author Tobias Wich
 */
public class FsSessionImpl implements FsSession {

    private final SkidCApiClient apiClient;

    private final String fsSessionId;
    private final FinishedCallback finishedCb;

    private InfoImpl infoImpl;

    FsSessionImpl(String fsSessionId, String skidBaseUri, FinishedCallback finishedCb) {
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
    public AuthModule select(Option o) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
