package com.sweak.unlockmaster.presentation.common.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

fun NavController.popBackStackThrottled(lifecycleOwner: LifecycleOwner) {
    val currentState = lifecycleOwner.lifecycle.currentState

    if (currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        popBackStack()
    }
}

fun NavController.navigateThrottled(route: String, lifecycleOwner: LifecycleOwner) {
    val currentState = lifecycleOwner.lifecycle.currentState

    if (currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        navigate(route)
    }
}

fun NavController.navigateThrottled(
    route: String,
    lifecycleOwner: LifecycleOwner,
    builder: NavOptionsBuilder.() -> Unit
) {
    val currentState = lifecycleOwner.lifecycle.currentState

    if (currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        navigate(route, builder)
    }
}