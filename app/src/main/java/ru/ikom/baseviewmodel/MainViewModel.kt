package ru.ikom.baseviewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainViewModel :
    BaseViewModel<MainViewModel.State, MainViewModel.Msg>(initialState = State.initial()) {

    val action = MutableSharedFlow<Action>()

    init {
        viewModelScope.launch {
            action.emit(Action.Render(new = uiState.stateToModel()))
        }
    }

    fun handleEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.OnClick -> handleEvent(event)
                is Event.Recover -> handleEvent(event)
            }
        }
    }

    private suspend fun handleEvent(event: Event.OnClick) {
        val oldState = uiState
        val items = oldState.items

        if (items[event.position].isSelected) return

        val newItems = items.mapIndexed { index, item ->
            if (index == event.position) item.copy(isSelected = true)
            else item.copy(isSelected = false)
        }

        dispatch(Msg.NewItems(
            items = newItems,
            position = event.position,
            textTitle = newItems[event.position].text
        ))
    }

    private suspend fun handleEvent(event: Event.Recover) {
        action.emit(Action.Render(uiState.stateToModel()))
    }

    override suspend fun dispatch(msg: Msg) {
        uiState = uiState.reduce(msg)
        action.emit(Action.Render(uiState.stateToModel()))
    }

    override suspend fun State.reduce(msg: Msg): State =
        when (msg) {
            is Msg.NewItems ->
                copy(
                    items = msg.items,
                    selectedItem = msg.position,
                    textTitle = msg.textTitle
                )
        }

    data class State(
        val items: List<ItemUi>,
        val selectedItem: Int,
        val textTitle: String,
    ) {
        companion object {
            fun initial(): State =
                State(
                    items = generateItems(),
                    selectedItem = -1,
                    textTitle = ""
                )
        }
    }

    sealed interface Event {
        class OnClick(
            val position: Int
        ) : Event

        class Recover : Event
    }

    sealed interface Action {

        class Render(
            val new: MainView.Model
        ) : Action
    }

    sealed interface Msg {
        class NewItems(
            val items: List<ItemUi>,
            val position: Int,
            val textTitle: String,
        ) : Msg
    }
}