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

package org.openecard.android.activation;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import org.openecard.android.system.OpeneCardContext;
import org.openecard.android.ex.ApduExtLengthNotSupported;
import org.openecard.android.utils.NfcUtils;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.gui.android.EacNavigatorFactory;
import org.openecard.gui.android.eac.EacGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class provides the basic functionality as specified in the technical guideline and the
 * initialisation of the Eac UI interface service. By extending the class, the UI can be added
 * (see BindingActivity in <a href="https://github.com/ecsec/open-ecard-android">ecsec/open-ecard-android</a>).
 *
 * @author Mike Prechtl
 */
public abstract class AbstractActivationActivity extends Activity {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractActivationActivity.class);

    private Dialog cardRemoveDialog;

    private volatile boolean alreadyInitialized = false;
    // if someone returns to the App, but Binding uri was already used.
    private volatile boolean eIDUrlUsed = false;

    @Override
    protected synchronized void onResume() {
	super.onResume();

	// enable dispatch with nfc tag
	NfcUtils.getInstance().enableNFCDispatch(this);
    }

    @Override
    protected synchronized void onPause() {
	super.onPause();

	try {
	    // disable dispatch with nfc tag
	    NfcUtils.getInstance().disableNFCDispatch(this);
	} catch (Exception e) {
	    LOG.info(e.getMessage(), e);
	}
    }

    @Override
    protected void onStart() {
	super.onStart();

	// add callback to this abstract activity when card is removed
	OpeneCardContext octx = OpeneCardContext.getContext();
	octx.getEventDispatcher().add(eventReceiver, EventType.CARD_REMOVED);

	waitForEacGui();

	Uri data = getIntent().getData();
	final String eIDUrl = data.toString();
	if (eIDUrl != null && ! eIDUrlUsed) {
	    // start TR procedure according to [BSI-TR-03124-1]
	    new Thread(new Runnable() {
		@Override
		public void run() {
		    ActivationController ac = new ActivationController();
		    ActivationResult result = ac.activate(eIDUrl);
		    handleActivationResult(result);
		}
	    }, "ActivationThread").start();
	    // when app is closed or minimized the authentication process is interrupted and have to start again
	    eIDUrlUsed = true;
	} else {
	    onAuthenticationFailure(new ActivationResult(ActivationResultCode.INTERNAL_ERROR,
		    "Authentication process already finished."));
	}
    }

    @Override
    protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
	try {
	    // extract nfc tag
	    NfcUtils.getInstance().retrievedNFCTag(intent);
	} catch (ApduExtLengthNotSupported ex) {
	    LOG.error(ex.getMessage());
	}
    }


    @Override
    protected void onStop() {
	super.onStop();

	// remove callback which is set onStart
	OpeneCardContext.getContext().getEventDispatcher().del(eventReceiver);
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	if (alreadyInitialized) {
	    alreadyInitialized = false;
	}
    }

    private final EventCallback eventReceiver = new EventCallback() {

	@Override
	public void signalEvent(EventType eventType, EventObject eventData) {
	    if (eventType.equals(EventType.CARD_REMOVED)) {
		if (cardRemoveDialog != null && cardRemoveDialog.isShowing()) {
		    cardRemoveDialog.dismiss();
		}
	    } else {
		throw new IllegalStateException("Recognized an unsupported Event: " + eventType.name());
	    }
	}
    };

    private void handleActivationResult(ActivationResult result) {
	switch (result.getResultCode()) {
	    case REDIRECT:
		onAuthenticationSuccess(result);
		break;
	    default:
		onAuthenticationFailure(result);
		break;
	}
    }

    /**
     * This method starts a thread which is waiting for the Eac Gui. If the Eac Gui is
     *
     */
    private void waitForEacGui() {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		OpeneCardContext octx = OpeneCardContext.getContext();
		EacNavigatorFactory eacNavFactory = octx.getEacNavigatorFactory();
		try {
		    EacGui eacGui = eacNavFactory.getIfacePromise().deref();
		    onEacIfaceSet(eacGui);
		} catch (InterruptedException ex) {
		    LOG.error("Waiting for Eac Gui was interrupted.", ex);
		}
	    }
	}, "WaitForEacGuiThread").start();
    }

    /**
     * Implement this method to recognize a successful authentication in the Sub-Activity. You can handle the following
     * steps on your own by overriding this method, for example show that the authentication was successful
     * and then redirect to the redirect location.
     *
     * @param result which contains additional information to the authentication.
     */
    public void onAuthenticationSuccess(final ActivationResult result) {
	// show card remove dialog before the redirect occurs
	// dialog is shown on ui thread
	runOnUiThread(new Runnable() {
	    @Override
	    public void run() {
		cardRemoveDialog = showCardRemoveDialog();
		cardRemoveDialog.setCanceledOnTouchOutside(false);
		cardRemoveDialog.setCancelable(false);
		// if card remove dialog is not shown, then show it
		if (! cardRemoveDialog.isShowing()) {
		    cardRemoveDialog.show();
		}
		// redirect to the termination uri when the card remove dialog is closed
		cardRemoveDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
		    @Override
		    public void onDismiss(DialogInterface dialog) {
			String location = result.getRedirectUrl();
			if (location != null) {
			    // redirct to result location
			    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
			    startActivity(i);
			}
		    }
		});
	    }
	});
    }

    /**
     * Implement this method to recognize a failed authentication in the Sub-Activity. You can handle the following
     * steps on your own, for example show that the authentication failed and then close the Activity with finish().
     *
     * @param result  which contains additional information to the authentication.
     */
    public abstract void onAuthenticationFailure(ActivationResult result);

    /**
     * Implement this method to show the card remove dialog. If the authentication process ends, the card should be
     * removed. To enable this, a card remove dialog is shown to the user. The dialog should contain only a hint for
     * the user. The dialog can not be removed by the user with a button click, only by the app when the card is removed.
     *
     * @return
     */
    public abstract Dialog showCardRemoveDialog();

    /**
     * This method is called if the Eac Gui is available. If this method is called you can access the server data and
     * start the authentication process.
     *
     * @param eacGui
     */
    public abstract void onEacIfaceSet(EacGui eacGui);

}
