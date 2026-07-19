package com.sitereports.app

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sitereports.app.ui.SiteReportsApp
import com.sitereports.app.ui.SiteReportsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setOnExitAnimationListener { provider ->
            provider.iconView.animate()
                .alpha(0f)
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(220L)
                .withEndAction(provider::remove)
                .start()
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        enterImmersiveMode()
        val application = application as SiteReportsApplication
        setContent {
            SiteReportsTheme {
                SiteReportsApp(
                    unitRepository = application.unitRepository,
                    reportRepository = application.reportRepository,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
