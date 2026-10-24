package org.ucop.domain

import org.slf4j.LoggerFactory

/**
 * One concise line per use case execution, to follow them in the logs: its name, its outcome and its duration.
 * The request is never logged: it may carry a password.
 */
object UseCaseLog {

    private val logger = LoggerFactory.getLogger("UseCase")

    fun <R> execution(useCaseName: String, useCase: () -> R): R {
        val start = System.nanoTime()
        try {
            val outcome = useCase()
            logger.info("{}: done ({} ms)", useCaseName, elapsedMillis(start))
            return outcome
        } catch (refusal: NominalException) {
            logger.warn("{}: refused ({}: {}) ({} ms)", useCaseName, refusal::class.simpleName, refusal.message, elapsedMillis(start))
            throw refusal
        } catch (failure: Exception) {
            logger.error("{}: failed ({}: {}) ({} ms)", useCaseName, failure::class.simpleName, failure.message, elapsedMillis(start))
            throw failure
        }
    }

    private fun elapsedMillis(start: Long) = (System.nanoTime() - start) / 1_000_000
}
