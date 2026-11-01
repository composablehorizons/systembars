package com.composables.systembars

import androidx.compose.runtime.*
import kotlin.jvm.JvmInline

/** The color of the content drawn on a system bar, such as its icons, text and handle. */
@JvmInline
public value class SystemBarContent internal constructor(internal val value: Int) {
    override fun toString(): String = when (this) {
        Light -> "Light"
        Dark -> "Dark"
        else -> "Unspecified"
    }

    public companion object {
        /** No preference. The most recent request that specifies this bar wins, otherwise the platform setting stays. */
        public val Unspecified: SystemBarContent = SystemBarContent(0)

        /** Light content, for dark backgrounds behind the bar. */
        public val Light: SystemBarContent = SystemBarContent(1)

        /** Dark content, for light backgrounds behind the bar. */
        public val Dark: SystemBarContent = SystemBarContent(2)
    }
}

/** The content requested for each system bar. */
@Immutable
public class SystemBarAppearance internal constructor(
    public val statusBar: SystemBarContent = SystemBarContent.Unspecified,
    public val navigationBar: SystemBarContent = SystemBarContent.Unspecified,
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

    private inline fun latest(bar: (SystemBarAppearance) -> SystemBarContent): SystemBarContent =
        requests.asReversed().firstNotNullOfOrNull { (_, appearance) ->
            bar(appearance).takeIf { it != SystemBarContent.Unspecified }
        } ?: SystemBarContent.Unspecified

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

/** Requests the same content for the status and navigation bars while in composition. */
@Composable
public fun SystemBars(all: SystemBarContent) {
    SystemBars(statusBar = all, navigationBar = all)
}

/**
 * Requests system bar content while in composition.
 *
 * The most recent request wins for each bar it specifies. When it leaves composition, the previous request applies again.
 */
@Composable
public fun SystemBars(
    statusBar: SystemBarContent = SystemBarContent.Unspecified,
    navigationBar: SystemBarContent = SystemBarContent.Unspecified,
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
