/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toDateString() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this)