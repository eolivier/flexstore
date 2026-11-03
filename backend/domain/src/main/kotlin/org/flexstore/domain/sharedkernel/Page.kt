package org.flexstore.domain.sharedkernel

// The number of a page of a list, from 0
data class PageNumber(val value: Int) {
    companion object {
        const val FIRST = 0

        fun first() = PageNumber(FIRST)

        // As asked by a client: a negative number is brought back to the first page
        fun of(requested: Int) = PageNumber(requested.coerceAtLeast(FIRST))
    }
}

// The number of items of a page, from 1 to the maximum
data class PageSize(val value: Int) {
    companion object {
        const val MINIMUM = 1
        const val DEFAULT = 20
        const val MAXIMUM = 100

        fun byDefault() = PageSize(DEFAULT)

        // As asked by a client: a size out of the limits is brought back to the nearest one
        fun clamped(requested: Int) = PageSize(requested.coerceIn(MINIMUM, MAXIMUM))
    }
}

// The page of a list that a user asks for
data class PageRequest(val number: PageNumber, val size: PageSize) {
    companion object {
        fun first() = PageRequest(PageNumber.first(), PageSize.byDefault())

        fun of(requestedNumber: Int, requestedSize: Int) =
            PageRequest(PageNumber.of(requestedNumber), PageSize.clamped(requestedSize))
    }
}

// The number of items of the whole list, whatever the page read
data class TotalCount(val value: Long)

// A page of a list (the orders, the users...), with the number of items of the whole list
data class Page<T>(val content: T, val total: TotalCount)
