package kr.ilf.kshoong.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kr.ilf.kshoong.Destination
import kr.ilf.kshoong.HealthConnectManager
import kr.ilf.kshoong.R
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import kr.ilf.kshoong.viewmodel.UiState
import java.time.Instant

@Composable
fun NavigationView(
    modifier: Modifier,
    navController: NavHostController,
    healthConnectManager: HealthConnectManager,
    viewModel: SwimmingViewModel
) {
    val context = LocalContext.current

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Destination.Loading.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }) {
        composable(Destination.Loading.route) {
            LoadingView(
                context = context,
                healthConnectManager = healthConnectManager,
                viewModel = viewModel,
                onLoadingComplete = {
                    navController.navigate(Destination.Sync.route) {
                        popUpTo(Destination.Loading.route) {
                            inclusive = true
                        }
                    }
                })
        }
        composable(Destination.Sync.route, enterTransition = {
            fadeIn(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            )
        }, exitTransition = {
            fadeOut()
        }) {
            SyncView(
                context = context,
                viewModel = viewModel,
                onSyncComplete = {
                    navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.Sync.route) {
                            inclusive = true
                        }

                        anim { }
                    }

                    viewModel.uiState.value = UiState.SCROLLING
                }
            )
        }

        composable(
            Destination.Home.route,
            enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) },
            exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) },
        ) {
            val webView = remember {
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxSize(),
                factory = { webView },
                update = {
                    it.loadUrl("https://ilf.kr:8899/test/clothTest")
                })
        }

        composable(
            Destination.Calendar.route,
            enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) },
            exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) },
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .background(Color.Transparent)
            ) {
                CalendarView(viewModel = viewModel)
                CalendarDetailView(viewModel = viewModel, Instant.now())
            }
        }

//        navigation(
//            startDestination = Destination.Calendar.route,
//            route = Destination.Home.route,
//            enterTransition = { EnterTransition.None },
//            exitTransition = { ExitTransition.None },
        // 달력 커스텀 애니메이션
//            enterTransition = {
//                fadeIn(
//                    animationSpec = tween(
//                        300, easing = LinearEasing
//                    )
//                ) + slideIntoContainer(
//                    animationSpec = tween(700, easing = CubicBezierEasing(0f, 1.20f, 0.5f, 1f)),
//                    towards = AnimatedContentTransitionScope.SlideDirection.Up
//                )
//            }
//        ) {
//
//        }
    }
}

@Composable
fun LoadingView(
    context: Context,
    healthConnectManager: HealthConnectManager,
    viewModel: SwimmingViewModel,
    onLoadingComplete: () -> Unit
) {
    fun setChangeToken() {
        val sharedPreferences = context.getSharedPreferences("changeToken", MODE_PRIVATE)
        viewModel.setChangeToken(sharedPreferences.getString("changeToken", null))
        onLoadingComplete()
    }

    val availability by healthConnectManager.availability
    if (availability && viewModel.hasAllPermissions.value.not()) {
        val permissions = viewModel.healthPermissions
        val permissionsLauncher =
            rememberLauncherForActivityResult(contract = viewModel.permissionsContract) {
                // Handle permission result/
                if (viewModel.checkPermissions()) {
                    setChangeToken()
                } else {
                    // 종료
                }
            }

        LaunchedEffect(Unit) {
            delay(500)
            permissionsLauncher.launch(permissions)
        }
    } else if (availability && viewModel.hasAllPermissions.value) {
        LaunchedEffect(Unit) {
            delay(500)
            setChangeToken()
        }
    } else {
        // 종료
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White, shape = ShapeDefaults.ExtraLarge),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher),
            contentDescription = "logo",
            modifier = Modifier.size(288.dp)
        )

        Text(
            text = "KSHOONG!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 270.dp)
        )
    }
}

@Composable
fun SyncView(
    context: Context,
    viewModel: SwimmingViewModel,
    onSyncComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(500)
        viewModel.initSwimmingData(onSyncComplete)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White, shape = ShapeDefaults.ExtraLarge),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher),
            contentDescription = "logo",
            modifier = Modifier.size(288.dp)
        )

        Text(
            text = "Synchronizing!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 270.dp)
        )
    }
}
