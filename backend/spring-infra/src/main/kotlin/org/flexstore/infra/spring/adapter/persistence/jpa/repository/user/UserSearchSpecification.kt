package org.flexstore.infra.spring.adapter.persistence.jpa.repository.user

import org.flexstore.domain.user.model.UserSearchText
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.springframework.data.jpa.domain.Specification

private const val LIKE_ESCAPE = '\\'

// Every word of the search is part of the first name, the last name or the email of the user, whatever the case
fun UserSearchText.toSpecification(): Specification<UserEntity> =
    Specification { user, _, criteriaBuilder ->
        val wordConditions = asWords().map { word ->
            val pattern = "%${word.escapedForLike()}%"
            val firstNameAndSpace = criteriaBuilder.concat(user.get("firstName"), " ")
            val fullName = criteriaBuilder.concat(firstNameAndSpace, user.get("lastName"))
            criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(fullName), pattern, LIKE_ESCAPE),
                criteriaBuilder.like(criteriaBuilder.lower(user.get("email")), pattern, LIKE_ESCAPE)
            )
        }
        criteriaBuilder.and(*wordConditions.toTypedArray())
    }

// The characters of the word are searched as they are, not as wildcards
private fun String.escapedForLike() = replace("$LIKE_ESCAPE", "$LIKE_ESCAPE$LIKE_ESCAPE")
    .replace("%", "$LIKE_ESCAPE%")
    .replace("_", "${LIKE_ESCAPE}_")
