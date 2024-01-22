package ru.rutoken.tech.usecasestests.rule

import android.util.Log
import io.kotest.assertions.until.fixed
import io.kotest.assertions.until.until
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import ru.rutoken.rtpcscbridge.RtPcscBridge
import ru.rutoken.rttransport.RtTransport
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * This rule waits for token to be attached with timeout of [waitTimeout].
 * The delay between token checks is fixed and equal to 500 ms.
 */
class TokenWaitingRule(private val waitTimeout: Duration = 5000L.milliseconds) : TestRule {

    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            var pcscReaderExists = false

            val pcscReaderObserver = object : RtTransport.PcscReaderObserver {
                override fun onReaderAdded(reader: RtTransport.PcscReader) {
                    pcscReaderExists = true
                }

                override fun onReaderRemoved(reader: RtTransport.PcscReader) {
                }
            }

            val rtTransport = RtPcscBridge.getTransport()
            rtTransport.addPcscReaderObserver(pcscReaderObserver) shouldBe true

            withClue("PC/SC reader should appear within $waitTimeout") {
                runBlocking {
                    until(waitTimeout, 500L.milliseconds.fixed(), suspend {
                        Log.v(TokenWaitingRule::class.java.canonicalName, "Checking if token is attached...")
                        pcscReaderExists
                    })
                }
            }

            rtTransport.removePcscReaderObserver(pcscReaderObserver)

            base.evaluate()
        }
    }
}
