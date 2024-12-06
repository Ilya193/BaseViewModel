package ru.ikom.baseviewmodel

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainStore : BaseStoreWithNull<MainStore.State>(initial = State.initial()) {

    override fun initSavedState(savedStateHandle: SavedStateHandle) {
        savedStateHandle.get<Bundle>(State.MAIN_PROVIDER_STORE_STATE)?.let {
            val state = it.getString(State.MAIN_STORE_STATE) ?: return@let
            init(Json.decodeFromString(state))
        }

        savedStateHandle.setSavedStateProvider(State.MAIN_PROVIDER_STORE_STATE) {
            val bundle = Bundle()
            val state = state
            if (state != null) {
                bundle.putString(State.MAIN_STORE_STATE, Json.encodeToString(state))
            }
            bundle
        }
    }

    @Serializable
    data class State(
        val information: String,
    ) {
        companion object {
            const val MAIN_PROVIDER_STORE_STATE = "MAIN_PROVIDER_STORE_STATE"
            const val MAIN_STORE_STATE = "MAIN_STORE_STATE"

            fun initial(): State = State(information = "")
        }
    }
}