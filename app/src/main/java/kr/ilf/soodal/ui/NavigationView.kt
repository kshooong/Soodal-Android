package kr.ilf.soodal.ui

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kr.ilf.soodal.Destination
import kr.ilf.soodal.R
import kr.ilf.soodal.SharedPrefConst
import kr.ilf.soodal.ui.theme.ColorCalBgEnd
import kr.ilf.soodal.ui.theme.ColorCalBgStart
import kr.ilf.soodal.ui.theme.ColorTextDefault
import kr.ilf.soodal.util.HealthConnectManager
import kr.ilf.soodal.viewmodel.CalendarUiState
import kr.ilf.soodal.viewmodel.CalendarViewModelImpl
import kr.ilf.soodal.viewmodel.UiState

@Composable
fun NavigationView(
    modifier: Modifier,
    navController: NavHostController,
    healthConnectManager: HealthConnectManager,
    viewModel: CalendarViewModelImpl
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
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) + fadeOut()
            },
            popEnterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) + fadeIn()
            },
            popExitTransition = {
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) + fadeOut()
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
                    .navigationBarsPadding()
                    .statusBarsPadding()
            ) {
                val headerHeight = 73.dp
                val weekHeight = 62.dp
                val spacing = 5.dp
                val weekModeOffset = remember { headerHeight + weekHeight + spacing + 5.dp }
                var calendarHeight by remember { mutableFloatStateOf(0f) }
                val density = LocalDensity.current
                val localWindowInfo = LocalWindowInfo.current
                val statusBarHeight =
                    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                val navBarHeight =
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                val containerHeight =
                    with(density) { localWindowInfo.containerSize.height.toDp() } - statusBarHeight - navBarHeight

                CalendarView(
                    modifier = Modifier
                        .wrapContentSize()
                        .onGloballyPositioned { coordinates ->
                            calendarHeight =
                                with(density) { coordinates.size.height.toDp().value }
                        },
                    headerHeight = headerHeight,
                    weekHeight = weekHeight,
                    spacing = spacing,
                    contentsBg = Color.Transparent,
                    viewModel = viewModel
                )

                // 배경 아이콘
                Box(
                    Modifier
                        .padding(bottom = 60.dp)
                        .height(containerHeight - 60.dp - calendarHeight.dp)
                        .padding(bottom = 5.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Image(
                        ImageBitmap.imageResource(R.drawable.ic_swimming_bg),
                        contentDescription = "",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .focusable(false),
                        alpha = 0.2f
                    )
                }

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
                    val initHeight = containerHeight - 60.dp
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
                        if (calendarMode == CalendarUiState.TO_WEEK) {
                            animatableOffset.animateTo(
                                weekModeOffset,
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
                                weekModeOffset,
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
            Destination.Settings.route,
            enterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) + fadeOut()
            },
            popEnterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) + fadeIn()
            },
            popExitTransition = {
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) + fadeOut()
            },
        ) {
            SettingsView(navController)
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
    viewModel: CalendarViewModelImpl,
    onLoadingComplete: () -> Unit
) {
    val setChangeToken = remember(context, viewModel) {
        {
            val sharedPreferences =
                context.getSharedPreferences(SharedPrefConst.AppSync.NAME, MODE_PRIVATE)
            viewModel.setChangeToken(
                sharedPreferences.getString(
                    SharedPrefConst.AppSync.KEY_CHANE_TOKEN,
                    null
                )
            )
        }
    }

    val availability by healthConnectManager.availability
    if (availability && viewModel.hasAllPermissions.value.not()) {
        val permissions = viewModel.healthPermissions
        val permissionsLauncher =
            rememberLauncherForActivityResult(contract = viewModel.permissionsContract) {
                // Handle permission result/
                if (viewModel.checkPermissions()) {
                    setChangeToken()
                    onLoadingComplete()
                } else {
                    (context as Activity).finish()
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
            onLoadingComplete()
        }
    } else {
        SoodalDialog(
            isVisible = true,
            title = stringResource(R.string.app_name),
            text = stringResource(R.string.dialog_message_health_connect_required),
            dismissText = stringResource(R.string.popup_label_exit),
            confirmText = stringResource(R.string.label_confirm),
            onDismissRequest = { (context as Activity).finishAndRemoveTask() },
            onConfirm = onConfirm@{
                val providerPackageName = HealthConnectManager.PROVIDER_PACKAGE_NAME
                val uriString =
                    "market://details?id=$providerPackageName"
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                    setPackage("com.android.vending")
                    putExtra("overlay", true)
                    putExtra("callerId", context.packageName)
                }

                if (context.packageManager.resolveActivity(
                        marketIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    ) == null
                ) {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$providerPackageName")
                        )
                    )

                    return@onConfirm
                }

                context.startActivity(marketIntent)
            })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White, shape = ShapeDefaults.ExtraLarge),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher),
            contentDescription = stringResource(R.string.description_logo),
            modifier = Modifier
                .size(288.dp)
                .focusable(false)
        )

        Text(
            text = "수 달",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 280.dp)
                .focusable(false),
            color = ColorTextDefault
        )
        Text(
            text = "영     력",
            color = Color.Gray,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(bottom = 220.dp)
                .focusable(false)
        )
    }
}

@Composable
fun SyncView(
    context: Context,
    viewModel: CalendarViewModelImpl,
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
            contentDescription = stringResource(R.string.description_logo),
            modifier = Modifier
                .size(288.dp)
                .focusable(false)
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
            modifier = Modifier
                .padding(bottom = 280.dp)
                .focusable(false),
            color = ColorTextDefault
        )
        Text(
            text = "영     력",
            color = Color.Gray,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(bottom = 220.dp)
                .focusable(false)
        )

        Text(
            text = stringResource(R.string.message_synchronizing),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 280.dp),
            color = ColorTextDefault
        )
    }
}
