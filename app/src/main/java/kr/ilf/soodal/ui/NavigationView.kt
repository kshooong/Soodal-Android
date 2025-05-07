package kr.ilf.soodal.ui

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kr.ilf.soodal.Destination
import kr.ilf.soodal.HealthConnectManager
import kr.ilf.soodal.R
import kr.ilf.soodal.ui.theme.ColorCalBgEnd
import kr.ilf.soodal.ui.theme.ColorCalBgStart
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
                            Pair(0f, ColorCalBgStart),
                            Pair(0.75f, ColorCalBgEnd),
                            start = Offset(0f, 0f),
                            end = Offset(0.5f, Float.POSITIVE_INFINITY)
                        )
                    )
                    .statusBarsPadding()
            ) {
                val weekHeight = 60.dp
                var calendarHeight by remember { mutableFloatStateOf(0f) }
                val configuration = LocalConfiguration.current
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

                val detailRecord by viewModel.currentDetailRecords.collectAsState()

                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = detailRecord.isNotEmpty(), enter = UpEnterTransition,
                    exit = DownExitTransition
                ) {
//                    val mySaver = Saver<Dp, Bundle>(
//                        save = { Bundle().apply { putFloat("detailHeight", it.value) } },
//                        restore = { it.getFloat("detailHeight").dp }
//                    )
//
                    val initHeight = configuration.screenHeightDp.dp - 60.dp
                    val detailHeight = remember { mutableStateOf(initHeight) }
                    val animatableOffset = remember {
                        Animatable(
                            initialValue = calendarHeight.dp,
                            Dp.VectorConverter,
                            Dp.VisibilityThreshold
                        )
                    }

                    var calendarMode by viewModel.calendarUiState
                    val animationDurationMills = 500

                    LaunchedEffect(calendarMode) {
                        // 아래 주석은 달력 OFFSET방식 애니메이션 기준
                        if (calendarMode == CalendarUiState.TO_WEEK) {
                            animatableOffset.animateTo(
                                calendarHeight.dp - ((weekHeight.value + 5) * 5).dp,
                                tween(animationDurationMills)
                            )

                            // 상세보기에서 스크롤위해 높이 수정
                            detailHeight.value = initHeight - animatableOffset.value
                            animatableOffset.snapTo(
                                0.dp,
                            )

                            calendarMode = CalendarUiState.WEEK_MODE
                        } else if (calendarMode == CalendarUiState.TO_MONTH) {
                            // 상세보기에서 스크롤위해 높이 수정한 높이 되돌리고 애니메이션 시작
                            animatableOffset.snapTo(
                                calendarHeight.dp - ((weekHeight.value + 5) * 5).dp,
                            )
                            detailHeight.value = initHeight

                            animatableOffset.animateTo(
                                calendarHeight.dp,
                                tween(animationDurationMills)
                            )

                            calendarMode = CalendarUiState.MONTH_MODE
                        }
                    }

                    CalendarDetailView(
                        Modifier
                            .offset { IntOffset(0, animatableOffset.value.roundToPx()) }
                            .padding(0.dp, 0.dp, 0.dp, 60.dp)
                            .fillMaxWidth()
                            .height(detailHeight.value)
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
                                animatableOffset,
                                calendarHeight.dp,
                                5.dp
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
