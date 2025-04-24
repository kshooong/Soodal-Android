package kr.ilf.soodal.ui

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
import kr.ilf.soodal.viewmodel.CalendarUiState
import kr.ilf.soodal.viewmodel.SwimmingViewModel
import kr.ilf.soodal.viewmodel.UiState

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
                val weekHeight = 60.dp
                var calendarHeight by remember { mutableFloatStateOf(0f) }
                val density = LocalDensity.current

                CalendarView(
                    modifier = Modifier
                        .wrapContentSize()
                        .onGloballyPositioned { coordinates ->
                            calendarHeight =
                                with(density) { coordinates.size.height.toDp().value }
                        },
                    weekHeight = weekHeight,
                    contentsBg = Color.Transparent,
                    viewModel = viewModel
                )
                val configuration = LocalConfiguration.current
                val initialHeight by remember {
                    derivedStateOf {
                        configuration.screenHeightDp - calendarHeight - 60
                    }
                }

                Log.d("initialHeight", "initialHeight: $initialHeight")

                val detailRecord by viewModel.currentDetailRecords.collectAsState()

                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = detailRecord.isNotEmpty(), enter = UpEnterTransition,
                    exit = DownExitTransition
                ) {
                    val mySaver = Saver<Dp, Bundle>(
                        save = { Bundle().apply { putFloat("detailHeight", it.value) } },
                        restore = { it.getFloat("detailHeight").dp }
                    )
                    val detailHeight = rememberSaveable(stateSaver = mySaver) {
                        mutableStateOf(initialHeight.dp)
                    }

                    val animatableHeight = remember {
                        Animatable(
                            initialValue = detailHeight.value,
                            Dp.VectorConverter,
                            Dp.VisibilityThreshold
                        )
                    }

                    val animationDurationMills = 400

                    LaunchedEffect(viewModel.calendarUiState.value) {
                        // 아래 주석은 OFFSET방식 애니메이션 기준
                        if (viewModel.calendarUiState.value == CalendarUiState.TO_WEEK) {
                            // 달력 애니메이션 종료되고 완전히 WEEK_MODE가 되면 initialHeight 가 큰 값으로 변함 -> DetailView 애니메이션 실행 시 계산해서 넣어줘야함
                            animatableHeight.animateTo(initialHeight.dp + ((weekHeight.value + 5) * 5).dp, tween(animationDurationMills))
                        } else if (viewModel.calendarUiState.value == CalendarUiState.TO_MONTH) {
                            // TO_MONTH 가 되면 바로 initialHeight 가 작은 값으로 변함 -> DetailView 애니메이션 실행 시 계산하지 않고 사용가능
                            animatableHeight.animateTo(initialHeight.dp, tween(animationDurationMills))
                        }
                    }

                    CalendarDetailView(
                        Modifier
                            .padding(0.dp, 0.dp, 0.dp, 60.dp)
                            .fillMaxWidth()
                            .height(animatableHeight.value)
                            .navigationBarsPadding()
                            .background(
                                Color.White,
                                shape = ShapeDefaults.ExtraLarge.copy(
                                    bottomStart = CornerSize(0.0.dp),
                                    bottomEnd = CornerSize(0.0.dp)
                                )
                            )
                            .padding(horizontal = 5.dp),
                        viewModel = viewModel,
//                    viewModel = PreviewViewmodel(), // preview
                        resizeBar = { resizeBarModifier ->
                            ResizeBar(
                                resizeBarModifier,
                                animatableHeight,
                                initialHeight.dp,
                                (calendarHeight + initialHeight - 5).dp
                            )
                        }
                    )
                }
            }
        }
    }
}

// 글로벌 EnterTransition 변수
private val UpEnterTransition: EnterTransition = fadeIn(
    animationSpec = tween(
        300, easing = LinearEasing
    )
) + slideInVertically(
    animationSpec = tween(
        300,
        easing = FastOutSlowInEasing
    ), initialOffsetY = { it }
)

// 글로벌 ExitTransition 변수
private val DownExitTransition: ExitTransition = fadeOut(
    animationSpec = tween(
        300, easing = LinearEasing
    )
) + slideOutVertically(
    animationSpec = tween(
        300,
        easing = FastOutSlowInEasing
    ), targetOffsetY = { it }
)

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
                    (context as Activity).finishAndRemoveTask()
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
        (context as Activity).finishAndRemoveTask()
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
            text = "수 달",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 280.dp)
        )
        Text(
            text = "영     력",
            color = Color.Gray,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 220.dp)
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

//        Text(
//            text = "Synchronizing!",
//            style = MaterialTheme.typography.splashTitle,
//            modifier = Modifier.padding(bottom = 270.dp)
//        )
        Text(
            text = "수 달",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 280.dp)
        )
        Text(
            text = "영     력",
            color = Color.Gray,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 220.dp)
        )

        Text(
            text = "동기화 중",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 280.dp)
        )
    }
}
