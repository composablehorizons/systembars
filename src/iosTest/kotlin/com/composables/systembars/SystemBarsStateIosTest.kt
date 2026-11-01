package com.composables.systembars

import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleDefault
import platform.UIKit.UIViewController
import kotlin.test.Test
import kotlin.test.assertEquals

class SystemBarsStateIosTest {
    private class CountingController : UIViewController(null, null) {
        var updates = 0
        override fun setNeedsStatusBarAppearanceUpdate() { updates++ }
    }

    @Test fun updatesTheControllerWhenTheStatusBarChanges() {
        val controller = CountingController()
        val state = SystemBarsState(controller)
        val screen = Any()
        assertEquals(UIStatusBarStyleDefault, state.statusBarStyle)
        state.update(screen, SystemBarAppearance(SystemBarContent.Dark))
        assertEquals(UIStatusBarStyleDarkContent, state.statusBarStyle)
        assertEquals(1, controller.updates)
        state.remove(screen)
        assertEquals(UIStatusBarStyleDefault, state.statusBarStyle)
        assertEquals(2, controller.updates)
    }
}
