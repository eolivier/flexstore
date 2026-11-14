package org.flexstore.domain.user.model

import org.flexstore.domain.sharedkernel.PageRequest

// The words an administrator types to find users: each word is part of the name or of the email of the user
data class UserSearchText(val value: String) {

    fun isBlank() = value.isBlank()

    // Read-only view for the adapters that translate the search (SQL): the words, in lower case
    fun asWords(): List<String> = value.lowercase().split(WORD_SEPARATOR).filter { word -> word.isNotBlank() }

    fun matches(user: User.DefinedUser): Boolean {
        val searchedText = "${user.fullName.value} ${user.email.value}".lowercase()
        return asWords().all { word -> searchedText.contains(word) }
    }

    companion object {
        private val WORD_SEPARATOR = Regex("\\s+")

        fun none() = UserSearchText("")
    }
}

// The page of the users an administrator asks for, among the users that match the text (all of them without text)
data class UsersSearch(val searchText: UserSearchText, val pageRequest: PageRequest) {

    constructor(pageRequest: PageRequest) : this(UserSearchText.none(), pageRequest)
}
