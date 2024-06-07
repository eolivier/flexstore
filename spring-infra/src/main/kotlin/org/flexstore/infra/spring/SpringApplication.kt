package org.flexstore.infra.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class FlexStoreApplication

fun main(args: Array<String>) {
    runApplication<FlexStoreApplication>(*args)
}
