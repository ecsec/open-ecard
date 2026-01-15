package org.openecard.demo

class Greeting {
	private val platform = getPlatform()

	fun greet(): String = "Hello, ${platform.name}!"
}
