package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.discount.*
import org.flexstore.domain.email.*
import org.flexstore.domain.email.model.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.product.*
import org.flexstore.domain.image.*
import org.flexstore.domain.legal.*
import org.flexstore.domain.pagecontent.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.restock.*
import org.flexstore.domain.review.*
import org.flexstore.domain.sales.*
import org.flexstore.domain.environment.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.twofactor.*
import org.flexstore.domain.twofactor.model.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import com.fasterxml.jackson.databind.JsonMappingException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.ucop.domain.NominalException
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException
import org.flexstore.domain.user.address.AddressNotFoundException
import org.flexstore.domain.user.address.IncompleteAddressException

@RestControllerAdvice
class RestExceptionHandler {

    private val logger = LoggerFactory.getLogger(RestExceptionHandler::class.java)

    @ExceptionHandler(NominalException::class)
    fun handleNominalException(nominal: NominalException): ResponseEntity<Map<String, String>> {
        // A precondition or a postcondition not met is answered for its precise exception, which is the cause of
        // the failure
        val ex: Exception = when (val cause = nominal.cause) {
            is PreConditionException -> cause
            is PostConditionException -> cause
            else -> nominal
        }
        return answerFor(ex)
    }

    // A precondition not met that reaches the adapter without a use case around it (an invalid value read in
    // a request body, a rule of the model): answered for itself
    @ExceptionHandler(PreConditionException::class)
    fun handlePreConditionException(precondition: PreConditionException): ResponseEntity<Map<String, String>> =
        answerFor(precondition)

    // One table: each exception of the domain is answered with its status, grouped by status. The message is the
    // one of the exception, the fallback only stands in when it has none
    private fun answerFor(ex: Exception): ResponseEntity<Map<String, String>> = when (ex) {
        // 400: a request the domain refuses. The reasons tell these answers from a malformed request, from an
        // incomplete address and from a lack of stock
        // Not 422, which tells the front that the cart is empty
        is IncompleteOrderAddressException ->
            reply(HttpStatus.BAD_REQUEST, ex, "Incomplete address", "reason" to INCOMPLETE_ADDRESS)
        is InvalidCurrentPasswordException -> reply(HttpStatus.BAD_REQUEST, ex, "Wrong current password")
        is InvalidCompanyException ->
            reply(HttpStatus.BAD_REQUEST, ex, "Invalid company", "reason" to INVALID_COMPANY)
        is IncompleteAddressException -> reply(HttpStatus.BAD_REQUEST, ex, "Incomplete address")
        is InvalidOrderTrackingException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid tracking")
        is InvalidSkuException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid SKU")
        is InvalidPromotionException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid promotion")
        is InvalidFreeShippingThresholdException ->
            reply(HttpStatus.BAD_REQUEST, ex, "Invalid threshold of free shipping")
        is InvalidSellerException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid seller")
        is InvalidLegalPageException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid legal page")
        is InvalidShopPageContentException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid shop page content")
        is InvalidAbandonedCartDelayException ->
            reply(HttpStatus.BAD_REQUEST, ex, "Invalid delay of the abandoned cart reminder")
        is InvalidPaymentDelayException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid payment delay")
        // The front asks the customer to accept the terms of sale
        is TermsOfSaleNotAcceptedException ->
            reply(HttpStatus.BAD_REQUEST, ex, "Terms of sale not accepted", "reason" to TERMS_NOT_ACCEPTED)
        is NegativeStockException -> reply(HttpStatus.BAD_REQUEST, ex, "Negative stock")
        is NegativeAlertThresholdException -> reply(HttpStatus.BAD_REQUEST, ex, "Negative alert threshold")
        is InvalidDiscountCodeException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid discount code")
        is InvalidReviewException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid review")
        is InvalidSalesPeriodException -> reply(HttpStatus.BAD_REQUEST, ex, "Invalid period of sales")

        // 401: same answer for an unknown email and a wrong password, so that the login does not reveal which
        // accounts exist
        is InvalidCredentialsException, is UserNotFoundByEmailException, is InvalidGoogleIdTokenException,
        is UserSignInException, is UserGoogleAuthenticationException, is SystemSignInForbiddenException ->
            replyWith(HttpStatus.UNAUTHORIZED, "Invalid credentials.")
        // The front signs the user out: they sign in again
        is RefreshTokenUnknownException, is RefreshTokenExpiredException, is RefreshTokenReusedException ->
            reply(HttpStatus.UNAUTHORIZED, ex, "Refresh token refused")
        // The code is wrong, or the sign-in challenge is unknown or older than its validity
        is TwoFactorCodeInvalidException, is TwoFactorChallengeNotFoundException ->
            reply(HttpStatus.UNAUTHORIZED, ex, "Invalid two-factor code")

        // 403: the password is right, but the account is suspended, locked or disabled: the front tells the user
        // why
        is AccountSignInRefusedException ->
            reply(HttpStatus.FORBIDDEN, ex, "Sign-in refused", "status" to ex.status.name)
        is AccountRefusedException ->
            reply(HttpStatus.FORBIDDEN, ex, "Account refused", "status" to ex.status.name)
        is OrderRefusedException ->
            reply(HttpStatus.FORBIDDEN, ex, "Order refused", "status" to ex.status.name)
        is UserCreationForbiddenException, is UserUpdateForbiddenException, is SystemRoleForbiddenException,
        is UserReadingForbiddenException, is UserDeletionForbiddenException,
        is LegalPagePublicationForbiddenException, is StockAlertForbiddenException,
        is SellerAdministrationForbiddenException, is ShopSettingsAdministrationForbiddenException,
        is DiscountCodeAdministrationForbiddenException, is ProductAdministrationForbiddenException,
        is ProductImageForbiddenException, is StockChangeForbiddenException,
        is ReviewModerationForbiddenException, is OrderAdministrationForbiddenException,
        is CreditNoteForbiddenException, is SalesDashboardForbiddenException,
        is EnvironmentAdministrationForbiddenException, is ShopPageContentForbiddenException,
        is TwoFactorAuthenticationForbiddenException,
        is SellerReadingForbiddenException, is ReviewReadingForbiddenException,
        is LegalPageReadingForbiddenException, is ProductImageReadingForbiddenException,
        is ShopSettingsReadingForbiddenException, is ShopPageContentReadingForbiddenException,
        is ProductReadingForbiddenException, is StockReadingForbiddenException,
        is RestockNotificationForbiddenException ->
            reply(HttpStatus.FORBIDDEN, ex, "Forbidden")
        // Only a customer who bought the product can review it
        is ReviewNotAllowedException -> reply(HttpStatus.FORBIDDEN, ex, "Review not allowed")

        // 404
        is ProductNotFoundException, is OrderNotFoundException, is AddressNotFoundException ->
            reply(HttpStatus.NOT_FOUND, ex, "Not found")
        is VerificationLinkUnknownException -> reply(HttpStatus.NOT_FOUND, ex, "Unknown verification link")
        is PasswordResetLinkUnknownException ->
            reply(HttpStatus.NOT_FOUND, ex, "Unknown link to choose a new password")
        is DiscountCodeNotFoundException -> reply(HttpStatus.NOT_FOUND, ex, "Unknown discount code")
        is ReviewNotFoundException -> reply(HttpStatus.NOT_FOUND, ex, "Unknown review")
        is InvoiceNotFoundException -> reply(HttpStatus.NOT_FOUND, ex, "No invoice")
        is CreditNoteNotFoundException -> reply(HttpStatus.NOT_FOUND, ex, "No credit note")
        is ProductImageNotFoundException -> reply(HttpStatus.NOT_FOUND, ex, "No image")

        // 409
        is UserAlreadyExists -> reply(HttpStatus.CONFLICT, ex, "User already exists")
        is TwoFactorAuthenticationAlreadyEnabledException, is TwoFactorEnrollmentNotPendingException,
        is TwoFactorAuthenticationNotEnabledException ->
            reply(HttpStatus.CONFLICT, ex, "Two-factor authentication conflict")
        // The order changed status in the meantime, or is already delivered, or is shipped (no refund): the
        // front reloads the orders
        is OrderStatusChangedException, is OrderStatusCannotAdvanceException, is OrderNotRefundableException ->
            reply(HttpStatus.CONFLICT, ex, "The status of the order cannot advance")
        // The order is not shipped yet, or is cancelled or refunded
        is OrderTrackingCannotBeCorrectedException ->
            reply(HttpStatus.CONFLICT, ex, "The tracking of the order cannot be corrected")
        // The administrator chooses another SKU
        is SkuAlreadyUsedException -> reply(HttpStatus.CONFLICT, ex, "SKU already used")
        // Not paid in time: the customer orders again
        is OrderCancelledException -> reply(HttpStatus.CONFLICT, ex, "Cancelled order")
        // The customer reads the terms of sale again
        is OutdatedTermsOfSaleException ->
            reply(HttpStatus.CONFLICT, ex, "Terms of sale changed", "reason" to OUTDATED_TERMS)
        // Not 422, which tells the front that the cart is empty
        is OutOfStockException, is StockReservationFailed ->
            reply(HttpStatus.CONFLICT, ex, "Not enough units in stock")
        // The product is in stock: the customer can buy it, there is nothing to wait for
        is RestockSubscriptionRefusedException, is RestockNotificationRefusedException ->
            reply(HttpStatus.CONFLICT, ex, "The product is in stock")
        is DiscountCodeAlreadyExistsException -> reply(HttpStatus.CONFLICT, ex, "Discount code already given")
        is ReviewAlreadyWrittenException -> reply(HttpStatus.CONFLICT, ex, "Product already reviewed")
        is OrderAlreadyPaidException -> reply(HttpStatus.CONFLICT, ex, "Order already paid")
        is AccountAlreadyVerifiedException -> reply(HttpStatus.CONFLICT, ex, "Email already verified")

        // 410: the link or the code was right but can no longer be used: the customer can ask for a new one
        is PasswordResetLinkExpiredException ->
            reply(HttpStatus.GONE, ex, "Expired link to choose a new password")
        is VerificationLinkExpiredException -> reply(HttpStatus.GONE, ex, "Expired verification link")
        // The code existed, but is expired, or used by as many orders as allowed
        is DiscountCodeExpiredException, is DiscountCodeUsedUpException ->
            reply(HttpStatus.GONE, ex, "Discount code no longer usable")

        is ImageTooLargeException -> reply(HttpStatus.PAYLOAD_TOO_LARGE, ex, "Image too large")
        is UnreadableImageException -> reply(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex, "Unreadable image")
        // The payment provider refused the refund: the order stays as it is
        is PaymentRefundRefusedException ->
            reply(HttpStatus.BAD_GATEWAY, ex, "Refund refused by the payment provider")
        else -> reply(HttpStatus.UNPROCESSABLE_ENTITY, ex, "Nominal exception")
    }

    private fun reply(
        status: HttpStatus,
        ex: Exception,
        fallbackMessage: String,
        vararg extraFields: Pair<String, String>
    ) = replyWith(status, ex.message ?: fallbackMessage, *extraFields)

    private fun replyWith(
        status: HttpStatus,
        message: String,
        vararg extraFields: Pair<String, String>
    ): ResponseEntity<Map<String, String>> {
        val fields = mapOf("error" to message, *extraFields)
        return ResponseEntity.status(status).body(fields)
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
        val malformed = "The request is malformed."
        val fieldMalformed = "The field $field is missing or malformed."
        val error = if (field.isEmpty()) malformed else fieldMalformed
        logger.warn("Unreadable request body: {} ({})", error, causeMessage.lineSequence().first())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to error, "reason" to MALFORMED_REQUEST))
    }

    // An uploaded file larger than spring.servlet.multipart.max-file-size, refused before reaching the domain
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleTooLargeUpload(ex: MaxUploadSizeExceededException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(mapOf("error" to "An image is 2 MB at most."))
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
