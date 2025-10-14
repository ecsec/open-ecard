package org.openecard.sc.pcsc.testutils

import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.openecard.sc.iface.SmartCardStackMissing
import org.openecard.sc.iface.SmartcardException
import org.openecard.sc.pcsc.PcscTerminalFactory

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(WhenPcscStackExtension::class)
annotation class WhenPcscStack

class WhenPcscStackExtension : ExecutionCondition {
	override fun evaluateExecutionCondition(p0: ExtensionContext): ConditionEvaluationResult {
		try {
			// when the stack does not fail, continue with the test execution
			PcscTerminalFactory.Companion.instance.load().let {
				it.establishContext()
				it.releaseContext()
			}
			return ConditionEvaluationResult.enabled("PCSC is available")
		} catch (ex: SmartCardStackMissing) {
			return ConditionEvaluationResult.disabled("PCSC is not available")
		} catch (ex: SmartcardException) {
			return ConditionEvaluationResult.disabled("PCSC could not be initialized: ${ex.message}")
		}
	}
}
