package kr.ilf.soodal.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kr.ilf.soodal.Destination
import kr.ilf.soodal.HealthConnectManager
import kr.ilf.soodal.R
import kr.ilf.soodal.ui.theme.ColorCalendarBgEnd
import kr.ilf.soodal.ui.theme.ColorCalendarBgStart
import kr.ilf.soodal.ui.theme.splashTitle
import kr.ilf.soodal.viewmodel.SwimmingViewModel
import kr.ilf.soodal.viewmodel.UiState
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
                    navController.navigate(Destination.Calendar.route) {
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
                    .background(
                        Brush.linearGradient(
                            Pair(0f, ColorCalendarBgStart),
                            Pair(0.75f, ColorCalendarBgEnd),
                            start = Offset(0f, 0f),
                            end = Offset(0.5f, Float.POSITIVE_INFINITY)
                        )
                    )
                    .statusBarsPadding()
            ) {
                CalendarView(
                    modifier = Modifier
                        .wrapContentSize(), contentsBg = Color.Transparent, viewModel = viewModel
                )

                val initialHeight = LocalConfiguration.current.screenHeightDp - 592.5f

                val detailRecord by viewModel.currentDetailRecords.collectAsState()
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = detailRecord.isNotEmpty(), enter = fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideInVertically(
                        animationSpec = tween(
                            300,
                            easing = FastOutSlowInEasing
                        ), initialOffsetY = { it }),
                    exit = fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutVertically(animationSpec = tween(
                        300,
                        easing = FastOutSlowInEasing
                    ), targetOffsetY = { it })
                ) {
                    CalendarDetailView(
                        modifier = Modifier,
                        viewModel = viewModel,
//                    viewModel = PreviewViewmodel(), // preview
                        Instant.now(),
                        initialHeight
                    )
                }

            }
        }
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
            text = "SOODAL!",
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
            style = MaterialTheme.typography.splashTitle,
            modifier = Modifier.padding(bottom = 270.dp)
        )
    }
}
