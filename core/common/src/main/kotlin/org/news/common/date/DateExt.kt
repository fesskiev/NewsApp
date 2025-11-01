package org.news.common.date

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
fun Long.toDateString(): String = dateFormatter.format(Date(this))