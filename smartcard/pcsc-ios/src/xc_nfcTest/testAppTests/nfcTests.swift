//
//  xCodeTestTests.swift
//  xCodeTestTests
//
//  Created by Florian Otto on 03.07.25.
//

import CoreNFC
import XCTest

@testable import openecard_pcscIos

final class xCodeTestTests: XCTestCase {
	func test_load() {
		let factory = IosTerminalFactory.companion.instance
		assert(factory.name.elementsEqual("IosNFC"))

		let terminals = factory.load()
		XCTAssertEqual(try terminals.getTerminal(name: "")?.name, "IosNFCTerminal")
	}

	func test_nfcSession_establishing() {
		let factory = IosTerminalFactory.companion.instance
		let terminals = factory.load()
		defer {
			do {
				try terminals.releaseContext()
			} catch {}
		}
		do {
			try terminals.establishContext()
			XCTAssertTrue(terminals.isEstablished)
			try terminals.releaseContext()
			XCTAssertTrue(!terminals.isEstablished)
		} catch {
			XCTFail()
		}
	}

	func test_waitForTag_throwsCancelOnUserCancel() async {
		let factory = IosTerminalFactory.companion.instance
		let terminals = factory.load()
		defer {
			do {
				try terminals.releaseContext()
			} catch {}
		}
		do {
			try terminals.establishContext()
			XCTAssertTrue(terminals.isEstablished)
		} catch {
			XCTFail("Could not establish \(error)")
		}

		do {
			let terminal = try terminals.getTerminal(name: "")
			terminal?.setAlertMessage(msg: "Tap Cancel")
			try await terminal?.waitForCardPresent()
			XCTFail("Session found tag but should have been cancelled")
		} catch {
			XCTAssertNotNil(error)
			print(error)
			XCTAssertTrue(error.localizedDescription.contains("The user pressed 'Cancel'"))
		}

	}
	func test_waitForTag_throwsTimeoutOnTimeout() async {
		let factory = IosTerminalFactory.companion.instance
		let terminals = factory.load()
		defer {
			do {
				try terminals.releaseContext()
			} catch {}
		}
		do {
			try terminals.establishContext()
			XCTAssertTrue(terminals.isEstablished)
		} catch {
			XCTFail("Could not establish \(error)")
		}

		do {
			let terminal = try terminals.getTerminal(name: "")
			terminal?.setAlertMessage(msg: "Wait till nfc discovery times out")
			try await terminal?.waitForCardPresent()

			XCTFail("Session found tag but should have been cancelled")
		} catch {
			XCTAssertNotNil(error)
			print(error)
			XCTAssertTrue(error.localizedDescription.contains("iOS NFC discovery timeout reached"))
		}

	}

	func test_tag_discover() async {
		let factory = IosTerminalFactory.companion.instance
		let terminals = factory.load()
		defer {
			do {
				try terminals.releaseContext()
			} catch {}
		}
		do {
			try terminals.establishContext()
			let terminal = try terminals.getTerminal(name: "")

			XCTAssertEqual(Sc_baseTerminalStateType.absent, try terminal?.getState())
			try await terminal?.waitForCardPresent()
			XCTAssertEqual(Sc_baseTerminalStateType.present, try terminal?.getState())
			XCTAssertEqual(
				try terminals.getTerminal(name: "")?.getState(),
				Sc_baseTerminalStateType.present)

		} catch {
			XCTFail("Failed with error: \(error)")
		}
	}

	func test_atr() async {
		let factory = IosTerminalFactory.companion.instance
		let terminals = factory.load()
		defer {
			do {
				try terminals.releaseContext()
			} catch {}
		}
		do {
			try terminals.establishContext()
			guard let terminal = try terminals.getTerminal(name: "") else {
				XCTFail()
				return
			}
			try await terminal.waitForCardPresent()

			XCTAssertNotNil(
				try terminal.connectTerminalOnly().card.atr
			)

		} catch {
			XCTFail("Failed with error: \(error)")
		}
	}
	func test_send_convertingRawData() async {
		let factory = IosTerminalFactory.companion.instance
		let terminals = factory.load()
		defer {
			do {
				try terminals.releaseContext()
			} catch {}
		}
		do {
			try terminals.establishContext()
			guard let terminal = try terminals.getTerminal(name: "") else {
				XCTFail()
				return
			}
			try await terminal.waitForCardPresent()

			let channel =
				try terminal
				.connectTerminalOnly().card.basicChannel as IosCardChannel
			let apdu = try IosApduHelperKt.commandApduFromRawData(
				data: Data([0x00, 0xA4, 0x00, 0x0c, 0x02, 0x3f, 0x00]))
			let result: Sc_baseResponseApdu = try channel.transmit(apdu: apdu!)

			try terminals.releaseContext()
			XCTAssertEqual(Sc_baseStatusWord.ok, result.status.type)

		} catch {
			XCTFail("Failed with error: \(error)")
		}
	}
	func test_send_convertingNativeApdu() async {
		let factory = IosTerminalFactory.companion.instance

		let terminals = factory.load()
		defer {
			do {
				try terminals.releaseContext()
			} catch {}
		}
		do {

			let terminal = try terminals.getTerminal(name: "")
			terminal?.iosNfcAlertMessages.provideCardMessage = "Provide card for test"
			try terminals.establishContext()
			guard let terminal = try terminals.getTerminal(name: "") else {
				XCTFail()
				return
			}
			try await terminal.waitForCardPresent()

			let channel =
				try terminal
				.connectTerminalOnly().card.basicChannel as IosCardChannel

			let selectMFCommand = NFCISO7816APDU(
				instructionClass: 0x00,
				instructionCode: 0xA4,
				p1Parameter: 0x00,
				p2Parameter: 0x0C,
				data: Data([0x3F, 0x00]),
				expectedResponseLength: -1
			)

			let apdu = try IosApduHelperKt.commandApduFromNFCISO7816APDU(
				iosApdu: selectMFCommand,
				forceExtendedLength: false
			)

			let result = try channel.transmit(apdu: apdu)

			try terminals.releaseContext()
			XCTAssertEqual(Sc_baseStatusWord.ok, result.status.type)

		} catch {
			XCTFail("Failed with error: \(error)")
		}
	}

	func test_tagLost_during_session() async {
		let factory = IosTerminalFactory.companion.instance

		let terminals = factory.load()
		defer {
			do {
				try terminals.releaseContext()
			} catch {}
		}
		do {
			try terminals.establishContext()
			guard let terminal = try terminals.getTerminal(name: "") else {
				XCTFail()
				return
			}
			try await terminal.waitForCardPresent()

			let channel =
				try terminal
				.connectTerminalOnly().card.basicChannel as IosCardChannel

			let selectMFCommand = NFCISO7816APDU(
				instructionClass: 0x00,
				instructionCode: 0xA4,
				p1Parameter: 0x00,
				p2Parameter: 0x0C,
				data: Data([0x3F, 0x00]),
				expectedResponseLength: -1
			)

			let apdu = try IosApduHelperKt.commandApduFromNFCISO7816APDU(
				iosApdu: selectMFCommand,
				forceExtendedLength: false
			)

			terminal.setAlertMessage(msg: "Remove card.")
			sleep(5)
			let result = try channel.transmit(apdu: apdu)

			XCTFail("Transmit was performed - did you remove card?")

		} catch {
			XCTAssertTrue(error.localizedDescription.contains("The smart card has been removed"))
		}
	}

	/**
	This is needed since the iOS NFC session, although calling "invalidated" callback after shutdown
	which updates the state and releases waiting functions, is not immediately ready to get started again.
	The tests, if started all at once to quickly try to start nfc-sessions which will fail.

	One cannot get a ready state from iOS directly, and in normal use-cases this behaviour shouldn't be triggered.
	Thus we live with the sleep below for test behaviour.
	 */
	override func tearDown() {
		super.tearDown()
		sleep(5)
	}
}
