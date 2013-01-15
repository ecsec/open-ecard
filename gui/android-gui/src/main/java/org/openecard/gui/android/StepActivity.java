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

package org.openecard.gui.android;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.I18n;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.android.views.StepView;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;
import org.openecard.scio.NFCCardTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This activity displays a specific step when showStep is called.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class StepActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(StepActivity.class.getName());
    private final I18n lang = I18n.getTranslation("gui");

    //TODO: better names for the LinearLayouts
    private LinearLayout llGuiInterface;
    private ArrayList<StepView> views = new ArrayList<StepView>();
    private LinearLayout llOutwards;
    private ScrollView scrollView;
    private View view;
    private LinearLayout ll1;
    private AndroidNavigator navigator;
    private Button btnNext;
    private Button btnBack;
    private Button btnCancel;

    public LinearLayout getLlGuiInterface() {
	return llGuiInterface;
    }

    public LinearLayout getLlOutwards() {
	return llOutwards;
    }

    public AndroidNavigator getNavigator() {
	return navigator;
    }

    public ArrayList<StepView> getViews() {
	return views;
    }

    public ScrollView getScrollView() {
	return scrollView;
    }

    public View getView() {
	return view;
    }

    public LinearLayout getLl1() {
	return ll1;
    }

    public Button getBtnNext() {
	return btnNext;
    }

    public Button getBtnBack() {
	return btnBack;
    }

    public Button getBtnCancel() {
	return btnCancel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.gui_interface);
	llGuiInterface = (LinearLayout) findViewById(R.id.linearLayoutGUIInterface);
	llOutwards = (LinearLayout) findViewById(R.id.linearLayoutOutwards);
	scrollView = (ScrollView) findViewById(R.id.scrollView1);
	view = findViewById(R.id.view1);
	ll1 = (LinearLayout) findViewById(R.id.linearLayout1);
	navigator = AndroidNavigator.getInstance();
	navigator.setActivity(this);
	if (savedInstanceState != null) {
	    showStep(navigator.getSteps().get(savedInstanceState.getInt("step")));
	}
	setupButtons();
    }

    /**
     * Sets the Buttons internationalized Text and adds OnClickListeners.
     */
    private void setupButtons() {
	btnNext = (Button) findViewById(R.id.button_next);
	btnNext.setText(lang.translationForKey("button.next"));
	btnCancel = (Button) findViewById(R.id.button_cancel);
	btnCancel.setText(lang.translationForKey("button.cancel"));
	btnBack = (Button) findViewById(R.id.button_back);
	btnBack.setText(lang.translationForKey("button.back"));

	btnNext.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		try {
		    hideSoftwareKeyboard();
		    navigator.setStepResult(ResultStatus.OK, getResultContent());
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	});

	btnCancel.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		// in case there is a running action, kill it and bail out
		if (navigator.getRunningAction() != null && !navigator.getRunningAction().isDone()) {
		    logger.debug("Canceling execution of the currently running StepAction.");
		    navigator.getRunningAction().cancel(true);
		    return;
		}
		try {
		    navigator.setStepResult(ResultStatus.CANCEL, getResultContent());
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	});

	btnBack.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {

		try {
		    navigator.setStepResult(ResultStatus.BACK, getResultContent());
		} catch (InterruptedException e) {

		    e.printStackTrace();
		}

	    }
	});
    }

    /**
     * Get result for all components on the frame that support result values.
     * 
     * @return List containing all result values. As a matter of fact this list can be empty.
     */
    public List<OutputInfoUnit> getResultContent() {
	ArrayList<OutputInfoUnit> result = new ArrayList<OutputInfoUnit>();
	for (StepView next : views) {
	    if (next.isValueType()) {
		result.add(next.getValue());
	    }
	}
	return result;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putInt("step", navigator.getCurrentStep());
    }

    void showStep(final Step step) {
	views.clear();
	runOnUiThread(new StepLayouterRunnable(step, this));
    }

    void hideSoftwareKeyboard() {
	InputMethodManager mgr = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
	View focus = getCurrentFocus();
	if (focus != null) {
	    mgr.hideSoftInputFromWindow(focus.getWindowToken(), 0);
	}
    }

    @Override
    public void onNewIntent(Intent intent) {
	try {
	    Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    IsoDep tag = IsoDep.get(tagFromIntent);
	    NFCCardTerminal.getInstance().setTag(tag);

	    EstablishContext establishContext = new EstablishContext();

	    //TODO remove reflection part to get the dispatcher
	    Context context = this.getApplicationContext();
	    Method m = context.getClass().getMethod("getEnv");
	    Object env = m.invoke(context);
	    m = env.getClass().getMethod("getDispatcher");
	    Dispatcher d = (Dispatcher) m.invoke(env);

	    EstablishContextResponse response = (EstablishContextResponse) d.deliver(establishContext);
	    Connect c = new Connect();
	    c.setContextHandle(response.getContextHandle());
	    c.setIFDName("Integrated NFC");
	    c.setSlot(new BigInteger("0"));
	    d.deliver(c);

	} catch (DispatcherException ex) {
	    logger.error(ex.getMessage(), ex);
	} catch (InvocationTargetException ex) {
	    logger.error(ex.getMessage(), ex);
	} catch (SecurityException ex) {
	    logger.error(ex.getMessage(), ex);
	} catch (NoSuchMethodException ex) {
	    logger.error(ex.getMessage(), ex);
	} catch (IllegalArgumentException ex) {
	    logger.error(ex.getMessage(), ex);
	} catch (IllegalAccessException ex) {
	    logger.error(ex.getMessage(), ex);
	}
    }

    @Override
    public synchronized void onResume() {
	super.onResume();
	Intent activityIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
	NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public synchronized void onPause() {
	super.onPause();
	NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }

}
