package ru.ikom.baseviewmodel

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainStore : BaseStoreWithNull<MainStore.State>(initial = State.initial()) {

    override fun initSavedState(savedStateHandle: SavedStateHandle) {
        savedStateHandle.get<State>(State.MAIN_STORE_STATE)?.let(::init)

        savedStateHandle.setSavedStateProvider(State.MAIN_PROVIDER_STORE_STATE) {
            val bundle = Bundle()
            val state = state
            if (state != null) {
                savedStateHandle[State.MAIN_STORE_STATE] = state
            }
            bundle
        }
    }

    @Parcelize
    data class State(
        val information: String,
    ) : Parcelable {
        companion object {
            const val MAIN_PROVIDER_STORE_STATE = "MAIN_PROVIDER_STORE_STATE"
            const val MAIN_STORE_STATE = "MAIN_STORE_STATE"

            fun initial(): State = State(information = "")
        }
    }
}