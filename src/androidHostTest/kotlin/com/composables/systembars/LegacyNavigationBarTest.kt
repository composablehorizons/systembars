package com.composables.systembars

import android.graphics.Color
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [25])
class LegacyNavigationBarTest {
    @Suppress("DEPRECATION")
    @Test fun leavesTheNavigationBarAloneBeforeAndroid8() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        activity.window.navigationBarColor = Color.BLACK
        val view = ComposeView(activity)
        activity.setContentView(view)
        view.setContent { SystemBars(SystemBarContent.Dark) }
        shadowOf(Looper.getMainLooper()).idle()
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        assertTrue(controller.isAppearanceLightStatusBars)
        assertEquals(Color.BLACK, activity.window.navigationBarColor)
    }
}
