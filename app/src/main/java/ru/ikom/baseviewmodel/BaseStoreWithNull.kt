package ru.ikom.baseviewmodel

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

abstract class BaseStoreWithNull<T: Any?>(initial: T?) {
    open var state = initial

    protected open var observers = mutableListOf<Observer<T>>()

    open val states: Flow<T?> = callbackFlow {
        channel.trySend(state)
        val observer: Observer<T> = observer { channel.trySend(it) }
        observers.add(observer)
        awaitClose { observers.remove(observer) }
    }

    open fun init(data: T) {
        state = data
        observers.forEach { it.onNext(data) }
    }

    open fun updateIfNotNull(block: (T) -> T) {
        val state = this.state ?: return
        val newState = block(state)
        this.state = newState
        observers.forEach { it.onNext(newState) }
    }

    open fun initSavedState(savedStateHandle: SavedStateHandle) {}

}