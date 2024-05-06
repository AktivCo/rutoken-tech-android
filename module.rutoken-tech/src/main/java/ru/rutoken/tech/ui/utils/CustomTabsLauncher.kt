package ru.rutoken.tech.ui.utils

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun Context.launchCustomTabsUrl(url: Uri) = CustomTabsIntent.Builder().build().launchUrl(this, url)