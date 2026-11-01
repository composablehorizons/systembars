package com.composables.systemui

import kotlin.test.Test
import kotlin.test.assertEquals

class SystemBarsStateTest {
    @Test fun restoresUnderlyingRequestWhenOverrideLeaves() {
        val state = SystemBarsState()
        val screen = Any()
        val overlay = Any()
        val light = SystemBarAppearance(SystemBarIcons.Light, SystemBarIcons.Light)
        val dark = SystemBarAppearance(SystemBarIcons.Dark)
        state.update(screen, light)
        state.update(overlay, dark)
        assertEquals(SystemBarAppearance(SystemBarIcons.Dark, SystemBarIcons.Light), state.appearance)
        state.update(screen, SystemBarAppearance())
        assertEquals(dark, state.appearance)
        state.remove(overlay)
        assertEquals(SystemBarAppearance(), state.appearance)
        state.update(screen, light)
        assertEquals(light, state.appearance)
        state.remove(screen)
        assertEquals(SystemBarAppearance(), state.appearance)
    }

    @Test fun notifiesOnlyWhenTheRequestedAppearanceChanges() {
        val state = SystemBarsState()
        var changes = 0
        state.onAppearanceChange = { changes++ }
        val screen = Any()
        val overlay = Any()
        state.update(screen, SystemBarAppearance(SystemBarIcons.Dark))
        state.update(screen, SystemBarAppearance(SystemBarIcons.Dark))
        assertEquals(1, changes)
        state.update(overlay, SystemBarAppearance(navigationBar = SystemBarIcons.Light))
        assertEquals(2, changes)
        state.update(overlay, SystemBarAppearance(SystemBarIcons.Unspecified, SystemBarIcons.Light))
        assertEquals(2, changes)
        state.remove(overlay)
        state.remove(screen)
        assertEquals(4, changes)
    }
}
