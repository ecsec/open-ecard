package org.openecard.richclient.pinmanagement

typealias PinChangeCallback<T> = (view: T, oldPin: String, newPin: String) -> Unit
typealias CanPinEntryCallback<T> = (view: T, can: String, pin: String) -> Unit
typealias PasswordEntryCallback<T> = (view: T, password: String) -> Unit
