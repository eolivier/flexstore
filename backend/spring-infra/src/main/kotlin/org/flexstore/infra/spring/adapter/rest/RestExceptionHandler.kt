package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.discount.*
import org.flexstore.domain.email.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.product.*
import org.flexstore.domain.image.*
import org.flexstore.domain.legal.*
import org.flexstore.domain.review.*
import org.flexstore.domain.sales.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.twofactor.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import com.fasterxml.jackson.databind.JsonMappingException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.ucop.domain.NominalException

@RestControllerAdvice
class RestExceptionHandler {

    private val logger = LoggerFactory.getLogger(RestExceptionHandler::class.java)

    @ExceptionHandler(NominalException::class)
    fun handleNominalException(ex: NominalException): ResponseEntity<Map<String, String>> {
        return when (ex) {
            is UserAlreadyExists -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "User already exists")))
            // Same answer for an unknown email and a wrong password, so that the login does not reveal which
            // accounts exist
            is InvalidCredentialsException, is UserNotFoundByEmailException -> ResponseEntity
                .status(401)
                .body(mapOf("error" to "Invalid credentials."))
            // The password is right, but the account is suspended, locked or disabled: the front tells the user
            // why
            // The front signs the user out: they sign in again
            is RefreshTokenUnknownException, is RefreshTokenExpiredException, is RefreshTokenReusedException ->
                ResponseEntity
                    .status(401)
                    .body(mapOf("error" to (ex.message ?: "Refresh token refused")))
            is AccountSignInRefusedException -> ResponseEntity
                .status(403)
                .body(mapOf("error" to (ex.message ?: "Sign-in refused"), "status" to ex.status.name))
            is AccountRefusedException -> ResponseEntity
                .status(403)
                .body(mapOf("error" to (ex.message ?: "Account refused"), "status" to ex.status.name))
            is OrderRefusedException -> ResponseEntity
                .status(403)
                .body(mapOf("error" to (ex.message ?: "Order refused"), "status" to ex.status.name))
            is UserCreationForbiddenException, is UserUpdateForbiddenException,
            is UserReadingForbiddenException, is UserDeletionForbiddenException,
            is LegalPagePublicationForbiddenException, is StockAlertForbiddenException,
            is SellerAdministrationForbiddenException, is ShopSettingsAdministrationForbiddenException,
            is DiscountCodeAdministrationForbiddenException, is ProductAdministrationForbiddenException,
            is ProductImageForbiddenException, is StockChangeForbiddenException,
            is ReviewModerationForbiddenException, is OrderAdministrationForbiddenException,
            is CreditNoteForbiddenException, is SalesDashboardForbiddenException,
            is TwoFactorAuthenticationForbiddenException -> ResponseEntity
                .status(403)
                .body(mapOf("error" to (ex.message ?: "Forbidden")))
            // The code is wrong, or the sign-in challenge is unknown or older than its validity
            is TwoFactorCodeInvalidException, is TwoFactorChallengeNotFoundException -> ResponseEntity
                .status(401)
                .body(mapOf("error" to (ex.message ?: "Invalid two-factor code")))
            is TwoFactorAuthenticationAlreadyEnabledException, is TwoFactorEnrollmentNotPendingException,
            is TwoFactorAuthenticationNotEnabledException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Two-factor authentication conflict")))
            // Not 422, which tells the front that the cart is empty. The reason tells this 400 from a malformed
            // request
            is IncompleteOrderAddressException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Incomplete address"), "reason" to INCOMPLETE_ADDRESS))
            is InvalidCurrentPasswordException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Wrong current password")))
            // The reason tells this 400 from a malformed request and from an incomplete address
            is InvalidCompanyException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid company"), "reason" to INVALID_COMPANY))
            is IncompleteAddressException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Incomplete address")))
            is ProductNotFoundException, is OrderNotFoundException, is AddressNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Not found")))
            is VerificationLinkUnknownException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Unknown verification link")))
            is PasswordResetLinkUnknownException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Unknown link to choose a new password")))
            // The link was right but is too old: the customer can ask for a new one
            is PasswordResetLinkExpiredException -> ResponseEntity
                .status(410)
                .body(mapOf("error" to (ex.message ?: "Expired link to choose a new password")))
            // The link was right but is too old: the customer can ask for a new one
            is VerificationLinkExpiredException -> ResponseEntity
                .status(410)
                .body(mapOf("error" to (ex.message ?: "Expired verification link")))
            // The order changed status in the meantime, or is already delivered, or is shipped (no refund): the
            // front reloads the orders
            is OrderStatusChangedException, is OrderStatusCannotAdvanceException,
            is OrderNotRefundableException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "The status of the order cannot advance")))
            is InvalidOrderTrackingException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid tracking")))
            // The order is not shipped yet, or is cancelled or refunded
            is OrderTrackingCannotBeCorrectedException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "The tracking of the order cannot be corrected")))
            is InvalidSkuException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid SKU")))
            is InvalidPromotionException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid promotion")))
            // The administrator chooses another SKU
            is SkuAlreadyUsedException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "SKU already used")))
            is InvalidFreeShippingThresholdException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid threshold of free shipping")))
            is InvalidSellerException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid seller")))
            is InvalidLegalPageException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid legal page")))
            is InvalidPaymentDelayException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid payment delay")))
            // Not paid in time: the customer orders again
            is OrderCancelledException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Cancelled order")))
            // The reasons tell these answers from a malformed request and from a lack of stock: the front asks
            // the customer to accept the terms of sale, or to read them again
            is TermsOfSaleNotAcceptedException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Terms of sale not accepted"), "reason" to TERMS_NOT_ACCEPTED))
            is OutdatedTermsOfSaleException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Terms of sale changed"), "reason" to OUTDATED_TERMS))
            // Not 422, which tells the front that the cart is empty
            is OutOfStockException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Not enough units in stock")))
            is NegativeStockException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Negative stock")))
            is NegativeAlertThresholdException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Negative alert threshold")))
            is ImageTooLargeException -> ResponseEntity
                .status(413)
                .body(mapOf("error" to (ex.message ?: "Image too large")))
            is UnreadableImageException -> ResponseEntity
                .status(415)
                .body(mapOf("error" to (ex.message ?: "Unreadable image")))
            is InvalidDiscountCodeException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid discount code")))
            is DiscountCodeAlreadyExistsException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Discount code already given")))
            is DiscountCodeNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Unknown discount code")))
            // The code existed, but can no longer be used: expired, or used by as many orders as allowed
            is DiscountCodeExpiredException, is DiscountCodeUsedUpException -> ResponseEntity
                .status(410)
                .body(mapOf("error" to (ex.message ?: "Discount code no longer usable")))
            is InvalidReviewException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid review")))
            // Only a customer who bought the product can review it
            is ReviewNotAllowedException -> ResponseEntity
                .status(403)
                .body(mapOf("error" to (ex.message ?: "Review not allowed")))
            is ReviewNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "Unknown review")))
            is ReviewAlreadyWrittenException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Product already reviewed")))
            is InvalidSalesPeriodException -> ResponseEntity
                .status(400)
                .body(mapOf("error" to (ex.message ?: "Invalid period of sales")))
            is InvoiceNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "No invoice")))
            is CreditNoteNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "No credit note")))
            is ProductImageNotFoundException -> ResponseEntity
                .status(404)
                .body(mapOf("error" to (ex.message ?: "No image")))
            // The payment provider refused the refund: the order stays as it is
            is PaymentRefundRefusedException -> ResponseEntity
                .status(502)
                .body(mapOf("error" to (ex.message ?: "Refund refused by the payment provider")))
            is OrderAlreadyPaidException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Order already paid")))
            is AccountAlreadyVerifiedException -> ResponseEntity
                .status(409)
                .body(mapOf("error" to (ex.message ?: "Email already verified")))
            else -> ResponseEntity
                .status(422)
                .body(mapOf("error" to (ex.message ?: "Nominal exception")))
        }
    }

    // A body that cannot be read (a field missing, or of the wrong type), refused before reaching the domain:
    // the field is named in the answer, and logged so that the cause of the refusal can be found
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, String>> {
        val mappingException = ex.cause as? JsonMappingException
        val references = mappingException?.path.orEmpty()
        val path = references.map { reference -> reference.fieldName ?: "${reference.index}" }
        // A field missing reaches the Kotlin constructor as null, which names it: the path stops at its object
        val causeMessage = ex.mostSpecificCause.message.orEmpty()
        val missingParameter = MISSING_PARAMETER.find(causeMessage)?.groupValues?.get(1)
        val field = (path + listOfNotNull(missingParameter)).joinToString(".")
        val error = if (field.isEmpty()) "The request is malformed." else "The field $field is missing or malformed."
        logger.warn("Unreadable request body: {} ({})", error, causeMessage.lineSequence().first())
        return ResponseEntity.status(400).body(mapOf("error" to error, "reason" to MALFORMED_REQUEST))
    }

    // An uploaded file larger than spring.servlet.multipart.max-file-size, refused before reaching the domain
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleTooLargeUpload(ex: MaxUploadSizeExceededException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(413).body(mapOf("error" to "An image is 2 MB at most."))
}

// The reasons of a 400 or a 409, for the front to tell them apart: a request the domain refuses, or one it never
// reads
private const val INCOMPLETE_ADDRESS = "INCOMPLETE_ADDRESS"
private const val TERMS_NOT_ACCEPTED = "TERMS_OF_SALE_NOT_ACCEPTED"
private const val OUTDATED_TERMS = "OUTDATED_TERMS_OF_SALE"
private const val MALFORMED_REQUEST = "MALFORMED_REQUEST"
private const val INVALID_COMPANY = "INVALID_COMPANY"

// The message of Kotlin when a non-null parameter receives null: "..., parameter street"
private val MISSING_PARAMETER = Regex("Parameter specified as non-null is null: .*, parameter (\\w+)$")
