package org.flexstore.domain.user.model

data class FirstName(val value: String)

data class LastName(val value: String)

// The first name and the last name of a person
data class FullName(val firstName: FirstName, val lastName: LastName) {

    // "First name Last name", as the person is greeted; a missing part is left out
    val value: String
        get() = listOf(firstName.value, lastName.value).filter { it.isNotBlank() }.joinToString(" ")

    fun isComplete() = firstName.value.isNotBlank() && lastName.value.isNotBlank()
}
