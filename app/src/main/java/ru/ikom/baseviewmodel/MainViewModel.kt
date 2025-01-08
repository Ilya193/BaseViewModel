package ru.ikom.baseviewmodel

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainViewModel(
    private val store: MainStore,
    private val savedState: SavedStateHandle,
) : BaseViewModel<MainViewModel.State, MainViewModel.Msg,
        MainViewModel.Event, Any>(initialState = State.initial()) {

    init {
        savedState.get<State>(State.STATE_KEY)?.let {
            uiState = it
        }

        savedState.setSavedStateProvider(State.UI_STATE_KEY) {
            savedState[State.STATE_KEY] = uiState
            Bundle()
        }

        store.initSavedState(savedState)

        viewModelScope.launch {
            store.states.filterNotNull().collect {
                dispatch(Msg.UpdateTitleInformation(information = it.information))
            }
        }
    }

    override fun handleEvent(event: Event) {
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

        val newTitle = newItems[event.position].text

        dispatch(
            Msg.NewItems(
                items = newItems,
                position = event.position,
                textTitle = newTitle
            )
        )

        store.updateIfNotNull {
            val newInformation = it.information + " " + newTitle
            it.copy(information = newInformation)
        }
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

            is Msg.UpdateTitleInformation -> copy(information = msg.information)
        }

    @Parcelize
    data class State(
        val items: List<ItemUi>,
        val selectedItem: Int,
        val textTitle: String,
        val information: String,
    ) : Parcelable {
        companion object {
            const val UI_STATE_KEY = "UI_STATE_KEY"
            const val STATE_KEY = "STATE_KEY"

            fun initial(): State =
                State(
                    items = generateItems(),
                    selectedItem = -1,
                    textTitle = "",
                    information = "",
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

        class UpdateTitleInformation(val information: String) : Msg
    }
}