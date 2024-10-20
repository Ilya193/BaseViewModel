package ru.ikom.baseviewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

abstract class BaseViewModel<State : Any, Msg : Any>(
    private val initialState: State
) : ViewModel() {

    protected var uiState = initialState

    protected var observer: Observer<State>? = null

    val states: Flow<State> = callbackFlow {
        observer = observer { channel.trySend(it) }
        awaitClose { observer = null }
    }

    protected fun dispatch(msg: Msg) {
        uiState = uiState.reduce(msg)
        observer?.onNext(uiState)
    }

    protected abstract fun State.reduce(msg: Msg): State

}

interface Observer<T> {
    fun onNext(value: T)
}

inline fun <T> observer(crossinline onNext: (T) -> Unit): Observer<T> = object : Observer<T> {
    override fun onNext(value: T) {
        onNext(value)
    }

}

suspend inline infix fun <T> Flow<T>.bindTo(crossinline action: (T) -> Unit) {
    collect {
        action(it)
    }
}