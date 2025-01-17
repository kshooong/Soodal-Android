package kr.ilf.kshoong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kr.ilf.kshoong.ui.BottomBarView
import kr.ilf.kshoong.ui.NavigationView
import kr.ilf.kshoong.ui.PopupView
import kr.ilf.kshoong.ui.theme.ColorBottomBar
import kr.ilf.kshoong.ui.theme.ColorBottomBarDivider
import kr.ilf.kshoong.ui.theme.KshoongTheme
import kr.ilf.kshoong.viewmodel.PopupUiState
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import kr.ilf.kshoong.viewmodel.SwimmingViewModelFactory
import kr.ilf.kshoong.viewmodel.UiState

class MainActivity : ComponentActivity() {

    private val healthConnectManager by lazy { HealthConnectManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.remove()
        }

        enableEdgeToEdge()
        setContent {
            KshoongTheme {
                val viewModel: SwimmingViewModel =
                    viewModel(factory = SwimmingViewModelFactory(application, healthConnectManager))
                val navController = rememberNavController()

                onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (viewModel.popupUiState.value != PopupUiState.NONE) {
                            viewModel.popupUiState.value = PopupUiState.NONE
                        } else {
                            finish()
                        }
                    }
                })

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(3.dp)
                        )
                ) {
                    val prevDestination = remember { mutableStateOf(Destination.Home.route) }

                    NavigationView(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        navController,
                        healthConnectManager,
                        viewModel,
                        prevDestination
                    )

                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        visible = viewModel.uiState.value != UiState.LOADING,
                        enter = fadeIn(
                            animationSpec = tween(
                                300, easing = LinearEasing
                            )
                        ) + slideInVertically(
                            animationSpec = tween(
                                500,
                                easing = FastOutSlowInEasing
                            ), initialOffsetY = { it }),
                        exit = fadeOut()
                    ) {
                        val currentDestination =
                            remember { mutableStateOf(navController.currentDestination?.route) }

                        LaunchedEffect(navController) {
                            navController.addOnDestinationChangedListener { _, destination, _ ->
                                currentDestination.value = destination.route
                            }
                        }

                        BottomBarView(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .height(60.dp)
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(ColorBottomBar)
                                .topBorder(0.5.dp, ColorBottomBarDivider),
                            currentDestination,
                            onHomeClick = {
                                if (navController.currentDestination?.route != Destination.Home.route) {
                                    prevDestination.value =
                                        navController.currentDestination?.route ?: "Home"
                                    navController.navigate(Destination.Home.route) {
                                        launchSingleTop = true
                                        popUpTo(navController.currentDestination?.route!!) {
                                            inclusive = true
                                        }
                                    }
                                }
                            },
                            onCalendarClick = {
                                if (navController.currentDestination?.route != Destination.Calendar.route) {
                                    prevDestination.value =
                                        navController.currentDestination?.route ?: "Home"
                                    navController.navigate(Destination.Calendar.route) {
                                        launchSingleTop = true
                                        popUpTo(navController.currentDestination?.route!!) {
                                            inclusive = true
                                        }
                                    }
                                }
                            },
                            onShopClick = {
                                if (navController.currentDestination?.route != Destination.Shop.route) {
                                    prevDestination.value =
                                        navController.currentDestination?.route ?: "Home"
                                    navController.navigate(Destination.Shop.route) {
                                        launchSingleTop = true
                                        popUpTo(navController.currentDestination?.route!!) {
                                            inclusive = true
                                        }
                                    }
                                }
                            },
                            onSettingClick = {}
                        )
                    }

                    PopupView(
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                        viewModel,
                        navController
                    )
                }
            }
        }
    }
}

fun Modifier.topBorder(width: Dp, color: Color): Modifier = this.then(
    Modifier.drawBehind {
        val strokeWidthPx = width.toPx()
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, strokeWidthPx / 2),
            end = androidx.compose.ui.geometry.Offset(size.width, strokeWidthPx / 2),
            strokeWidth = strokeWidthPx
        )
    }
)


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