package org.flexstore.infra.spring.config

import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.review.*
import org.flexstore.domain.review.usecase.*
import org.flexstore.domain.user.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the review sub-domain, declared step by step: use cases, then their group
@Configuration
open class ReviewUseCaseConfig {

    @Bean
    open fun addReviewUseCase(reviewRepository: ReviewRepository,
                              orderRepository: OrderRepository,
                              productRepository: ProductRepository,
                              userRepository: UserRepository) =
        AddReviewUseCase(reviewRepository, orderRepository, productRepository, userRepository)

    @Bean
    open fun listProductReviewsUseCase(reviewRepository: ReviewRepository) =
        ListProductReviewsUseCase(reviewRepository)

    @Bean
    open fun listRatingSummariesUseCase(reviewRepository: ReviewRepository) =
        ListRatingSummariesUseCase(reviewRepository)

    @Bean
    open fun deleteReviewUseCase(reviewRepository: ReviewRepository) = DeleteReviewUseCase(reviewRepository)

    @Bean
    open fun readReviewEligibilityUseCase(reviewRepository: ReviewRepository, orderRepository: OrderRepository) =
        ReadReviewEligibilityUseCase(reviewRepository, orderRepository)

    @Bean
    open fun reviewUseCases(addReviewUseCase: AddReviewUseCase,
                            listProductReviewsUseCase: ListProductReviewsUseCase,
                            listRatingSummariesUseCase: ListRatingSummariesUseCase,
                            deleteReviewUseCase: DeleteReviewUseCase,
                            readReviewEligibilityUseCase: ReadReviewEligibilityUseCase) =
        ReviewUseCases(
            addReviewUseCase, listProductReviewsUseCase, listRatingSummariesUseCase, deleteReviewUseCase,
            readReviewEligibilityUseCase
        )
}
