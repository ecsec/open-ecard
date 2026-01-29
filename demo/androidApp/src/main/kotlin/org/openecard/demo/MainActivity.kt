package org.openecard.demo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.openecard.sc.pcsc.AndroidTerminalFactory

class MainActivity : ComponentActivity() {
	var nfcTerminalFactory: AndroidTerminalFactory? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)
		nfcTerminalFactory = AndroidTerminalFactory.instance(this)

		setContent {
			App(nfcTerminalFactory)
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		intent.let {
			nfcTerminalFactory?.tagIntentHandler(it)
		}
	}
}

@Preview
@Composable
fun AppAndroidPreview() {
	App()
}
