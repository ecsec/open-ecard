package org.openecard.demo

interface Platform {
	val name: String
}

expect fun getPlatform(): Platform
