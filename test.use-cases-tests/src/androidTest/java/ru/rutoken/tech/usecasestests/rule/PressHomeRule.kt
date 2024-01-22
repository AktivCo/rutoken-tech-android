package ru.rutoken.tech.usecasestests.rule

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class PressHomeRule : TestRule {
    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressHome()
            base.evaluate()
        }
    }
}
