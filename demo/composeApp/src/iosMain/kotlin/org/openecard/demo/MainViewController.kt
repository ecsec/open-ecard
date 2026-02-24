package org.openecard.demo

import IosTerminalFactory
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App(IosTerminalFactory.instance) }
