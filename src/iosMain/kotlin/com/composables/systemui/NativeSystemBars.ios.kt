@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalNativeApi::class)

package com.composables.systemui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import kotlin.native.ref.WeakReference
import platform.Foundation.NSLog
import platform.UIKit.*

/**
 * Creates a [SystemBarsState] that asks [viewController] to update its status bar whenever the requested appearance changes.
 *
 * Return [statusBarStyle] from the controller's `preferredStatusBarStyle()`.
 */
public fun SystemBarsState(viewController: UIViewController): SystemBarsState {
    val controller = WeakReference(viewController)
    return SystemBarsState().apply {
        onAppearanceChange = { controller.get()?.setNeedsStatusBarAppearanceUpdate() }
    }
}

/** The UIKit status bar style for the requested [SystemBarsState.appearance]. */
public val SystemBarsState.statusBarStyle: UIStatusBarStyle
    get() = when (appearance.statusBar) {
        SystemBarIcons.Unspecified -> UIStatusBarStyleDefault
        SystemBarIcons.Light -> UIStatusBarStyleLightContent
        SystemBarIcons.Dark -> UIStatusBarStyleDarkContent
    }

/** Hosts [content] in a view controller that applies its [SystemBars] requests to the status bar. */
public fun SystemUiViewController(content: @Composable () -> Unit): UIViewController =
    SystemUiHostingController(content)

private class SystemUiHostingController(private val content: @Composable () -> Unit) : UIViewController(null, null) {
    private val systemBarsState = SystemBarsState(this)

    override fun preferredStatusBarStyle(): UIStatusBarStyle = systemBarsState.statusBarStyle

    override fun viewDidLoad() {
        super.viewDidLoad()
        val child = ComposeUIViewController { ProvideSystemBars(systemBarsState, content) }
        addChildViewController(child)
        view.addSubview(child.view)
        child.view.setFrame(view.bounds)
        child.view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        child.didMoveToParentViewController(this)
    }
}

private var warnedMissingHost = false

@Composable
internal actual fun NativeSystemBars(appearance: SystemBarAppearance) {
    if (!warnedMissingHost) {
        warnedMissingHost = true
        NSLog("SystemBars has no effect: host Compose with SystemUiViewController or ProvideSystemBars(SystemBarsState(viewController)).")
    }
}
