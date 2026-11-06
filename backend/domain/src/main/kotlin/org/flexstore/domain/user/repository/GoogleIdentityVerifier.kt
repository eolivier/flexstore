package org.flexstore.domain.user.repository

import org.flexstore.domain.user.model.GoogleIdToken
import org.flexstore.domain.user.model.GoogleIdentity

// The port to Google: tells who a token was given to
fun interface GoogleIdentityVerifier {

    // Throws InvalidGoogleIdTokenException when the token cannot be trusted
    fun verify(idToken: GoogleIdToken): GoogleIdentity
}
