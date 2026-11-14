package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException

data class UnpaidOrderForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)

data class OrderNotAwaitingPaymentException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class PaymentTimeLimitNotExceededException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderCancelledException(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)
data class OrderCancellationFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
data class EmptyCartException(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)
data class IncompleteOrderAddressException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderRefusedException(val status: AccountStatus, override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderStatusCannotAdvanceException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderStatusChangedException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderStatusAdvanceFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
data class OrderNotRefundableException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderRefundFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
data class OrderAdministrationForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderNotFoundException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage) {
    constructor(message: String) : this(ErrorMessage(message))
}
data class TermsOfSaleNotAcceptedException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OutdatedTermsOfSaleException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class OrderPlacementFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
