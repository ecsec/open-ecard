package org.openecard.client.android.activities;

import java.math.BigInteger;

import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openecard.client.android.ApplicationContext;
import org.openecard.client.android.R;
import org.openecard.client.common.ECardConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity is used for the pin management.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class PINManagementActivity extends Activity {
    private static final String TAG = "PINManagementActivity";
    private static final boolean Debug = true;
    public Dialog dialog;
    private ApplicationContext appState;

    @Override
    protected void onStart() {
	super.onStart();
	// new PINStatusTask(this).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	appState = ((ApplicationContext) getApplicationContext());
	// // Setup the window
	// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	setContentView(R.layout.pin_management);
	// et = (TextView) findViewById(R.id.remainingTries);
	// Log.d(TAG, et.toString());
	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

	TextView mTitle = (TextView) findViewById(R.id.title_left_text);
	mTitle.setText(R.string.app_name);
	mTitle = (TextView) findViewById(R.id.title_right_text);
	mTitle.setText(R.string.menu_pinmanagement);
	// Set result CANCELED incase the user backs out
	// setResult(Activity.RESULT_CANCELED);
	// final EditText editTextCAN = (EditText)
	// findViewById(R.id.editTextCAN);
	// editTextCAN.setEnabled(false);
	// EditText editTextPUK = (EditText) findViewById(R.id.editTextPUK);
	// editTextPUK.setEnabled(false);
	// // Initialize the button to perform device discovery
	Button changePIN = (Button) findViewById(R.id.button_changePIN);
	changePIN.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		EditText editTextOldPIN = (EditText) findViewById(R.id.editTextOldPIN);
		EditText editTextNewPIN = (EditText) findViewById(R.id.editTextNewPIN);
		EditText editTextNewPINRepeat = (EditText) findViewById(R.id.editTextNewPINRepeat);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {

		    Connect c = new Connect();
		    c.setIFDName("Integrated NFC");
		    c.setExclusive(false);
		    c.setContextHandle(appState.getCTX());
		    c.setSlot(new BigInteger("0"));
		    ConnectResponse cr = appState.getEnv().getIFD().connect(c);
		    builder = factory.newDocumentBuilder();
		    Document doc = builder.newDocument();
		    EstablishChannel ec = new EstablishChannel();
		    ec.setSlotHandle(cr.getSlotHandle());
		    DIDAuthenticationDataType did = new DIDAuthenticationDataType();
		    did.setProtocol(ECardConstants.Protocol.PACE);

		    Element e = doc.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "PinID");
		    e.setTextContent("02"); // 02=CAN;03=PIN
		    did.getAny().add(e);
		    e = doc.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "CHAT");
		    e.setTextContent("7f4c12060904007f0007030102025305300301ffb7");
		    did.getAny().add(e);
		    e = doc.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "PIN");
		    e.setTextContent("975096");
		    did.getAny().add(e);

		    ec.setAuthenticationProtocolData(did);

		    EstablishChannelResponse ecr = appState.getEnv().getIFD().establishChannel(ec);
		    Toast.makeText(PINManagementActivity.this, ecr.getResult().getResultMajor(), Toast.LENGTH_SHORT).show();
		    System.out.println("## " + ecr.getResult().getResultMajor());
		    System.out.println("## " + ecr.getResult().getResultMinor());
		    // System.out.println("## " +
		    // ecr.getResult().getResultMessage().getValue());
		} catch (ParserConfigurationException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}

		// if (editTextOldPIN.getText().length() != 6 ||
		// editTextNewPIN.getText().length() != 6 ||
		// editTextNewPINRepeat.getText().length() != 6) {
		// Toast.makeText(getApplicationContext(),
		// "Bitte füllen Sie alle PIN-Felder komplett aus.",
		// Toast.LENGTH_LONG).show();
		// return;
		// }
		// if
		// (!editTextNewPIN.getText().toString().equals(editTextNewPINRepeat.getText().toString()))
		// {
		// Toast.makeText(getApplicationContext(),
		// "Die beiden neuen PINs sind nicht gleich.",
		// Toast.LENGTH_LONG).show();
		// return;
		// }
		//
		// TextView tv = (TextView) findViewById(R.id.remainingTries);
		// if (tv.getText().equals("suspended")) {
		// new
		// PACETask(PINManagementActivity.this).execute(SecretType.PACE_CAN.name(),
		// editTextCAN.getText().toString());
		// } else {
		//
		// new
		// PACETask(PINManagementActivity.this).execute(SecretType.PACE_PIN.name(),
		// editTextOldPIN.getText().toString());
		//
		// }

	    }
	});

    }
}
