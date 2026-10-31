package org.flexstore.domain.sharedkernel

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

// The current moment, for the domain: the clock of the system, which a test fixes when it needs a known moment
object InstantProvider {

    private var clock: Clock = Clock.systemUTC()

    fun now(): Instant = clock.instant()

    // For the tests: the moment stays the one given, until reset
    fun fixAt(instant: Instant) {
        clock = Clock.fixed(instant, ZoneOffset.UTC)
    }

    fun reset() {
        clock = Clock.systemUTC()
    }
}
