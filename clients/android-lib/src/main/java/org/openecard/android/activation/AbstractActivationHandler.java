/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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
import android.content.Intent;
import android.net.Uri;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.android.ex.ApduExtLengthNotSupported;
import org.openecard.android.system.OpeneCardContext;
import org.openecard.android.system.OpeneCardServiceClient;
import org.openecard.android.system.ServiceResponse;
import org.openecard.android.utils.NfcUtils;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.event.IfdEventObject;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.util.CombinedPromise;
import org.openecard.common.util.Promise;
import org.openecard.gui.android.AndroidGui;
import org.openecard.gui.android.UserConsentNavigatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class provides the basic eID activation functionality as specified in BSI TR-03124-1.
 * <p>It takes care of performing the Intent handling, initializing the Open eCard Stack (Service) and provides
 * the EacGui interface which is needed to implement the UI.</p>
 * <p>An example implementation can be found in the CustomActivationActivity in
 * <a href="https://github.com/ecsec/open-ecard-android">ecsec/open-ecard-android</a>).</p>
 *
 * <p>The parent activity must call the following handler functions in order to use the implementation in the wrapper.</p>
 * <ul>
 * <li>{@code onStart()}</li>
 * <li>{@code onStop()}</li>
 * <li>{@code onPause()}</li>
 * <li>{@code onResume()}</li>
 * <li>{@code onNewIntent(Intent)}</li>
 * </ul>
 * <p>Such an implementation looks as follows:</p>
 * <pre>{@code
 * private final AbstractActivationHandler activationImpl;
 *
 * protected void onStart() {
 *     super.onStart();
 *     activationImpl.onStart();
 * }
 * }</pre>
 *
 * @author Mike Prechtl
 * @author Tobias Wich
 * @param <T> Type of the parent activity, so it is convenient to access functions and fields from this class.
 * @param <GUI>
 */
public abstract class AbstractActivationHandler <T extends Activity, GUI extends AndroidGui>
	implements ActivationImplementationInterface <GUI> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractActivationHandler.class);

    protected final T parent;

    protected Dialog cardRemoveDialog;

    private Thread authThread;
    private boolean cardPresent;
    private final List<Class<? extends AndroidGui>> androidGuiClasses;
    private AndroidGui androidGui;

    private OpeneCardServiceClient client;
    private OpeneCardContext octx;
    private Class<?> returnClass;


    public AbstractActivationHandler(T parent, Class<? extends AndroidGui>... androidGuiClasses) {
	this.parent = parent;
	this.androidGuiClasses = Arrays.asList(androidGuiClasses);
    }


    public synchronized void onResume() {
	// enable dispatch with nfc tag
	NfcUtils.getInstance().enableNFCDispatch(parent);
    }

    public synchronized void onPause() {
	try {
	    // disable dispatch with nfc tag
	    NfcUtils.getInstance().disableNFCDispatch(parent);
	} catch (Exception e) {
	    LOG.info(e.getMessage(), e);
	}
    }

    public void onStart() {
	client = new OpeneCardServiceClient(parent.getApplicationContext());
	new Thread(() -> {
	    ServiceResponse r = client.startService();
	    switch (r.getResponseLevel()) {
		case INFO:
		    onOecInitSuccess(client.getContext());
		    break;
		default:
		    onAuthenticationFailure(new ActivationResult(ActivationResultCode.INTERNAL_ERROR, r.getMessage()));
	    }
	}, "Oec Service Initializer").start();
    }

    protected boolean isActivateUrlAllowed(@Nonnull Uri eIDUrl) {
	return true;
    }

    private void onOecInitSuccess(OpeneCardContext ctx) {
	this.octx = ctx;
	final ActivationController ac = new ActivationController(octx);

	// add callback to this abstract activity when card is removed
	cardRecognized = false;

	// check card states for already inserted card and call recognised handler
	CardStateEntry availableCard = getAvailableCard();
	if (availableCard != null) {
	    insertionHandler.signalEvent(EventType.CARD_INSERTED, new IfdEventObject(availableCard.handleCopy()));
	    cardDetectHandler.signalEvent(EventType.RECOGNIZED_CARD_ACTIVE, new IfdEventObject(availableCard.handleCopy()));
	}

	octx.getEventDispatcher().add(insertionHandler, EventType.CARD_REMOVED, EventType.CARD_INSERTED);
	octx.getEventDispatcher().add(cardDetectHandler, EventType.RECOGNIZED_CARD_ACTIVE);
	octx.getEventDispatcher().add(removalHandler, EventType.CARD_REMOVED);

	Intent actIntent = parent.getIntent();
	Uri data = actIntent.getData();
	setReturnClass(forClassName(actIntent.getStringExtra(RETURN_CLASS)));

	if (data == null || ! isActivateUrlAllowed(data)) {
	    handleActivationResult(new ActivationResult(ActivationResultCode.INTERNAL_ERROR,
		    "Missing or invalid activation URL received."));
	} else {
	    String eIDUrl = data.toString();
	    waitForEacGui();
	    // startService TR procedure according to [BSI-TR-03124-1]
	    authThread = new Thread(() -> {
		ActivationResult result = ac.activate(eIDUrl);
		handleActivationResult(result);
	    }, "OeC Activation Process");
	    authThread.start();
	    // when app is closed or minimized the authentication process is interrupted and have to startService again
	}
    }

    private CardStateEntry getAvailableCard() {
	// look in card states for a card matching our criteria
	Set<String> types = getSupportedCards();

	if (types != null) {
	    for (String type : types) {
		ConnectionHandleType query = new ConnectionHandleType();
		ConnectionHandleType.RecognitionInfo rinfo = new ConnectionHandleType.RecognitionInfo();
		rinfo.setCardType(RETURN_CLASS);
		query.setRecognitionInfo(rinfo);
		CardStateEntry entry = octx.getCardStates().getEntry(query);
		if (entry != null) {
		    return entry;
		}
	    }
	    return null;
	} else {
	    return octx.getCardStates().getEntry(new ConnectionHandleType());
	}
    }

    @Nullable
    private Class<?> forClassName(@Nullable String className) {
	if (className != null) {
	    try {
		return parent.getClassLoader().loadClass(className);
	    } catch (ClassNotFoundException ex) {
		LOG.error("Invalid return class named in activation intent.", ex);
	    }
	}

	return null;
    }


    public void onNewIntent(Intent intent) {
	checkNfcTag(intent);
    }

    private void checkNfcTag(Intent intent) {
	if (this.octx != null) {
	    try {
		// extract nfc tag
		NfcUtils.getInstance().retrievedNFCTag(intent);
	    } catch (ApduExtLengthNotSupported ex) {
		LOG.error(ex.getMessage());
	    }
	}
    }


    public void onStop() {
	// make sure nothing is running anymore
	cancelAuthenticationInt(authThread, false);

	// remove callback which is set onStart
	if (octx != null) {
	    octx.getEventDispatcher().del(insertionHandler);
	    octx.getEventDispatcher().del(cardDetectHandler);
	    octx.getEventDispatcher().del(removalHandler);
	}
	// unbind client
	if (client != null) {
	    client.unbindService();
	}

	// clear member variables
	returnClass = null;
	client = null;
	octx = null;
	cardRemoveDialog = null;
	androidGui = null;
	cardRecognized = false;
    }

    private final EventCallback insertionHandler = new EventCallback() {
	@Override
	public void signalEvent(EventType eventType, EventObject eventData) {
	    switch (eventType) {
		case CARD_REMOVED:
		    cardPresent = false;
		    Dialog d = cardRemoveDialog;
		    if (d != null && d.isShowing()) {
			d.dismiss();
		    }
		    break;
		case CARD_INSERTED:
		    cardPresent = true;
		default:
		    LOG.debug("Received an unsupported Event: " + eventType.name());
		    break;
	    }
	}
    };

    private boolean cardRecognized = false;
    private final EventCallback cardDetectHandler = new EventCallback() {
	@Override
	public void signalEvent(EventType eventType, EventObject eventData) {
	    switch (eventType) {
		case RECOGNIZED_CARD_ACTIVE:
		    Set<String> supportedCards = getSupportedCards();
		    ConnectionHandleType handle = eventData.getHandle();
		    final String type = handle.getRecognitionInfo().getCardType();

		    if (supportedCards == null || supportedCards.contains(type)) {
			// remove handler when the correct card is present
			if (octx != null) {
			    octx.getEventDispatcher().del(this);
			}

			cardRecognized = true;
			onCardInserted(type);
		    }
		    break;
		default:
		    LOG.debug("Received an unsupported Event: " + eventType.name());
		    break;
	    }
	}
    };

    private final EventCallback removalHandler = new EventCallback() {
	@Override
	public void signalEvent(EventType eventType, EventObject eventData) {
	    if (cardRecognized) {
		cardRecognized = false;
		onCardRemoved();
	    }
	}
    };


    private synchronized void handleActivationResult(final ActivationResult result) {
	// only this first invocation must be processed, in order to prevent double finish when cancelling the auth job
	if (authThread == null) {
	    return;
	} else {
	    authThread = null;
	}
	authThread = null;

	switch (result.getResultCode()) {
	    case OK:
	    case REDIRECT:
		onAuthenticationSuccess(result);
		break;
	    case INTERRUPTED:
		onAuthenticationInterrupted(result);
		break;
	    default:
		onAuthenticationFailure(result);
		break;
	}
    }

    /**
     * This method starts a thread which is waiting for the Android Gui.
     * If the Gui is available, the {@link #onGuiIfaceSet(org.openecard.gui.android.AndroidGui)} function will be
     * called.
     */
    private void waitForEacGui() {
	new Thread(() -> {
	    List<UserConsentNavigatorFactory<? extends AndroidGui>> eacNavFactories;
	    eacNavFactories = octx.getGuiNavigatorFactories(androidGuiClasses);
	    try {
		androidGui = waitForGuiPromise(eacNavFactories);
		// the following cast is an assumption that all classes are compatible to the required generic
		onGuiIfaceSet((GUI) androidGui);
	    } catch (InterruptedException ex) {
		LOG.error("Waiting for Eac Gui was interrupted.", ex);
	    }
	}, "WaitForEacGuiThread").start();
    }

    private AndroidGui waitForGuiPromise(List<UserConsentNavigatorFactory<? extends AndroidGui>> factories)
	    throws InterruptedException {
	ArrayList<Promise<AndroidGui>> promises = new ArrayList<>();
	for (UserConsentNavigatorFactory<? extends AndroidGui> next : factories) {
	    Promise<? extends AndroidGui> promise = next.getIfacePromise();
	    promises.add((Promise<AndroidGui>) promise);
	}

	CombinedPromise<AndroidGui> cp = new CombinedPromise<>(promises);
	return cp.retrieveFirst();
    }


    @Override
    public void onCardInserted(String cardType) {
	// default implementation does nothing
	LOG.info("Card recognized event received in activity: cardType={}", cardType);
    }

    @Override
    public void onCardRemoved() {
	// default implementation does nothing
	LOG.info("Card removed event received in activity.");
    }

    @Override
    public void cancelAuthentication() {
	cancelAuthentication(false);
    }

    @Override
    public void cancelAuthentication(boolean runInThread) {
	cancelAuthenticationInt(authThread, runInThread);
    }

    private void cancelAuthenticationInt(Thread at, boolean runInNewThread) {
	if (at != null) {
	    if (at == authThread) {
		authThread = null;
	    }
	    // define function
	    Runnable fun = () -> {
		try {
		    // cancel task and handle event
		    String msg = "";
		    ActivationResult result = new ActivationResult(ActivationResultCode.INTERRUPTED, msg);
		    onAuthenticationInterrupted(result);

		    LOG.info("Stopping Authentication thread ...");
		    at.interrupt();
		    at.join();
		    LOG.info("Authentication thread has stopped.");

		} catch (InterruptedException ex) {
		    LOG.error("Waiting for Authentication thread interrupted.");
		} finally {
		    // make sure it is really null after this method is finished
		    authThread = null;
		}
	    };

	    // run in thread if requested or run synchronous
	    if (runInNewThread) {
		new Thread(fun).start();
	    } else {
		fun.run();
	    }
	}
    }


    @Override
    public void onAuthenticationSuccess(final ActivationResult result) {
	// show card remove dialog before the redirect occurs
	final String location = result.getRedirectUrl();

	// only display if a card is available
	if (isCardPresent()) {
	    Dialog d = getDialog();
	    if (d != null) {
		cardRemoveDialog = d;
		// dialog functions must run on the UI thread
		parent.runOnUiThread(() -> {
		    d.setCanceledOnTouchOutside(false);
		    d.setCancelable(false);
		    // if card remove dialog is not shown, then show it
		    if (! d.isShowing()) {
			d.show();
		    }
		    // redirect to the termination uri when the card remove dialog is closed
		    d.setOnDismissListener(dialog -> {
			// clean dialog field
			cardRemoveDialog = null;
			// perform redirect
			if (result.getResultCode() == ActivationResultCode.REDIRECT) {
			    // handlers belong into the background
			    new Thread(() -> {
				authenticationSuccessAction(location);
			    }, "EAC Success Handler").start();
			}
		    });
		});
		// return as the redirect is handled in the DismissListener
		return;
	    }
	}

	// no dialog shown, just perfrom action
	if (result.getResultCode() == ActivationResultCode.REDIRECT) {
	    authenticationSuccessAction(location);
	}
    }

    @Nullable
    private Dialog getDialog() {
	try {
	    FutureTask<Dialog> t = new FutureTask(this::showCardRemoveDialog);
	    parent.runOnUiThread(t);
	    return t.get();
	} catch (ExecutionException ex) {
	    LOG.error("Exception seen while obtaining Card Removed Dialog.", ex);
	    return null;
	} catch (InterruptedException ex) {
	    LOG.warn("Interruption received while obtaining Card Removed Dialog, continuing nevertheless.");
	    return null;
	}
    }

    protected void authenticationSuccessAction(String location) {
	if (location != null) {
	    // redirect to result location
	    parent.startActivity(createRedirectIntent(location));
	}
    }

    /**
     * Default handler calling the failure handler ({@link #onAuthenticationFailure(ActivationResult)}).
     *
     * @param result Result with redirect status code.
     */
    @Override
    public void onAuthenticationInterrupted(ActivationResult result) {
	// forward as failure. Can be overridden by the implementor
	onAuthenticationFailure(result);
    }


    /**
     * Sets the return class which shall be used as a target in the redirect URL Intent.
     *
     * @param clazz Return class.
     */
    protected final void setReturnClass(@Nullable Class<?> clazz) {
	this.returnClass = clazz;
    }

    protected Intent createRedirectIntent(String location) {
	return createRedirectIntent(location, returnClass);
    }

    /**
     * Build URL Intent which is invoked in the {@link #onAuthenticationSuccess(ActivationResult)}.
     *
     * @param location Redirect URL used in the Intent.
     * @param returnClazz Optional activity class which is the target of the Intent.
     * @return URL intent containing the redirect URL.
     */
    protected Intent createRedirectIntent(String location, @Nullable Class<?> returnClazz) {
	Intent i;
	Uri redirectUri = Uri.parse(location);
	if (returnClazz != null) {
	    i = new Intent(Intent.ACTION_VIEW, redirectUri, parent, returnClazz);
	} else {
	    i = new Intent(Intent.ACTION_VIEW, redirectUri);
	}
	return i;
    }

    protected boolean isCardPresent() {
	return cardPresent;
    }

}
