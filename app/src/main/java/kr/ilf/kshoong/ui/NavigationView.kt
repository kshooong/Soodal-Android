package kr.ilf.kshoong.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.view.ViewGroup
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kr.ilf.kshoong.Destination
import kr.ilf.kshoong.HealthConnectManager
import kr.ilf.kshoong.R
import kr.ilf.kshoong.ui.theme.ColorBottomBarButton
import kr.ilf.kshoong.ui.theme.ColorBottomBarButtonActive
import kr.ilf.kshoong.viewmodel.PopupUiState
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import kr.ilf.kshoong.viewmodel.UiState
import java.time.Instant

@Composable
fun NavigationView(
    modifier: Modifier,
    navController: NavHostController,
    healthConnectManager: HealthConnectManager,
    viewModel: SwimmingViewModel,
    prevDestination: MutableState<String>
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
        }
    }
    val shopWebView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
        }
    }

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
            enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) + fadeIn() },
            exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) + fadeOut() },
            popEnterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) + fadeIn() },
            popExitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) + fadeOut() },

            ) {
            val url = "https://ilf.kr:8899/test/clothTest"

            Column(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier
                        .size(300.dp),
                    factory = {
                        webView.parent?.let {
                            (it as ViewGroup).removeView(webView)
                        }
                        webView
                    },

                    update = { webView ->
                        // URL이 변경되지 않은 경우에만 업데이트
                        if (webView.url != url) {
                            webView.loadUrl(url)
                        }
                    }
                )

                val items = listOf(
                    "기본",
                    "누더기",
                    "루돌프",
                    "밀집모자",
                    "빨간조끼",
                    "수경",
                    "수모",
                    "운동복",
                    "병아리모자",
                    "오리튜브",
                    "미니가방",
                    "파랑옷",
                    "개구리모자",
                    "개구리목도리",
                    "묘기공머리띠"
                )

                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                ) {
                    items.forEach {
                        Column {
                            Button(onClick = {
                                webView.evaluateJavascript(
                                    "getGif('$it')",
                                    null
                                )
                            }) {
                                Text(text = "get$it")
                            }

                            Button(onClick = {
                                webView.evaluateJavascript(
                                    "deleteGif('$it')",
                                    null
                                )
                            }) {
                                Text(text = "delete$it")
                            }
                        }
                    }
                }

                IconButton(
                    modifier = Modifier
                        .padding(bottom = 60.dp)
                        .navigationBarsPadding()
                        .size(50.dp)
                        .align(Alignment.End),
                    onClick = {
                        viewModel.popupUiState.value = PopupUiState.MODIFY
                    }) {

                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.btn_edit),
                        modifier = modifier.size(50.dp),
                        contentDescription = "기록 버튼",
                        tint = Color.Unspecified
                    )
                }
            }
        }

        composable(
            Destination.Calendar.route,
            enterTransition = {
                slideIntoContainer(
                    towards = if (Destination.Home.route == prevDestination.value) {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.End
                    }
                ) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    towards =
                    if (Destination.Home.route == navController.currentDestination?.route) {
                        AnimatedContentTransitionScope.SlideDirection.End
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    }
                ) + fadeOut()
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = if (Destination.Home.route == prevDestination.value) {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.End
                    }
                ) + fadeIn()
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards =
                    if (Destination.Home.route == navController.currentDestination?.route) {
                        AnimatedContentTransitionScope.SlideDirection.End
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    }
                ) + fadeOut()
            },
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .background(Color.Transparent)
            ) {
                CalendarView(modifier = Modifier.wrapContentSize(), viewModel = viewModel)

                val initialHeight = LocalConfiguration.current.screenHeightDp - 600

                CalendarDetailView(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    viewModel = viewModel,
                    Instant.now(),
                    initialHeight
                )
            }
        }

        composable(
            Destination.Shop.route,
            enterTransition = {
                slideIntoContainer(
                    towards = if (Destination.Setting.route == prevDestination.value) {
                        AnimatedContentTransitionScope.SlideDirection.End
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    }
                ) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    towards =
                    if (Destination.Setting.route == navController.currentDestination?.route) {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.End
                    }
                ) + fadeOut()
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = if (Destination.Setting.route == prevDestination.value) {
                        AnimatedContentTransitionScope.SlideDirection.End
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    }
                ) + fadeIn()
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards =
                    if (Destination.Setting.route == navController.currentDestination?.route) {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.End
                    }
                ) + fadeOut()
            },
        ) {
            val url = "https://ilf.kr:8899/test/clothTestWithButton"

            AndroidView(
                modifier = Modifier
                    .fillMaxSize(),
                factory = {
                    shopWebView.parent?.let {
                        (it as ViewGroup).removeView(shopWebView)
                    }
                    shopWebView
                },

                update = { shopWebView ->
                    // URL이 변경되지 않은 경우에만 업데이트
                    if (shopWebView.url != url) {
                        shopWebView.loadUrl(url)
                    }
                }
            )
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
