package org.flexstore.domain.order.model

import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException

// The name of a carrier, as told to the customer
data class CarrierName(val value: String)

// The page of a carrier where the customer follows their parcel
data class TrackingPage(val value: String)

// The carriers an administrator chooses from when an order is shipped. Each one knows the tracking page of its
// parcels, except OTHER: the customer then only reads the tracking number
enum class Carrier(private val frenchName: String, private val trackingPageTemplate: String) {
    COLISSIMO("Colissimo", "https://www.laposte.fr/outils/suivre-vos-envois?code=$NUMBER_PLACEHOLDER"),
    CHRONOPOST(
        "Chronopost", "https://www.chronopost.fr/tracking-no-cms/suivi-page?listeNumerosLT=$NUMBER_PLACEHOLDER"
    ),
    UPS("UPS", "https://www.ups.com/track?tracknum=$NUMBER_PLACEHOLDER"),
    DHL("DHL", "https://www.dhl.com/fr-fr/home/tracking.html?tracking-id=$NUMBER_PLACEHOLDER"),
    FEDEX("FedEx", "https://www.fedex.com/fedextrack/?trknbr=$NUMBER_PLACEHOLDER"),
    OTHER("Autre transporteur", "");

    fun displayName() = CarrierName(frenchName)

    fun hasTrackingPage() = trackingPageTemplate.isNotEmpty()

    // Only for a carrier that has a tracking page
    fun trackingPageOf(trackingNumber: TrackingNumber) =
        TrackingPage(trackingPageTemplate.replace(NUMBER_PLACEHOLDER, trackingNumber.value))

    companion object {
        // As typed by the administrator: upper or lower case
        fun from(typedCarrier: String) =
            entries.firstOrNull { it.name == typedCarrier.trim().uppercase() }
                ?: throw InvalidOrderTrackingException(ErrorMessage("Unknown carrier: $typedCarrier."))
    }
}

private const val NUMBER_PLACEHOLDER = "{number}"

// The number that identifies a parcel with its carrier: letters, digits and hyphens, without spaces
data class TrackingNumber(val value: String) {

    fun isValid() = value.length <= MAX_LENGTH && WELL_FORMED.matches(value)

    fun isInvalid() = !isValid()

    companion object {
        const val MAX_LENGTH = 40
        private val WELL_FORMED = Regex("[A-Za-z0-9-]+")

        // As typed: the spaces around are dropped
        fun from(typedNumber: String) = TrackingNumber(typedNumber.trim())
    }
}

// How the parcel of an order is followed: the carrier and the tracking number given together, or nothing
sealed interface OrderTracking {

    fun isGiven(): Boolean

    fun isInvalid(): Boolean

    data class Given(val carrier: Carrier, val trackingNumber: TrackingNumber) : OrderTracking {

        override fun isGiven() = true

        override fun isInvalid() = trackingNumber.isInvalid()

        fun hasTrackingPage() = carrier.hasTrackingPage()

        // Only for a carrier that has a tracking page
        fun trackingPage() = carrier.trackingPageOf(trackingNumber)
    }

    // An order not shipped yet, or shipped without tracking
    data object None : OrderTracking {
        override fun isGiven() = false

        override fun isInvalid() = false
    }

    companion object {
        // As typed by an administrator: both the carrier and the number, or none of them
        fun typed(typedCarrier: String, typedNumber: String): OrderTracking {
            val carrierIsMissing = typedCarrier.isBlank()
            val numberIsMissing = typedNumber.isBlank()
            return when {
                carrierIsMissing && numberIsMissing -> None
                carrierIsMissing || numberIsMissing -> throw InvalidOrderTrackingException(
                    ErrorMessage("The carrier and the tracking number are given together.")
                )
                else -> Given(Carrier.from(typedCarrier), TrackingNumber.from(typedNumber))
            }
        }
    }
}

// An administrator corrects the carrier and the tracking number of an order shipped
data class OrderTrackingCorrection(
    val editor: UserId,
    val orderId: OrderId,
    val tracking: OrderTracking.Given
)

data class InvalidOrderTrackingException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderTrackingCannotBeCorrectedException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderTrackingChangeFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
