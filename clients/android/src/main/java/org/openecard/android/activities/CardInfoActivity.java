/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.android.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import org.openecard.android.ApplicationContext;
import org.openecard.android.R;
import org.openecard.common.I18n;
import org.openecard.common.enums.EventType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.recognition.CardRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Activity shows information about the currently used ecard.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardInfoActivity extends Activity implements EventCallback {

    // logger and translation
    private static final Logger logger = LoggerFactory.getLogger(CardInfoActivity.class);
    private final I18n lang = I18n.getTranslation("android");

    // gui constants
    private static final String REQUEST_CARD = "android.cardinfo.request_card";
    private static final String REQUEST_TEMINAL = "android.cardinfo.request_terminal";
    private static final String NO_CARD = "android.cardinfo.cardname";

    private ApplicationContext appContext;
    CardRecognition recognition;
    private HashMap<String, TerminalFragment> fragments = new HashMap<String, TerminalFragment>();
    private TextView textInfo;
    private LinearLayout linearLayoutCardInfoActivity;
    private int numTerminals;
    private int numCards;
    private List<String> ifdNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// Set up the window layout
	setContentView(R.layout.cardinfo);
	linearLayoutCardInfoActivity = (LinearLayout) findViewById(R.id.linearLayoutCardActivity);
	textInfo = (TextView) findViewById(R.id.label_info);
	// register for events
	appContext = ((ApplicationContext) getApplicationContext());
	recognition = appContext.getRecognition();

	// get an instance of FragmentTransaction from your Activity
	FragmentManager fragmentManager = getFragmentManager();

	try {
	    Dispatcher dispatcher = appContext.getEnv().getDispatcher();
	    EstablishContext establishContext = new EstablishContext();

	    EstablishContextResponse response = (EstablishContextResponse) dispatcher.deliver(establishContext);

	    ListIFDs listIFDs = new ListIFDs();
	    listIFDs.setContextHandle(response.getContextHandle());
	    ListIFDsResponse listIFDsResponse = (ListIFDsResponse) dispatcher.deliver(listIFDs);
	    ifdNames = listIFDsResponse.getIFDName();
	    numTerminals = ifdNames.size();
	    if (ifdNames.size() == 0) {
		textInfo.setVisibility(View.VISIBLE);
		textInfo.setText(lang.translationForKey(REQUEST_TEMINAL));
	    } else {
		textInfo.setVisibility(View.GONE);
		for (String ifdName : ifdNames) {
		    addTerminalFragment(fragmentManager, ifdName);
		}
	    }
	} catch (DispatcherException e) {
	    logger.error("Couldn't get initial state of IFDs.", e);
	} catch (InvocationTargetException e) {
	    logger.error("Couldn't get initial state of IFDs.", e);
	}
	fragmentManager.executePendingTransactions();
	appContext.getEnv().getEventManager().registerAllEvents(this);
    }

    @Override
    protected void onStart() {
	super.onStart();
	ConnectionHandleType cHandle = new ConnectionHandleType();
	for (String ifdName : ifdNames) {
	    cHandle.setIFDName(ifdName);
	    CardStateEntry entry = appContext.getCardStates().getEntry(cHandle);
	    if (entry != null) {
		TerminalFragment fragment = fragments.get(ifdName);
		numCards++;
		cHandle = entry.handleCopy();
		String cardType = cHandle.getRecognitionInfo().getCardType();
		InputStream is = recognition.getCardImage(cardType);
		Drawable drawable = Drawable.createFromStream(is, null);
		fragment.updateFragment(drawable, recognition.getTranslatedCardName(cardType));
	    }
	}
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void signalEvent(final EventType eventType, final Object eventData) {
	runOnUiThread(new UiUpdateRunnable(eventType, eventData));
    }

    private void removeTerminalFragment(FragmentManager fragmentManager, String ifdName) {
	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	TerminalFragment fragment = fragments.get(ifdName);
	fragmentTransaction.remove(fragment);
	fragments.remove(ifdName);
	fragmentTransaction.commit();
    }

    private void addTerminalFragment(FragmentManager fragmentManager, String ifdName) {
	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	TerminalFragment fragment = new TerminalFragment();
	fragment.setIFDName(ifdName);
	fragment.setRecognition(recognition);
	fragments.put(ifdName, fragment);
	fragmentTransaction.add(R.id.cardInfoLinearLayout, fragment);
	fragmentTransaction.commit();
    }

    /**
     * This Runnable updates the UI depending on the happended event.
     *
     * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
     */
    private final class UiUpdateRunnable implements Runnable {

	private final EventType eventType;
	private final Object eventData;

	private UiUpdateRunnable(EventType eventType, Object eventData) {
	    this.eventType = eventType;
	    this.eventData = eventData;
	}

	@Override
	public void run() {
	    FragmentManager fragmentManager = getFragmentManager();
	    if (eventType.equals(EventType.CARD_RECOGNIZED)) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		String cardType = ch.getRecognitionInfo().getCardType();
		InputStream is = recognition.getCardImage(cardType);
		Drawable drawable = Drawable.createFromStream(is, null);
		TerminalFragment myFragment = fragments.get(ch.getIFDName());
		myFragment.updateFragment(drawable, recognition.getTranslatedCardName(cardType));
		numCards++;
	    } else if (eventType.equals(EventType.CARD_REMOVED)) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		Drawable drawable;
		if (appContext.usingNFC()) {
		    try {
			drawable = Drawable.createFromStream(getAssets().open("NFC-logo.png"), null);
		    } catch (IOException e) {
			logger.error("Coudn't load nfc logo; using default no card image.", e);
			InputStream is = recognition.getNoCardImage();
			drawable = Drawable.createFromStream(is, null);
		    }
		} else {
		    InputStream is = recognition.getNoCardImage();
		    drawable = Drawable.createFromStream(is, null);
		}
		TerminalFragment myFragment = fragments.get(ch.getIFDName());
		myFragment.updateFragment(drawable, lang.translationForKey(NO_CARD));
		numCards--;
	    } else if (eventType.equals(EventType.TERMINAL_ADDED)) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		addTerminalFragment(fragmentManager, ch.getIFDName());
		numTerminals++;
	    } else if (eventType.equals(EventType.TERMINAL_REMOVED)) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		removeTerminalFragment(fragmentManager, ch.getIFDName());
		numTerminals--;
	    }

	    // pending transactions must be executed before invalidating the layout
	    fragmentManager.executePendingTransactions();

	    if (numTerminals == 0) {
		textInfo.setText(lang.translationForKey(REQUEST_TEMINAL));
		textInfo.setVisibility(View.VISIBLE);
	    } else if (numCards == 0) {
		textInfo.setText(lang.translationForKey(REQUEST_CARD));
		textInfo.setVisibility(View.VISIBLE);
	    } else {
		textInfo.setVisibility(View.GONE);
	    }

	    linearLayoutCardInfoActivity.invalidate();
	}
    }

}
