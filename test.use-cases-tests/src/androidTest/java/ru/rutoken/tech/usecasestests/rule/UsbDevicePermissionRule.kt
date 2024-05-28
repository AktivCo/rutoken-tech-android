/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.usecasestests.rule

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldNotBe
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.regex.Pattern

private val OK_BUTTON_REGEX = Pattern.compile("OK|ОК")

class UsbDevicePermissionRule(private val appName: String) : TestRule {
    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val appNameDialogTextSelector =
                By.text(Pattern.compile(".*" + Pattern.quote(appName) + ".*")).clazz("android.widget.TextView")

            device.wait(Until.findObject(appNameDialogTextSelector), 3000L)?.let { dialogTextObject ->
                val rootElement = dialogTextObject.rootElement
                val okButton = rootElement.findObject(By.text(OK_BUTTON_REGEX).clazz("android.widget.Button"))
                withClue("OK button not found in permission dialog for app: $appName") { okButton shouldNotBe null }
                okButton.click()
                device.wait(Until.gone(appNameDialogTextSelector), 1000L) shouldNotBe null
            }

            base.evaluate()
        }
    }
}

private val UiObject2.rootElement: UiObject2
    get() {
        var parentObject: UiObject2 = this
        while (parentObject.parent != null) {
            parentObject = parentObject.parent
        }
        return parentObject
    }
