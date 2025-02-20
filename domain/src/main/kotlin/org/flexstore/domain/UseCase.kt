package org.flexstore.domain

interface UseCase<T> {

    fun preConditions()
    fun nominal()
    fun alternative()
    fun postConditions()

    fun scenario(t: T) {
        preConditions()
        try {
            nominal()
        } catch (ne: NominalException) {
            alternative()
        }
        postConditions()
    }
}