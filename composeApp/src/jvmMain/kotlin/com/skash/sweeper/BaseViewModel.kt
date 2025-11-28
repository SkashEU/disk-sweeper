package com.skash.sweeper

import com.skash.forge.event.EventBus
import com.skash.forge.navigation.NavigationDispatcher
import com.skash.forge.viewmodel.NavResultAwareStateViewModel
import com.skash.forge.viewmodel.StateViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

abstract class BaseViewModel<State : Any, Intent> : StateViewModel<State, Intent, UIEvent> {
    constructor(initialState: State, eventBus: EventBus<UIEvent>?) : super(
        initialState = initialState,
        eventBus = eventBus,
        navigationDispatcher = resolveNavigationDispatcher(),
    )

    constructor(initialState: State, useEventBus: Boolean = true) : this(
        initialState = initialState,
        eventBus = if (useEventBus) resolveEventBus() else null,
    )

    private companion object Companion : KoinComponent {
        fun resolveEventBus(): EventBus<UIEvent> = get()

        fun resolveNavigationDispatcher(): NavigationDispatcher = get()
    }
}

abstract class BaseViewModelWithNavResult<State : Any, Intent, NavResult : Any> :
    NavResultAwareStateViewModel<State, Intent, UIEvent, NavResult> {
    constructor(initialState: State, eventBus: EventBus<UIEvent>?) : super(
        initialState = initialState,
        eventBus = eventBus,
        navigationDispatcher = resolveNavigationDispatcher(),
    )

    constructor(initialState: State, useEventBus: Boolean = true) : this(
        initialState = initialState,
        eventBus = if (useEventBus) resolveEventBus() else null,
    )

    private companion object Companion : KoinComponent {
        fun resolveEventBus(): EventBus<UIEvent> = get()

        fun resolveNavigationDispatcher(): NavigationDispatcher = get()
    }
}