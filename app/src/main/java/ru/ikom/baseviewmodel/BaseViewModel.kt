package ru.ikom.baseviewmodel

import androidx.lifecycle.ViewModel

abstract class BaseViewModel<Model : Any> : ViewModel() {

    protected inline fun <T> diff(
        old: Model,
        new: Model,
        get: (Model) -> T,
        compare: (old: T, new: T) -> Boolean = { a, b -> a == b },
        set: (T) -> Unit
    ) : Boolean {
        val oldValue = get(old)
        val newValue = get(new)

        if (!compare(oldValue, newValue)) {
            set(newValue)
            return true
        }
        return false
    }

    protected abstract suspend fun diff(old: Model, new: Model)

}