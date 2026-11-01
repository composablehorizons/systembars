package com.composables.systemui

import androidx.compose.runtime.*

enum class SystemBarIcons { Unspecified, Light, Dark }

data class SystemBarAppearance(
    val statusBar: SystemBarIcons = SystemBarIcons.Unspecified,
    val navigationBar: SystemBarIcons = SystemBarIcons.Unspecified,
)

@Stable
class SystemUiState {
    private val requests = mutableStateListOf<Pair<Any, SystemBarAppearance>>()
    val appearance: SystemBarAppearance
        get() = SystemBarAppearance(latest { it.statusBar }, latest { it.navigationBar })

    private inline fun latest(bar: (SystemBarAppearance) -> SystemBarIcons): SystemBarIcons =
        requests.asReversed().firstNotNullOfOrNull { (_, appearance) ->
            bar(appearance).takeIf { it != SystemBarIcons.Unspecified }
        } ?: SystemBarIcons.Unspecified

    internal fun update(key: Any, appearance: SystemBarAppearance) {
        val index = requests.indexOfFirst { it.first === key }
        if (index < 0) requests.add(key to appearance)
        else if (requests[index].second != appearance) requests[index] = key to appearance
    }

    internal fun remove(key: Any) { requests.removeAll { it.first === key } }
}

private val LocalSystemUi = compositionLocalOf<SystemUiState?> { null }

@Composable
fun ProvideSystemUi(state: SystemUiState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSystemUi provides state, content = content)
}

@Composable
fun SystemBars(all: SystemBarIcons) {
    SystemBars(statusBar = all, navigationBar = all)
}

@Composable
fun SystemBars(
    statusBar: SystemBarIcons = SystemBarIcons.Unspecified,
    navigationBar: SystemBarIcons = SystemBarIcons.Unspecified,
) {
    val appearance = SystemBarAppearance(statusBar, navigationBar)
    val state = LocalSystemUi.current
    if (state == null) {
        NativeSystemBars(appearance)
    } else {
        val key = remember { Any() }
        DisposableEffect(state, key) {
            state.update(key, appearance)
            onDispose { state.remove(key) }
        }
        SideEffect { state.update(key, appearance) }
    }
}

@Composable
internal expect fun NativeSystemBars(appearance: SystemBarAppearance)
