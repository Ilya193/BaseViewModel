package ru.ikom.baseviewmodel

import androidx.lifecycle.ViewModel

abstract class BaseViewModel<State : Any, Msg : Any>(
    private val initialState: State
) : ViewModel() {

    protected var uiState = initialState

    protected abstract suspend fun dispatch(msg: Msg)

    protected abstract suspend fun State.reduce(msg: Msg): State

}