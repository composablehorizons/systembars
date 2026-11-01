package com.composables.systemui

import androidx.compose.runtime.*

/** The color of the icons drawn in a system bar. */
public enum class SystemBarIcons {
    /** No preference. The most recent request that specifies this bar wins, otherwise the platform setting stays. */
    Unspecified,

    /** Light-colored icons, for dark content behind the bar. */
    Light,

    /** Dark-colored icons, for light content behind the bar. */
    Dark,
}

/** The icon appearance requested for each system bar. */
@Immutable
public class SystemBarAppearance(
    public val statusBar: SystemBarIcons = SystemBarIcons.Unspecified,
    public val navigationBar: SystemBarIcons = SystemBarIcons.Unspecified,
) {
    override fun equals(other: Any?): Boolean =
        other is SystemBarAppearance && statusBar == other.statusBar && navigationBar == other.navigationBar

    override fun hashCode(): Int = 31 * statusBar.hashCode() + navigationBar.hashCode()

    override fun toString(): String = "SystemBarAppearance(statusBar=$statusBar, navigationBar=$navigationBar)"
}

/**
 * Collects the [SystemBars] requests made inside [ProvideSystemBars].
 *
 * Read [appearance] to draw or apply the requested system bars yourself, for example in a preview tool.
 */
@Stable
public class SystemBarsState {
    private val requests = mutableStateListOf<Pair<Any, SystemBarAppearance>>()
    internal var onAppearanceChange: (() -> Unit)? = null

    /** The requested appearance. Each bar comes from the most recent request that specifies it. */
    public val appearance: SystemBarAppearance
        get() = SystemBarAppearance(latest { it.statusBar }, latest { it.navigationBar })

    private inline fun latest(bar: (SystemBarAppearance) -> SystemBarIcons): SystemBarIcons =
        requests.asReversed().firstNotNullOfOrNull { (_, appearance) ->
            bar(appearance).takeIf { it != SystemBarIcons.Unspecified }
        } ?: SystemBarIcons.Unspecified

    internal fun update(key: Any, appearance: SystemBarAppearance) = notifyingChanges {
        val index = requests.indexOfFirst { it.first === key }
        if (index < 0) requests.add(key to appearance)
        else if (requests[index].second != appearance) requests[index] = key to appearance
    }

    internal fun remove(key: Any) = notifyingChanges { requests.removeAll { it.first === key } }

    private inline fun notifyingChanges(change: () -> Unit) {
        val previous = appearance
        change()
        if (appearance != previous) onAppearanceChange?.invoke()
    }
}

private val LocalSystemBarsState = compositionLocalOf<SystemBarsState?> { null }

/** Sends the [SystemBars] requests made inside [content] to [state] instead of the platform. */
@Composable
public fun ProvideSystemBars(state: SystemBarsState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSystemBarsState provides state, content = content)
}

/** Requests the same icon appearance for the status and navigation bars while in composition. */
@Composable
public fun SystemBars(all: SystemBarIcons) {
    SystemBars(statusBar = all, navigationBar = all)
}

/**
 * Requests system bar icon appearance while in composition.
 *
 * The most recent request wins for each bar it specifies. When it leaves composition, the previous request applies again.
 */
@Composable
public fun SystemBars(
    statusBar: SystemBarIcons = SystemBarIcons.Unspecified,
    navigationBar: SystemBarIcons = SystemBarIcons.Unspecified,
) {
    val appearance = SystemBarAppearance(statusBar, navigationBar)
    val state = LocalSystemBarsState.current
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
