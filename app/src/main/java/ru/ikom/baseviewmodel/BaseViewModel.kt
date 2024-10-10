package ru.ikom.baseviewmodel

import androidx.lifecycle.ViewModel

abstract class BaseViewModel<Model : Any>(
    private val initialState: Model
) : ViewModel() {

    protected var uiState = initialState

}