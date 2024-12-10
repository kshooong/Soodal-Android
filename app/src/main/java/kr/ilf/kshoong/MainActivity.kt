package kr.ilf.kshoong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kr.ilf.kshoong.ui.BottomBarView
import kr.ilf.kshoong.ui.NavigationView
import kr.ilf.kshoong.ui.theme.KshoongTheme
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import kr.ilf.kshoong.viewmodel.SwimmingViewModelFactory

class MainActivity : ComponentActivity() {

    private val healthConnectManager by lazy { HealthConnectManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.remove()
        }

        enableEdgeToEdge()
        setContent {
            KshoongTheme {
                val viewModel: SwimmingViewModel =
                    viewModel(factory = SwimmingViewModelFactory(application, healthConnectManager))
                val navController = rememberNavController()

                Box(modifier = Modifier.fillMaxSize()) {
                    NavigationView(
                        Modifier
                            .fillMaxSize(),
                        navController,
                        healthConnectManager,
                        viewModel
                    )

                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        visible = viewModel.isLoaded.value,
                        enter =  fadeIn(
                            animationSpec = tween(
                                300, easing = LinearEasing
                            )
                        ) + slideInVertically(animationSpec = tween(500, easing = FastOutSlowInEasing) ,initialOffsetY = { it }),
                        exit = fadeOut()
                    ) {
                        BottomBarView(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .height(100.dp)
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Transparent),
                            {
                                if (navController.currentDestination?.route != Destination.Calendar.route)
                                    navController.navigate(Destination.Calendar.route) {
                                        launchSingleTop = true
                                        popUpTo(navController.currentDestination?.route!!) {
                                            inclusive = true
                                        }
                                    }
                            },
                            {
                                if (navController.currentDestination?.route != Destination.Detail.route)
                                    navController.navigate(Destination.Detail.route) {
                                        launchSingleTop = true
                                        popUpTo(navController.currentDestination?.route!!) {
                                            inclusive = true
                                        }
                                    }
                            },
                        )
                    }
                }
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