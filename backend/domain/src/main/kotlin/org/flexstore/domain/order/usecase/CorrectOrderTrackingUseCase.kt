package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator corrects the carrier and the tracking number of an order, while it is shipped or delivered
class CorrectOrderTrackingUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : UseCase<OrderTrackingCorrection> {

    override fun getPreConditions(): PreConditions<OrderTrackingCorrection> = PreConditions(
        editorIsAdministratorCondition(),
        orderExistsCondition(),
        orderAllowsTheCorrectionCondition(),
        trackingIsValidCondition()
    )

    override fun getNominalScenario(): NominalScenario<OrderTrackingCorrection> =
        NominalScenario(Step { correction ->
            val order = orderRepository.findById(correction.orderId)!!
            orderRepository.save(order.withTracking(correction.tracking))
        })

    override fun getPostConditions(): PostConditions<OrderTrackingCorrection> =
        PostConditions(trackingIsKeptCondition())

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<OrderTrackingCorrection> { correction ->
        if (userRepository.isNotAdministrator(correction.editor)) {
            throw OrderAdministrationForbiddenException(
                ErrorMessage("Only an administrator can correct the tracking of an order.")
            )
        }
    }

    private fun orderExistsCondition() = PreCondition<OrderTrackingCorrection> { correction ->
        if (orderRepository.orderIsMissing(correction.orderId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${correction.orderId.value} not found."))
        }
    }

    private fun orderAllowsTheCorrectionCondition() = PreCondition<OrderTrackingCorrection> { correction ->
        val order = orderRepository.findById(correction.orderId)!!
        if (order.status.refusesTrackingCorrection()) {
            val message = "The tracking of order ${order.number()} is corrected once it is shipped, not when it is " +
                "${order.status}."
            throw OrderTrackingCannotBeCorrectedException(ErrorMessage(message))
        }
    }

    private fun trackingIsValidCondition() = PreCondition<OrderTrackingCorrection> { correction ->
        if (correction.tracking.isInvalid()) {
            val message = "A tracking number is letters, digits and hyphens, ${TrackingNumber.MAX_LENGTH} at most."
            throw InvalidOrderTrackingException(ErrorMessage(message))
        }
    }

    private fun trackingIsKeptCondition() = PostCondition<OrderTrackingCorrection> { correction ->
        if (orderRepository.findById(correction.orderId)?.tracking != correction.tracking) {
            val message = "The tracking of order ${correction.orderId.value} was not corrected."
            throw OrderTrackingChangeFailed(ErrorMessage(message))
        }
    }
}
