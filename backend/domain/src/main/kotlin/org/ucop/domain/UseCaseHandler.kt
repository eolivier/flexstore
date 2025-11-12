package org.ucop.domain

import org.ucop.domain.entity.UseCase

interface UseCaseHandler<T> {
    fun setNext(useCaseHandler: UseCaseHandler<T>)
    fun handle(t : T)
    fun canHandle(t: T): Boolean
}

class EmptyUseCaseHandler<T> : UseCaseHandler<T> {
    override fun setNext(useCaseHandler: UseCaseHandler<T>) {}
    override fun handle(t: T) {}
    override fun canHandle(t: T) = false
}

class DefaultUseCaseHandler<T>(val useCase: UseCase<T>, private var nextUseCaseHandler: UseCaseHandler<T> = EmptyUseCaseHandler()) : UseCaseHandler<T> {

    override fun setNext(useCaseHandler: UseCaseHandler<T>) {
        nextUseCaseHandler = useCaseHandler
    }

    override fun handle(t: T) {
        if (canHandle(t)) {
            useCase.unfold(t)
        }
        nextUseCaseHandler.handle(t)
    }

    override fun canHandle(t: T): Boolean {
        try {
            useCase.getPreConditions().forEach { it.validate(t) }
        } catch (pce: PreConditionException) {
            return false
        }
        return true
    }
}
