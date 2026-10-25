package org.flexstore.domain.sharedkernel

// The address of the shop (its front), where the links sent to the customers lead
data class ShopUrl(val value: String) {

    // The address of a page of the shop, whether the address of the shop ends with a slash or not
    fun pageAt(path: String) = "${value.trimEnd('/')}/${path.trimStart('/')}"
}
