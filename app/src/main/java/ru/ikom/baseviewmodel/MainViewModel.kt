package ru.ikom.baseviewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel<MainViewModel.State>(initialState = State.initial()) {

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

        val newState = oldState.copy(
            items = newItems,
            selectedItem = event.position,
            textTitle = newItems[event.position].text
        )
        action.emit(Action.Render(newState.stateToModel()))
        uiState = newState
    }

    private suspend fun handleEvent(event: Event.Recover) {
        val newState = uiState
        action.emit(Action.Render(newState.stateToModel()))
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
}