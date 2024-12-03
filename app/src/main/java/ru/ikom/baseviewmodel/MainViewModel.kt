package ru.ikom.baseviewmodel

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainViewModel(
    private val savedState: SavedStateHandle,
) : BaseViewModel<MainViewModel.State, MainViewModel.Msg, Any>(initialState = State.initial()) {

    init {
        val initState = savedState.get<Bundle>(State.UI_STATE_KEY)

        initState?.let {
            val state = it.getString(State.STATE_KEY) ?: ""
            if (state.isNotEmpty()) uiState = Json.decodeFromString(state)
        }

        savedState.setSavedStateProvider(State.UI_STATE_KEY) {
            println("s149 setSavedStateProvider CALL")
            bundleOf(State.STATE_KEY to Json.encodeToString(uiState))
        }
    }

    fun handleEvent(event: Event) {
        when (event) {
            is Event.OnViewCreated -> handleEvent(event)
            is Event.OnClick -> handleEvent(event)
        }
    }

    private fun handleEvent(event: Event.OnClick) {
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

    private fun handleEvent(event: Event.OnViewCreated) {
        observerState?.onNext(uiState)
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

    @Serializable
    data class State(
        val items: List<ItemUi>,
        val selectedItem: Int,
        val textTitle: String,
    ) {
        companion object {
            const val UI_STATE_KEY = "UI_STATE_KEY"
            const val STATE_KEY = "STATE_KEY"

            fun initial(): State =
                State(
                    items = generateItems(),
                    selectedItem = -1,
                    textTitle = ""
                )
        }
    }

    sealed interface Event {
        class OnViewCreated : Event

        class OnClick(
            val position: Int
        ) : Event
    }

    sealed interface Msg {
        class NewItems(
            val items: List<ItemUi>,
            val position: Int,
            val textTitle: String,
        ) : Msg
    }
}