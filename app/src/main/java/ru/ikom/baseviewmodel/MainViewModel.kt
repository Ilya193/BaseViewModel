package ru.ikom.baseviewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel<MainViewModel.Model>() {

    private val uiState = MutableStateFlow(Model())

    val action = MutableSharedFlow<Action>()

    init {
        viewModelScope.launch {
            action.emit(Action.Init(items = uiState.value.items))
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
        val oldState = uiState.value
        val items = uiState.value.items

        if (items[event.position].isSelected) return

        val newItems = items.mapIndexed { index, item ->
            if (index == event.position) item.copy(isSelected = true)
            else item.copy(isSelected = false)
        }

        val newState = oldState.copy(items = newItems, selectedItem = event.position)
        diff(oldState, newState)
    }

    private suspend fun handleEvent(event: Event.Recover) {
        val state = uiState.value
        action.emit(Action.OnSelectItem(items = state.items))
        if (state.selectedItem < 0) return
        action.emit(Action.UpdateText(text = state.items[state.selectedItem].text))
    }

    override suspend fun diff(old: Model, new: Model) {
        var change = false

        change = diff(
            old = old,
            new = new,
            get = Model::items,
            compare = { a, b -> a === b },
            set = { action.emit(Action.OnSelectItem(items = new.items)) }
        ) || change

        change = diff(
            old = old,
            new = new,
            get = Model::selectedItem,
            set = { action.emit(Action.UpdateText(text = new.items[new.selectedItem].text)) }
        ) || change

        if (change) {
            uiState.update { new }
        }
    }

    data class Model(
        val items: List<ItemUi> = generateItems(),
        val selectedItem: Int = -1,
    )

    sealed interface Event {
        class OnClick(
            val position: Int
        ) : Event

        class Recover : Event
    }

    sealed interface Action {
        class Init(
            val items: List<ItemUi>
        ) : Action

        class OnSelectItem(
            val items: List<ItemUi>
        ) : Action

        class UpdateText(
            val text: String
        ) : Action
    }
}