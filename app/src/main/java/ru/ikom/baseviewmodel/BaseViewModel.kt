package ru.ikom.baseviewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

abstract class BaseViewModel<State : Any, Msg : Any, Event: Any, Label: Any>(
    private val initialState: State,
) : ViewModel() {

    protected var uiState = initialState

    protected var observerState: Observer<State>? = null

    private var label: Label? = null
    private var observerLabel: Observer<Label>? = null

    val states: Flow<State> = callbackFlow {
        observerState = observer { channel.trySend(it) }
        awaitClose { observerState = null }
    }

    val labels: Flow<Label?> = callbackFlow {
        sendDataAndReset(channel, label)
        observerLabel = observer { sendDataAndReset(channel, label) }
        awaitClose { observerLabel = null }
    }

    abstract fun handleEvent(event: Event)

    protected fun dispatch(msg: Msg) {
        uiState = uiState.reduce(msg)
        observerState?.onNext(uiState)
    }

    protected fun publish(label: Label) {
        this.label = label
        observerLabel?.onNext(label)
    }

    private fun sendDataAndReset(channel: SendChannel<Label?>, label: Label?) {
        channel.trySend(label)
        this.label = null
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