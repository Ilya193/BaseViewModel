package ru.ikom.baseviewmodel

class MainViewModel :
    BaseViewModel<MainViewModel.State, MainViewModel.Msg, MainViewModel.Label>(initialState = State.initial()) {

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

            is Msg.UpdateLock -> copy(isLock = msg.isLock)
        }

    data class State(
        val items: List<ItemUi>,
        val selectedItem: Int,
        val textTitle: String,
        val isLock: Boolean,
    ) {
        companion object {
            fun initial(): State =
                State(
                    items = generateItems(),
                    selectedItem = -1,
                    textTitle = "",
                    isLock = false,
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

        class UpdateLock(val isLock: Boolean) : Msg
    }

    sealed interface Label {
        class Log(val i: Int) : Label
    }
}