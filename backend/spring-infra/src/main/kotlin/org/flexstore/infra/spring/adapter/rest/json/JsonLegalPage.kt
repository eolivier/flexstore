package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.legal.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

// A legal page as everyone reads it: the id of its version (the customers accept the terms of sale by it), its
// plain text (empty until the administrators write it) and when it was published
data class JsonLegalPage(
    val id: String,
    val kind: LegalPageKind,
    val text: String,
    val publishedAt: Instant
) {
    constructor(legalPage: LegalPage) :
        this(legalPage.id.value.value, legalPage.kind, legalPage.text.value, legalPage.publishedAt.value)
}

// The new text of a legal page, written by an administrator
data class JsonLegalText @JsonCreator constructor(@JsonProperty("text") val text: String) {

    fun toLegalText() = LegalText(text.trim())
}
