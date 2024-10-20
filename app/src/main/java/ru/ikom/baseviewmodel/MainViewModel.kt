package ru.ikom.baseviewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainViewModel :
    BaseViewModel<MainViewModel.State, MainViewModel.Msg>(initialState = State.initial()) {

    fun handleEvent(event: Event) {
        when (event) {
            is Event.OnClick -> handleEvent(event)
            is Event.OnViewCreated -> handleEvent(event)
        }
    }

    private fun handleEvent(event: Event.OnClick) {
        val oldState = uiState
        val items = oldState.items

        if (items[event.position].isSelected) return

        viewModelScope.launch {
            val newItems = async(Dispatchers.IO) {
                items.mapIndexed { index, item ->
                    if (index == event.position) item.copy(isSelected = true)
                    else item.copy(isSelected = false)
                }
            }.await()

            dispatch(Msg.NewItems(
                items = newItems,
                position = event.position,
                textTitle = newItems[event.position].text
            ))
        }
    }

    private fun handleEvent(event: Event.OnViewCreated) {
        observer?.onNext(uiState)
    }

    override fun State.reduce(msg: Msg): State =
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

        class OnViewCreated : Event
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