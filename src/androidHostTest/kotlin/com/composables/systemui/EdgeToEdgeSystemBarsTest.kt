package com.composables.systemui

import android.os.Looper
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EdgeToEdgeSystemBarsTest {
    @Test fun enforcedEdgeToEdgeAppliesAndRestoresIcons() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        activity.applicationInfo.targetSdkVersion = 35
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        val view = ComposeView(activity)
        activity.setContentView(view)
        view.setContent { SystemBars(SystemBarIcons.Dark) }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(controller.isAppearanceLightStatusBars)
        assertTrue(controller.isAppearanceLightNavigationBars)
        activity.setContentView(View(activity))
        shadowOf(Looper.getMainLooper()).idle()
        assertFalse(controller.isAppearanceLightStatusBars)
        assertFalse(controller.isAppearanceLightNavigationBars)
    }
}
