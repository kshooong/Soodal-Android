package kr.ilf.kshoong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kr.ilf.kshoong.ui.NavigationView
import kr.ilf.kshoong.ui.theme.KshoongTheme
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import kr.ilf.kshoong.viewmodel.SwimmingViewModelFactory

class MainActivity : ComponentActivity() {

    private val healthConnectManager by lazy { HealthConnectManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        enableEdgeToEdge()
        setContent {
            KshoongTheme {
                val viewModel: SwimmingViewModel =
                    viewModel(factory = SwimmingViewModelFactory(application, healthConnectManager))
                val navController = rememberNavController()
                NavigationView(navController, healthConnectManager, viewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
//    val isLoading = true
//    KshoongTheme {
//        if (isLoading) {
//            LoadingVIew()
//        } else {
//    SwimCalendarView4(MainActivity.data)

//        }
//    }
}