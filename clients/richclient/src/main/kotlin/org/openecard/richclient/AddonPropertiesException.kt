package org.openecard.richclient

/**
 * Exception type for all exceptions related to the AddonProperties.
 *
 * @author Hans-Martin Haase
 */
class AddonPropertiesException : Exception {
	constructor(message: String) : super(message)

	constructor(cause: Throwable) : super(cause)

	constructor(message: String, cause: Throwable) : super(message, cause)
}
