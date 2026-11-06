package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.user.model.GoogleIdToken

data class JsonGoogleLoginRequest @JsonCreator constructor(
    @JsonProperty("idToken") val idToken: String
) {
    fun toGoogleIdToken() = GoogleIdToken(idToken)
}
