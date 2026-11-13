package org.flexstore.infra.spring.adapter.rest.json

import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// The moments and the days of the API are written in ISO-8601 (2026-11-13T10:15:30Z, 2026-11-13)
fun Instant.asJsonText(): String = DateTimeFormatter.ISO_INSTANT.format(this)

fun LocalDate.asJsonText(): String = DateTimeFormatter.ISO_LOCAL_DATE.format(this)
