package org.flexstore.mcp

// The languages of the shop: the products not translated into one are given in English
enum class Language(val code: String) {
    EN("en"),
    FR("fr");

    companion object {
        fun byDefault() = EN

        fun isSupported(code: String) = entries.any { it.code == code }

        fun of(code: String): Language = entries.first { it.code == code }
    }
}
