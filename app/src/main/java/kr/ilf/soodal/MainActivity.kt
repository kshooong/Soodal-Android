package kr.ilf.soodal

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import kr.ilf.soodal.ui.BottomBarView
import kr.ilf.soodal.ui.NavigationView
import kr.ilf.soodal.ui.PopupView
import kr.ilf.soodal.ui.theme.ColorBottomBar
import kr.ilf.soodal.ui.theme.ColorBottomBarDivider
import kr.ilf.soodal.ui.theme.SoodalTheme
import kr.ilf.soodal.util.HealthConnectManager
import kr.ilf.soodal.viewmodel.CalendarViewModelImpl
import kr.ilf.soodal.viewmodel.CalendarViewModelFactory
import kr.ilf.soodal.viewmodel.PopupUiState
import kr.ilf.soodal.viewmodel.UiState

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
            SoodalTheme {
                val viewModel: CalendarViewModelImpl =
                    viewModel(factory = CalendarViewModelFactory(application, healthConnectManager))
                val navController = rememberNavController()
                var popupUiState by viewModel.popupUiState

                onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        popupUiState = when (popupUiState) {
                            PopupUiState.NONE -> {
                                // 백 스택에 이전 화면이 있다면 pop
                                if (navController.previousBackStackEntry != null) {
                                    navController.popBackStack()
                                    return
                                }

                                // 없다면 앱종료 팝업
                                PopupUiState.APP_FINISH
                            }

                            PopupUiState.NEW_RECORD_MODIFY -> PopupUiState.NEW_RECORD
                            else -> PopupUiState.NONE
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
                    NavigationView(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        navController,
                        healthConnectManager,
                        viewModel
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
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(ColorBottomBar)
                                .topBorder(0.5.dp, ColorBottomBarDivider),
                            currentDestination,
                            onCalendarClick = {
                                if (navController.currentDestination?.route != Destination.Calendar.route) {
                                    if (navController.currentDestination?.route == Destination.Settings.route) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate(Destination.Calendar.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            onSettingClick = {
                                if (navController.currentDestination?.route != Destination.Settings.route) {
                                    navController.navigate(Destination.Settings.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }

                    PopupView(
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
//    SoodalTheme {
//        if (isLoading) {
//            LoadingVIew()
//        } else {
//    SwimCalendarView4(MainActivity.data)

//        }
//    }
}