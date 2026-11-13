package org.flexstore.domain.seller

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.IdentityBased
import org.ucop.domain.entity.Actor
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException

// One version of the seller: each edit gives a new one, so that an invoice keeps the seller of its issue
data class SellerId(override val value: Identity) : IdentityBased {
    companion object {
        fun nextId() = SellerId(Identity.nextId())
    }
}

data class SellerName(val value: String) {
    fun isGiven() = value.isNotBlank()
}

// The address of the seller, on one line
data class SellerAddress(val value: String) {
    fun isGiven() = value.isNotBlank()
}

private const val SIREN_DIGITS = 9
private const val SIREN_GROUP_DIGITS = 3

// The SIRET of the seller (the French identifier of its establishment): 14 digits, maybe grouped by spaces
data class Siret(val value: String) {
    fun isValid() = SIRET.matches(value.replace(" ", ""))

    // Its first 9 digits, which identify the company itself
    fun siren(): SellerSiren {
        val sirenDigits = value.replace(" ", "").take(SIREN_DIGITS)
        return SellerSiren(sirenDigits.chunked(SIREN_GROUP_DIGITS).joinToString(" "))
    }
}

// The SIREN of the seller (the French identifier of the company), grouped by 3 digits: 123 456 789
data class SellerSiren(val value: String)

// The intra-community VAT number of the seller: its country (FR), then 2 to 13 digits or letters
data class SellerVatNumber(val value: String) {
    fun isValid() = VAT_NUMBER.matches(value.replace(" ", "").uppercase())
}

// The phone number of the seller: digits, maybe after a +, grouped by spaces, dots, dashes or parentheses
data class SellerPhone(val value: String) {
    fun isGiven() = value.isNotBlank()

    fun isValid() = PHONE.matches(value)
}

// The email the customers write to
data class SellerEmail(val value: String) {
    fun isGiven() = value.isNotBlank()

    fun isValid() = EMAIL.matches(value)
}

// The website of the shop, with or without its scheme (https://www.flexstore.fr, www.flexstore.fr)
data class SellerWebsite(val value: String) {
    fun isGiven() = value.isNotBlank()

    fun isValid() = WEBSITE.matches(value)
}

// How the customers reach the seller. The versions of the seller written before these were asked have none
data class SellerReach(val phone: SellerPhone, val email: SellerEmail, val website: SellerWebsite) {
    fun isValid() = phone.isValid() && email.isValid() && website.isValid()

    fun isInvalid() = !isValid()

    companion object {
        fun none() = SellerReach(SellerPhone(""), SellerEmail(""), SellerWebsite(""))
    }
}

// The legal form of the seller: SAS, SARL, EURL...
data class SellerLegalForm(val value: String) {
    fun isGiven() = value.isNotBlank()
}

// The share capital of the seller, in euros: digits, maybe grouped by spaces (10 000)
data class ShareCapital(val value: String) {
    fun isValid() = SHARE_CAPITAL.matches(value)
}

// The city of the trade and companies register (RCS) where the seller is registered
data class RegistryCity(val value: String) {
    fun isGiven() = value.isNotBlank()
}

// How the seller is incorporated, written on the invoices with its SIREN. The versions of the seller written
// before these were asked have none
data class SellerIncorporation(
    val legalForm: SellerLegalForm,
    val shareCapital: ShareCapital,
    val registryCity: RegistryCity
) {
    fun isValid() = legalForm.isGiven() && shareCapital.isValid() && registryCity.isGiven()

    fun isInvalid() = !isValid()

    companion object {
        fun none() = SellerIncorporation(SellerLegalForm(""), ShareCapital(""), RegistryCity(""))
    }
}

// The social networks where the shop has an account
enum class SocialNetwork { FACEBOOK, INSTAGRAM, X, TIKTOK, YOUTUBE, TWITCH }

// The web address of the account of the shop on a social network, with or without its scheme; empty when the
// shop has no account there
data class SocialNetworkUrl(val value: String) {
    fun isGiven() = value.isNotBlank()

    fun isNotGiven() = value.isBlank()

    fun isValid() = isNotGiven() || WEBSITE.matches(value)
}

// The accounts of the shop on the social networks: each one is given or not. The versions of the seller written
// before they were asked have none
data class SocialNetworks private constructor(private val urls: Map<SocialNetwork, SocialNetworkUrl>) {
    fun urlOf(socialNetwork: SocialNetwork) = urls.getValue(socialNetwork)

    fun isValid() = SocialNetwork.entries.all { socialNetwork -> urlOf(socialNetwork).isValid() }

    fun isInvalid() = !isValid()

    companion object {
        // A network missing from the given addresses has none
        fun of(givenUrls: Map<SocialNetwork, SocialNetworkUrl>) = SocialNetworks(
            SocialNetwork.entries.associateWith { socialNetwork ->
                givenUrls[socialNetwork] ?: SocialNetworkUrl("")
            }
        )

        fun none() = of(emptyMap())
    }
}

// Who the seller is and where it is
data class SellerContact(val name: SellerName, val address: SellerAddress) {
    fun isComplete() = name.isGiven() && address.isGiven()

    fun isIncomplete() = !isComplete()
}

// How the seller is registered
data class SellerIdentifier(val siret: Siret, val vatNumber: SellerVatNumber) {
    fun isValid() = siret.isValid() && vatNumber.isValid()

    fun isInvalid() = !isValid()
}

// What the administrators write about the seller: who it is, how it is registered, how to reach it, how it is
// incorporated, and where its social networks are
data class SellerDetail(
    val contact: SellerContact,
    val identifier: SellerIdentifier,
    val reach: SellerReach,
    val incorporation: SellerIncorporation,
    val socialNetworks: SocialNetworks
) {
    // A seller without social networks
    constructor(
        contact: SellerContact,
        identifier: SellerIdentifier,
        reach: SellerReach,
        incorporation: SellerIncorporation
    ) : this(contact, identifier, reach, incorporation, SocialNetworks.none())
}

// The shop that sells, as written on its invoices, in one of its versions
data class Seller(val id: SellerId, val detail: SellerDetail)

// An administrator who gives a new version of the seller
data class SellerUpdate(val editor: Actor, val seller: Seller)

// Anyone who reads the social networks of the current seller
data class SocialNetworksReading(val reader: Actor)

// An administrator who reads the current seller
data class SellerReading(val reader: Actor)

data class SellerAdministrationForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class InvalidSellerException(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)
data class SellerUpdateFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
data class SellerNotFoundException(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)

private val SIRET = Regex("[0-9]{14}")
private val VAT_NUMBER = Regex("[A-Z]{2}[0-9A-Z]{2,13}")
private val SHARE_CAPITAL = Regex("[0-9][0-9 ]*")
private val PHONE = Regex("\\+?[0-9][0-9 .()-]{5,19}")
private val EMAIL = Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
private val WEBSITE = Regex("(https?://)?[^\\s/.]+(\\.[^\\s/.]+)+(/\\S*)?")

// A public read refused: its reader is neither a visitor, nor a user, nor the system
data class SellerReadingForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
