package kr.ilf.soodal.ui

import android.content.res.Resources
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.ilf.soodal.R
import kr.ilf.soodal.database.entity.DetailRecord
import kr.ilf.soodal.database.entity.DetailRecordWithHR
import kr.ilf.soodal.database.entity.HeartRateSample
import kr.ilf.soodal.ui.theme.ColorBackStroke
import kr.ilf.soodal.ui.theme.ColorBackStrokeSecondary
import kr.ilf.soodal.ui.theme.ColorBreastStroke
import kr.ilf.soodal.ui.theme.ColorBreastStrokeSecondary
import kr.ilf.soodal.ui.theme.ColorButterfly
import kr.ilf.soodal.ui.theme.ColorButterflySecondary
import kr.ilf.soodal.ui.theme.ColorCalendarDate
import kr.ilf.soodal.ui.theme.ColorCalendarDateBg
import kr.ilf.soodal.ui.theme.ColorCalendarDateBgDis
import kr.ilf.soodal.ui.theme.ColorCalendarDateDis
import kr.ilf.soodal.ui.theme.ColorCalendarItemBg
import kr.ilf.soodal.ui.theme.ColorCalendarItemBgDis
import kr.ilf.soodal.ui.theme.ColorCalendarOnItemBg
import kr.ilf.soodal.ui.theme.ColorCalendarOnItemBorder
import kr.ilf.soodal.ui.theme.ColorCalendarToday
import kr.ilf.soodal.ui.theme.ColorCalendarTodayBg
import kr.ilf.soodal.ui.theme.ColorCrawl
import kr.ilf.soodal.ui.theme.ColorCrawlSecondary
import kr.ilf.soodal.ui.theme.ColorKickBoard
import kr.ilf.soodal.ui.theme.ColorKickBoardSecondary
import kr.ilf.soodal.ui.theme.ColorMixEnd
import kr.ilf.soodal.ui.theme.ColorMixEndSecondary
import kr.ilf.soodal.ui.theme.ColorMixStart
import kr.ilf.soodal.ui.theme.ColorMixStartSecondary
import kr.ilf.soodal.ui.theme.SkyBlue6
import kr.ilf.soodal.ui.theme.notoSansKr
import kr.ilf.soodal.viewmodel.AnimationTypeUiState
import kr.ilf.soodal.viewmodel.CalendarUiState
import kr.ilf.soodal.viewmodel.PopupUiState
import kr.ilf.soodal.viewmodel.SwimmingViewModel
import kr.ilf.soodal.viewmodel.UiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration

val selectedMonthSaver =
    mapSaver(save = { mapOf("selectedMonth" to it) },
        restore = { it["selectedMonth"] as LocalDate })

@Composable
fun CalendarView(
    modifier: Modifier,
    weekHeight: Dp,
    contentsBg: Color,
    viewModel: SwimmingViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var calendarMode by viewModel.calendarUiState

    val today by remember { mutableStateOf(LocalDate.now()) }
    val todayWeek = remember { today.minusDays(today.dayOfWeek.value % 7 - 3L) } // 오늘이 있는 주의 수요일
    var currentMonth by viewModel.currentMonth
    var currentWeek by viewModel.currentWeek // 선택된 날이 있는 주의 수요일
    val selectedMonth = rememberSaveable(stateSaver = selectedMonthSaver) {
        mutableStateOf(
            LocalDate.now().withDayOfMonth(1)
        )
    }
    val animationCount by viewModel.animationCount
    val selectedDateStr =
        rememberSaveable() { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }
    val monthPagerState = rememberPagerState(0, pageCount = { 12 }) // 12달 간의 달력 제공
    var initialWeekPage by remember { mutableIntStateOf(0) }
    val weekPagerState = rememberPagerState(initialWeekPage, pageCount = { 52 }) // 12달 간의 달력 제공

    // 최초 진입 시 DetailRecord 조회, 새 데이터 확인 / dispose 시 데이터 초기화
    DisposableEffect(Unit) {
        calendarMode = CalendarUiState.MONTH_MODE
        val selectedInstant = selectedMonth.value.withDayOfMonth(selectedDateStr.value.toInt())
            .atStartOfDay(ZoneId.systemDefault()).toInstant()
        viewModel.findDetailRecord(selectedInstant)
        viewModel.checkAndShowNewRecordPopup()

        onDispose { viewModel.resetDetailRecord() }
    }

    LaunchedEffect(monthPagerState) {
        snapshotFlow { monthPagerState.isScrollInProgress }.distinctUntilChanged()
            .collect {
                if (monthPagerState.isScrollInProgress) {
                    viewModel.uiState.value = UiState.SCROLLING
                } else {
                    viewModel.uiState.value = UiState.COMPLETE

                }
            }
    }

    LaunchedEffect(weekPagerState.currentPage) {
        if ((calendarMode == CalendarUiState.WEEK_MODE)) {
            currentWeek = todayWeek.minusWeeks(weekPagerState.currentPage.toLong())

            val monthDifference = calculateMonthDifference(todayWeek, currentWeek)
            currentMonth = today.withDayOfMonth(1).minusMonths(monthDifference.toLong())
            coroutineScope.launch {
                monthPagerState.scrollToPage(monthDifference)
            }
        }
    }

    LaunchedEffect(monthPagerState.currentPage) {
        if ((calendarMode == CalendarUiState.MONTH_MODE)) {
            currentMonth = today.withDayOfMonth(1).minusMonths(monthPagerState.currentPage.toLong())

            // 선택된 날이 이번 달에 있으면 그 날짜가 속한 주를 currentWeek으로 설정
            if (selectedMonth.value.year == currentMonth.year && selectedMonth.value.month == currentMonth.month) {
                val selectedDate = selectedMonth.value.withDayOfMonth(selectedDateStr.value.toInt())
                val selectedWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value % 7 - 3L)
                currentWeek = selectedWeek
            } else {
                // 바뀐 월의 첫 번째 주를 currentWeek으로 설정
                val tempWeek = currentMonth.minusDays(currentMonth.dayOfWeek.value % 7 - 3L)
                currentWeek = if (tempWeek.dayOfMonth > 4) tempWeek.plusWeeks(1L) else tempWeek
            }

            val weekTarget = ChronoUnit.WEEKS.between(currentWeek, todayWeek).toInt()
            coroutineScope.launch {
                initialWeekPage = weekTarget
                weekPagerState.scrollToPage(weekTarget)
            }
        }

        viewModel.updateDailyRecords()
    }

    LaunchedEffect(animationCount) {
        // 표시될 주를 제외한 5주의 애니메이션이 종료되면 동작
        if (animationCount >= 5) {
            if (calendarMode == CalendarUiState.TO_WEEK) {
//                launch {
//                    weekPagerState.scrollToPage(
//                        ChronoUnit.WEEKS.between(currentWeek, todayWeek).toInt()
//                    )
//                }
                calendarMode = CalendarUiState.WEEK_MODE

            } else if (calendarMode == CalendarUiState.TO_MONTH) {
                calendarMode = CalendarUiState.MONTH_MODE
//                val monthDifference = calculateMonthDifference(todayWeek, currentWeek)
//                pagerState.scrollToPage(monthDifference)
            }
            viewModel.animationCount.intValue = 0
        }
    }

    Column(modifier = modifier) {
        CalendarHeaderView(viewModel, contentsBg)
//        Row {
//            Button(onClick = {
//                var animationType by viewModel.animationType
//                animationType = if (animationType == AnimationTypeUiState.SIZING) {
//                    AnimationTypeUiState.OFFSET
//                } else {
//                    AnimationTypeUiState.SIZING
//                }
//            }) { Text("Animation Type") }
//        }

        HorizontalPager(
            state = if (calendarMode == CalendarUiState.WEEK_MODE) weekPagerState else monthPagerState,
            userScrollEnabled = calendarMode == CalendarUiState.WEEK_MODE || calendarMode == CalendarUiState.MONTH_MODE,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(contentsBg, shape = RoundedCornerShape(10.dp))
                .padding(vertical = 5.dp),
            key = {
                if (calendarMode == CalendarUiState.WEEK_MODE) today.minusWeeks(it.toLong()) else today.minusMonths(
                    it.toLong()
                )
            },
            reverseLayout = true
        ) {
            val context = LocalContext.current

            if (calendarMode == CalendarUiState.WEEK_MODE) {
                val week = todayWeek.minusWeeks(it.toLong())
                val isCurrentWeek = week in currentWeek.minusWeeks(1)..currentWeek.plusWeeks(1)
                val daysInMonth = week.lengthOfMonth()
                val firstDayOfMonth = week.withDayOfMonth(1)
                val firstDayOfWeek =
                    (firstDayOfMonth.dayOfWeek.value % 7) // 0: Sunday, 6: Saturday
                val prevMonth = week.minusMonths(1)
                val daysInPrevMonth = prevMonth.lengthOfMonth()
                val weekOfMonth = getWeekOfMonth(week) - 1

                val dayCounter = (1 + weekOfMonth * 7 - firstDayOfWeek).coerceAtLeast(1)

                WeekView(
                    Modifier
                        .padding(horizontal = 5.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
//                        .padding(horizontal = 7.5.dp, vertical = 2.5.dp)
                        .background(Color.White, shape = RoundedCornerShape(14.dp)),
                    weekOfMonth,
                    firstDayOfWeek,
                    daysInPrevMonth,
                    viewModel,
                    week,
                    today,
                    dayCounter,
                    daysInMonth,
                    selectedDateStr,
                    selectedMonth,
                    isCurrentWeek,
                    weekHeight,
                    {}
                ) { clickedDate ->
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    when {
                        // 오늘보다 이후
                        clickedDate.isAfter(today) -> {
                            Toast.makeText(context, "오늘 이후는 선택할 수 없습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }

                        // 1년보다 이전
                        clickedDate.withDayOfMonth(1)
                            .isBefore(today.withDayOfMonth(1).minusMonths(11L)) -> {
                            Toast.makeText(context, "12달보다 이전은 선택할 수 없습니다", Toast.LENGTH_SHORT)
                                .show()
                        }

                        // 현재 달
                        else -> {
                            selectedMonth.value = clickedDate
                            selectedDateStr.value = clickedDate.dayOfMonth.toString()

                            viewModel.findDetailRecord(
                                clickedDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant()
                            )

                            val monthDifference = calculateMonthDifference(todayWeek, clickedDate)
                            currentMonth =
                                today.withDayOfMonth(1).minusMonths(monthDifference.toLong())
                            coroutineScope.launch {
                                monthPagerState.scrollToPage(monthDifference)
                            }
                        }
                    }
                }
            } else {
                val month = today.minusMonths(it.toLong())

                MonthView(
                    viewModel,
                    month,
                    selectedMonth,
                    selectedDateStr,
                    today,
                    weekHeight
                ) { clickedDate ->
                    when {
                        // 오늘보다 이후
                        clickedDate.isAfter(today) -> {
                            Toast.makeText(context, "오늘 이후는 선택할 수 없습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }

                        // 1년보다 이전
                        clickedDate.withDayOfMonth(1)
                            .isBefore(today.withDayOfMonth(1).minusMonths(11L)) -> {
                            Toast.makeText(context, "12달보다 이전은 선택할 수 없습니다", Toast.LENGTH_SHORT)
                                .show()
                        }

                        // 현재 달
                        clickedDate.month == currentMonth.month -> {
                            selectedMonth.value = clickedDate
                            selectedDateStr.value = clickedDate.dayOfMonth.toString()

                            viewModel.findDetailRecord(
                                clickedDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant()
                            )

                            currentWeek =
                                clickedDate.minusDays(clickedDate.dayOfWeek.value % 7 - 3L)

                            val weekTarget =
                                ChronoUnit.WEEKS.between(currentWeek, todayWeek).toInt()
                            coroutineScope.launch {
                                initialWeekPage = weekTarget
                                weekPagerState.scrollToPage(weekTarget)
                            }
                        }

                        // 전,다음달
                        else -> {
                            selectedMonth.value = clickedDate
                            selectedDateStr.value = clickedDate.dayOfMonth.toString()

                            viewModel.findDetailRecord(
                                clickedDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant()
                            )

                            val diffMonth =
                                ChronoUnit.MONTHS.between(
                                    clickedDate.withDayOfMonth(1),
                                    currentMonth
                                )
                                    .toInt()
                            CoroutineScope(Dispatchers.Main).launch {
                                withContext(coroutineScope.coroutineContext) {
                                    val monthTarget = monthPagerState.currentPage + diffMonth
                                    val weekTarget =
                                        ChronoUnit.WEEKS.between(currentWeek, todayWeek).toInt()

                                    monthPagerState.animateScrollToPage(monthTarget)
                                    initialWeekPage = weekTarget
                                    weekPagerState.scrollToPage(weekTarget)
                                }
                            }

                            currentWeek =
                                clickedDate.minusDays(clickedDate.dayOfWeek.value % 7 - 3L)
                        }
                    }
                }
            }
        }
    }
}

private fun getWeekOfMonth(date: LocalDate): Int {
    val firstDayOfMonth = date.withDayOfMonth(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek
    val dayOfMonth = date.dayOfMonth

    val offset = firstDayOfWeek.value % 7

    return (dayOfMonth + offset - 1) / 7 + 1
}

@Composable
private fun MonthView(
    viewModel: SwimmingViewModel,
    month: LocalDate,
    selectedMonth: MutableState<LocalDate>,
    selectedDateStr: MutableState<String>,
    today: LocalDate,
    weekHeight: Dp,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.withDayOfMonth(1)
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7) // 0: Sunday, 6: Saturda
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    Column(
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .wrapContentSize()
            .background(Color.Transparent),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // 날짜 표시
        var dayCounter = 1
        val currentWeek by viewModel.currentWeek

        // 주 단위로 날짜를 표시
        for (week in 0..5) { // 최대 6주까지 표시
            val isCurrentWeek =
                if (currentWeek.month == month.month && currentWeek.year == month.year) {
                    week == (getWeekOfMonth(currentWeek) - 1)
                } else {
                    val selectedDate =
                        selectedMonth.value.withDayOfMonth(selectedDateStr.value.toInt())

                    week == (getWeekOfMonth(selectedDate) - 1)
                }

            val calendarMode by viewModel.calendarUiState
            val weekViewModifier =
                when (viewModel.animationType.value) {
                    AnimationTypeUiState.OFFSET -> {
                        var offset by remember { mutableStateOf(if (calendarMode == CalendarUiState.MONTH_MODE || calendarMode == CalendarUiState.WEEK_MODE) 0.dp else (weekHeight + 5.dp) * week) }
                        var elevation by remember { mutableStateOf(0.dp) }
                        var dynamicDuration by remember { mutableIntStateOf(300) }
                        val animatedElevation by animateDpAsState(
                            elevation,
                            animationSpec = tween(dynamicDuration),
                            finishedListener = {
                                dynamicDuration = 500 - dynamicDuration
                                if (it != 0.dp)
                                    elevation = 0.dp

                            }
                        )
                        val animatedOffset by animateDpAsState(
                            offset,
                            animationSpec = tween(500),
                            finishedListener = { _ -> viewModel.animationCount.value += 1 })


                        var scaleRatio by remember { mutableFloatStateOf(0f) }
                        val animatedScaleRatio by animateFloatAsState(
                            scaleRatio,
                            animationSpec = tween(dynamicDuration),
                            finishedListener = {
                                dynamicDuration = 500 - dynamicDuration
                                if (it != 0f)
                                    scaleRatio = 0f

                            }
                        )

                        LaunchedEffect(calendarMode) {
                            offset = when (calendarMode) {
                                CalendarUiState.MONTH_MODE, CalendarUiState.TO_MONTH, CalendarUiState.WEEK_MODE -> 0.dp
                                CalendarUiState.TO_WEEK -> (weekHeight + 5.dp) * week
                            }

                            if (calendarMode == CalendarUiState.TO_WEEK || calendarMode == CalendarUiState.TO_MONTH) {
                                elevation = 5.dp
                                scaleRatio = 0.005f
                            }
                        }

                        Modifier
                            .zIndex(if (isCurrentWeek) 6f else (5 - week).toFloat())
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .offset { IntOffset(0, -animatedOffset.roundToPx()) }
                            .graphicsLayer {
                                val ratio =
                                    if (isCurrentWeek) 1 + animatedScaleRatio else 1 - animatedScaleRatio * (week + 1)
                                scaleX = ratio
                                scaleY = ratio
                            }
                            .shadow(
                                if (isCurrentWeek) animatedElevation else 0.dp,
                                RoundedCornerShape(14.dp)
                            )
                            .background(Color.White, shape = RoundedCornerShape(14.dp))
                    }

                    AnimationTypeUiState.SIZING -> {
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    }
                }

            WeekView(
                weekViewModifier,
                week,
                firstDayOfWeek,
                daysInPrevMonth,
                viewModel,
                month,
                today,
                dayCounter,
                daysInMonth,
                selectedDateStr,
                selectedMonth,
                isCurrentWeek,
                weekHeight,
                updateDayCounter = { dayCounter = it },
                onDateClick
            )
        }
    }
}

@Composable
private fun WeekView(
    modifier: Modifier,
    week: Int,
    firstDayOfWeek: Int,
    daysInPrevMonth: Int,
    viewModel: SwimmingViewModel,
    month: LocalDate,
    today: LocalDate,
    dayCounterStart: Int,
    daysInMonth: Int,
    selectedDateStr: MutableState<String>,
    selectedMonth: MutableState<LocalDate>,
    isCurrentWeek: Boolean,
    dayHeight: Dp,
    updateDayCounter: (Int) -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    var dayCounter = dayCounterStart

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val calendarMode by viewModel.calendarUiState

        val dayViewModifier =
            when (viewModel.animationType.value) {
                AnimationTypeUiState.OFFSET -> {
                    val alpha by remember { mutableFloatStateOf(1f) }
                    val height by remember { mutableStateOf(dayHeight) }

                    Modifier
                        .weight(1f)
                        .height(height)
                        .alpha(alpha)
                }

                AnimationTypeUiState.SIZING -> {
                    var height by remember { mutableStateOf(if (isCurrentWeek || calendarMode == CalendarUiState.MONTH_MODE || calendarMode == CalendarUiState.WEEK_MODE) dayHeight else 0.dp) }
                    var alpha by remember { mutableFloatStateOf(if (isCurrentWeek || calendarMode == CalendarUiState.MONTH_MODE || calendarMode == CalendarUiState.WEEK_MODE) 1f else 0f) }

                    LaunchedEffect(calendarMode) {
                        when (calendarMode) {
                            CalendarUiState.MONTH_MODE, CalendarUiState.TO_MONTH, CalendarUiState.WEEK_MODE -> {
                                height = dayHeight
                                alpha = 1f
                            }

                            CalendarUiState.TO_WEEK -> if (isCurrentWeek) {
                                height = dayHeight
                                alpha = 1f
                            } else {
                                height = 0.dp
                                alpha = 0f
                            }
                        }
                    }

                    val animatedAlpha by animateFloatAsState(alpha, animationSpec = tween())
                    val animatedHeight by animateDpAsState(
                        height,
                        animationSpec = tween(),
                        finishedListener = { _ ->
                            viewModel.animationCount.intValue += 1
                        })

                    Modifier
                        .weight(1f)
                        .height(animatedHeight)
                        .alpha(animatedAlpha)

                }
            }

        for (day in 0..6) {
            if (week == 0 && day < firstDayOfWeek) {
                // 이전 달 날짜 표시
                val preMonth = month.minusMonths(1L)
                val prevDay = daysInPrevMonth - firstDayOfWeek + day + 1
                var bgColor = ColorCalendarItemBgDis
                var borderColor = Color.Transparent
                val isThisMonth = calendarMode != CalendarUiState.MONTH_MODE && isCurrentWeek

                if (isThisMonth) {
                    val sameDate = prevDay.toString() == selectedDateStr.value
                    val sameMonth = preMonth.month == selectedMonth.value.month
                    if (sameDate && sameMonth) {
                        borderColor = ColorCalendarOnItemBorder
                        bgColor = ColorCalendarOnItemBg
                    } else {
                        bgColor = ColorCalendarItemBg
                    }
                }

                val animatedBgColor by animateColorAsState(bgColor, animationSpec = tween())

                DayView(
                    modifier = dayViewModifier
                        .background(animatedBgColor, shape = RoundedCornerShape(14.dp))
                        .border(
                            1.5.dp,
                            borderColor,
                            RoundedCornerShape(14.dp)
                        ),
                    viewModel = viewModel,
                    month = preMonth.withDayOfMonth(prevDay),
                    day = prevDay.toString(),
                    today = today,
                    isThisMonth = isThisMonth,
                    onDateClick = onDateClick
                )

            } else if (dayCounter > daysInMonth) {
                val nextMonth = month.plusMonths(1L)
                val nextDay = dayCounter - daysInMonth
                var bgColor = ColorCalendarItemBgDis
                var borderColor = Color.Transparent
                val isThisMonth = calendarMode != CalendarUiState.MONTH_MODE && isCurrentWeek

                if (isThisMonth) {
                    val sameDate = nextDay.toString() == selectedDateStr.value
                    val sameMonth = nextMonth.month == selectedMonth.value.month
                    if (sameDate && sameMonth) {
                        borderColor = ColorCalendarOnItemBorder
                        bgColor = ColorCalendarOnItemBg
                    } else {
                        bgColor = ColorCalendarItemBg
                    }
                }

                val animatedBgColor by animateColorAsState(bgColor, animationSpec = tween())
                // 다음 달 날짜 표시
                DayView(
                    modifier = dayViewModifier
                        .background(animatedBgColor, shape = RoundedCornerShape(14.dp))
                        .border(
                            1.5.dp,
                            borderColor,
                            RoundedCornerShape(14.dp)
                        ),
                    viewModel = viewModel,
                    month = nextMonth.withDayOfMonth(nextDay),
                    day = nextDay.toString(),
                    today = today,
                    isThisMonth = isThisMonth,
                    onDateClick = onDateClick
                )

                updateDayCounter(++dayCounter)
            } else {
                // 이번 달 날짜 표시
                if (week > 0 || day >= firstDayOfWeek) {
                    val sameDate = dayCounter.toString() == selectedDateStr.value
                    val sameMonth = month.month == selectedMonth.value.month
                    val borderColor = if (sameDate && sameMonth) {
                        ColorCalendarOnItemBorder
                    } else {
                        Color.Transparent
                    }

                    val bgColor = if (sameDate && sameMonth) {
                        ColorCalendarOnItemBg
                    } else {
                        ColorCalendarItemBg
                    }

                    DayView(
                        modifier = dayViewModifier
                            .background(bgColor, shape = RoundedCornerShape(14.dp))
                            .border(
                                1.5.dp,
                                borderColor,
                                RoundedCornerShape(14.dp)
                            ),
                        viewModel = viewModel,
                        month = month.withDayOfMonth(dayCounter),
                        day = dayCounter.toString(),
                        today = today,
                        isThisMonth = true,
                        onDateClick = onDateClick
                    )

                    updateDayCounter(++dayCounter)
                }
            }
        }
    }
}

@Composable
private fun DayView(
    modifier: Modifier,
    viewModel: SwimmingViewModel,
    month: LocalDate,
    day: String,
    today: LocalDate,
    isThisMonth: Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    val thisDate = month.withDayOfMonth(day.toInt())

    Box(modifier = modifier
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onDateClick(thisDate) }
        )
        .padding(1.dp))
    {
        val dateBorderColor =
            if (today == thisDate) ColorCalendarOnItemBorder else Color.Transparent
        val dateBgColor =
            if (isThisMonth)
                if (today == thisDate) ColorCalendarTodayBg else ColorCalendarDateBg
            else ColorCalendarDateBgDis
        val dateTextColor =
            if (isThisMonth)
                if (today == thisDate) ColorCalendarToday else ColorCalendarDate
            else ColorCalendarDateDis

        val animatedTextColor by animateColorAsState(dateTextColor, animationSpec = tween())

        // 날짜 박스
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(15.dp)
                .border(1.dp, dateBorderColor, RoundedCornerShape(5.dp))
                .background(dateBgColor, RoundedCornerShape(5.dp))
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center),
                text = day, // 날짜만 표시
                fontSize = 10.sp,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center,
                fontFamily = notoSansKr,
                color = animatedTextColor
            )
        }

        val dailyRecords by viewModel.dailyRecords.collectAsState()
        val dailyRecord = remember {
            derivedStateOf {
                dailyRecords[thisDate.atStartOfDay().atZone(ZoneId.systemDefault())]
            }
        }

        dailyRecord.value?.let {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = dailyRecord.value!!.totalDistance!!,
                color = Color.Gray,
                fontFamily = notoSansKr,
                fontSize = 8.sp,
                lineHeight = 8.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val colorCrawl = if (isThisMonth) ColorCrawl else ColorCrawlSecondary
            val colorBackStroke = if (isThisMonth) ColorBackStroke else ColorBackStrokeSecondary
            val colorBreastStroke =
                if (isThisMonth) ColorBreastStroke else ColorBreastStrokeSecondary
            val colorButterfly = if (isThisMonth) ColorButterfly else ColorButterflySecondary
            val colorKickBoard = if (isThisMonth) ColorKickBoard else ColorKickBoardSecondary
            val colorMixStart = if (isThisMonth) ColorMixStart else ColorMixStartSecondary
            val colorMixEnd = if (isThisMonth) ColorMixEnd else ColorMixEndSecondary

            val animatedColorCrawl by animateColorAsState(colorCrawl, animationSpec = tween())
            val animatedColorBackStroke by animateColorAsState(
                colorBackStroke,
                animationSpec = tween()
            )
            val animatedColorBreastStroke by animateColorAsState(
                colorBreastStroke,
                animationSpec = tween()
            )
            val animatedColorButterfly by animateColorAsState(
                colorButterfly,
                animationSpec = tween()
            )
            val animatedColorKickBoard by animateColorAsState(
                colorKickBoard,
                animationSpec = tween()
            )
            val animatedColorMixStart by animateColorAsState(
                colorMixStart,
                animationSpec = tween()
            )
            val animatedColorMixEnd by animateColorAsState(colorMixEnd, animationSpec = tween())

            val brushList = remember(isThisMonth) {
                derivedStateOf {
                    dailyRecord.value?.let { record ->
                        // 거리 정보를 리스트로 변환
                        val distances = mapOf(
                            SolidColor(animatedColorCrawl) to record.crawl,
                            SolidColor(animatedColorBackStroke) to record.backStroke,
                            SolidColor(animatedColorBreastStroke) to record.breastStroke,
                            SolidColor(animatedColorButterfly) to record.butterfly,
                            SolidColor(animatedColorKickBoard) to record.kickBoard,
                            Brush.verticalGradient(
                                Pair(0f, animatedColorMixStart),
                                Pair(1f, animatedColorMixEnd)
                            ) to record.mixed
                        )

                        val ratioList = distributeDistance(distances, 8)
                        ratioList
                    } ?: emptyList<Brush>()

                }
            }

            if (brushList.value.isNotEmpty()) {
                IconWithPolygon(
                    painterResource(id = R.drawable.ic_pearl2),
                    brushList.value,
                    28.dp,
                    16.dp,
                    false
                )
            }
        }
    }
}

@Composable
private fun CalendarHeaderView(
    viewModel: SwimmingViewModel,
    contentsBg: Color
) {
    val currentMonth by viewModel.currentMonth
    val currentMonthTotal by viewModel.currentMonthTotal.collectAsState()
    val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
    // 년, 월
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = currentMonth.format(monthFormatter),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(top = 10.dp, start = 5.dp, end = 5.dp)
                .background(contentsBg, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 15.dp),
            textAlign = TextAlign.Center
        )

        val totalDistance =
            remember { derivedStateOf { currentMonthTotal.totalDistance ?: "0" } }
        val totalCaloriesBurned =
            remember { derivedStateOf { currentMonthTotal.totalEnergyBurned ?: "0" } }

        Column(Modifier.wrapContentSize(), verticalArrangement = Arrangement.Center) {
            Text(
                totalDistance.value + "m",
                color = Color.Gray,
                fontFamily = notoSansKr,
                fontSize = 12.sp,
                lineHeight = 12.sp,
            )
            Text(
                totalCaloriesBurned.value.toFloat().roundToInt().toString() + " kcal",
                color = Color.Gray,
                fontFamily = notoSansKr,
                fontSize = 12.sp,
                lineHeight = 12.sp,
            )
        }

    }

    // 요일 헤더
    Row(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp, top = 5.dp)
            .background(contentsBg, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "일",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            color = Color.Red
        )

        listOf("월", "화", "수", "목", "금").forEach {
            Text(
                text = it,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "토",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            color = Color.Blue
        )
    }
}

@Composable
fun CalendarDetailView(
    modifier: Modifier,
    viewModel: SwimmingViewModel,
    resizeBar: @Composable (Modifier) -> Unit
) {
    Box(modifier = modifier.clipToBounds()) {
        AnimatedContent(
            targetState = viewModel.calendarUiState.value in setOf(
                CalendarUiState.WEEK_MODE,
                CalendarUiState.TO_WEEK
            ),
            modifier = Modifier,
            label = "CalendarUiState",
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { targetState ->
            var calendarMode by viewModel.calendarUiState
            val onModeChange = remember {
                {
                    if (calendarMode == CalendarUiState.WEEK_MODE) {
                        calendarMode = CalendarUiState.TO_MONTH
                    } else if (calendarMode == CalendarUiState.MONTH_MODE) {
                        calendarMode = CalendarUiState.TO_WEEK
                    }
                }
            }

            val detailRecords by viewModel.currentDetailRecords.collectAsState()

            LaunchedEffect(detailRecords) {
                viewModel.calculateTotalDetailRecord()
            }

            if (viewModel.calendarUiState.value == CalendarUiState.MONTH_MODE) {
                resizeBar(Modifier)
            }

            Column(
                Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top, unbounded = true)
                    .padding(start = 10.dp, end = 10.dp, bottom = 5.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (targetState) {
                    detailRecords.forEach { detailRecordWithHR ->
                        DetailModeContent(detailRecordWithHR, onModeChange) {
                            viewModel.setModifyRecord(detailRecordWithHR.detailRecord)
                            viewModel.popupUiState.value = PopupUiState.MODIFY
                        }
                    }
                } else {
                    val totalDetailRecordWithHR by viewModel.totalDetailRecordWithHR.collectAsState()

                    totalDetailRecordWithHR?.let {
                        MonthModeContent(it, onModeChange)
                    }
                }

            }
        }
    }
}


@Composable
fun MonthModeContent(
    totalDetailRecordWithHR: DetailRecordWithHR,
    onDetailClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = totalDetailRecordWithHR.detailRecord.distance + "m",
            fontSize = 36.dp.toSp,
            lineHeight = 36.dp.toSp,
        )

        Button(onClick = onDetailClick) { Text("detail") }
    }

    DetailDataView(totalDetailRecordWithHR)
}

@Composable
fun DetailModeContent(
    detailRecordWithHR: DetailRecordWithHR,
    onCloseClick: () -> Unit = {},
    onModifyClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = detailRecordWithHR.detailRecord.distance + "m",
            fontSize = 36.dp.toSp,
            lineHeight = 36.dp.toSp,
        )

        Text(
            text = detailRecordWithHR.detailRecord.startTime.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
            style = MaterialTheme.typography.bodySmall
        )
        // 임시 수정버튼
        Button(
            modifier = Modifier.height(24.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(),
            onClick = onModifyClick
        ) {
            Text(text = "영법 수정", fontSize = 12.sp)
        }

        // 임시 닫기버튼
        Button(
            modifier = Modifier.height(24.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(),
            onClick = onCloseClick
        ) {
            Text(text = "닫기 ", fontSize = 12.sp)
        }
    }

    DetailDataView(detailRecordWithHR)
}

@Composable
private fun DetailDataView(
    detailRecordWithHR: DetailRecordWithHR,
    isDetail: Boolean = false
) {
    val detailRecord = detailRecordWithHR.detailRecord

    val activeTime = detailRecord.activeTime?.let { Duration.parse(it) } ?: Duration.ZERO
    val calories = detailRecord.energyBurned?.toDouble() ?: 0.0
    val maxHR = detailRecord.maxHeartRate?.toInt() ?: 0
    val minHR = detailRecord.minHeartRate?.toInt() ?: 0

    val animationSpec = spring(
        visibilityThreshold = Int.VisibilityThreshold, stiffness = Spring.StiffnessLow
    )
    val animatedCrawl by animateIntAsState(detailRecord.crawl, animationSpec)
    val animatedBackStroke by animateIntAsState(detailRecord.backStroke, animationSpec)
    val animatedBreastStroke by animateIntAsState(detailRecord.breastStroke, animationSpec)
    val animatedButterfly by animateIntAsState(detailRecord.butterfly, animationSpec)
    val animatedKickBoard by animateIntAsState(detailRecord.kickBoard, animationSpec)
    val animatedMixed by animateIntAsState(detailRecord.mixed, animationSpec)

    val distanceList = listOf(
        Triple(detailRecord.crawl, animatedCrawl, SolidColor(ColorCrawl)),
        Triple(detailRecord.backStroke, animatedBackStroke, SolidColor(ColorBackStroke)),
        Triple(
            detailRecord.breastStroke,
            animatedBreastStroke,
            SolidColor(ColorBreastStroke)
        ),
        Triple(detailRecord.butterfly, animatedButterfly, SolidColor(ColorButterfly)),
        Triple(
            detailRecord.mixed, animatedMixed, Brush.verticalGradient(
                Pair(0f, ColorMixStart),
                Pair(1f, ColorMixEnd)
            )
        ),
        Triple(detailRecord.kickBoard, animatedKickBoard, SolidColor(ColorKickBoard))
    )

    Column {
        Row(
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("시간")
                Text(activeTime.toCustomTimeString())
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("칼로리")
                Text(calories.toInt().toString())
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("최대 심박")
                Text(maxHR.toString())
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("최소 심박")
                Text(minHR.toString())
            }
        }
    }

    Column(
        Modifier
            .padding(top = 5.dp)
            .fillMaxWidth()
            .background(
                SkyBlue6, shape = RoundedCornerShape(15.dp)
            )
            .padding(8.dp)
    ) {
        var refValue by remember { mutableIntStateOf(1000) }
        val animatedRefVal by animateIntAsState(
            refValue, spring(
                visibilityThreshold = Int.VisibilityThreshold,
                stiffness = 200f
            )
        )

        distanceList.filter {
            it.first != 0
        }.sortedByDescending {
            it.first
        }.forEachIndexed { i, it ->
            if (i == 0) refValue = max(1000, it.first)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .height(30.dp)
                        .fillMaxWidth(it.second / animatedRefVal.toFloat())
                        .background(
                            it.third,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(end = 7.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (it.first >= 75) Text(
                        it.second.toString(),
                        lineHeight = 14.dp.toSp,
                        fontSize = 14.dp.toSp,
                        color = Color.Black.copy(0.8f)
                    )
                }

                if (it.first < 75) Text(
                    it.second.toString(),
                    lineHeight = 14.dp.toSp,
                    fontSize = 14.dp.toSp,
                    color = Color.Black.copy(0.8f)
                )
            }
        }
    }
}

@Composable
fun ResizeBar(
    modifier: Modifier = Modifier,
    animatableOffset: Animatable<Dp, AnimationVector1D>,
    initialOffset: Dp,
    minOffset: Dp
) {
    val scope = rememberCoroutineScope()
    val velocityTracker = remember { VelocityTracker() } // 속도 추적기
    var currentOffset by remember { mutableStateOf(initialOffset) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPointerInputChange(change) // 위치 기록

                        // 현재 높이 조절 (실시간 변경)
                        val newOffset = min(
                            initialOffset, // 최소 크기 제한
                            max(
                                minOffset,
                                animatableOffset.value + dragAmount.y.toDp()
                            )
                        )
                        scope.launch {
                            animatableOffset.snapTo(newOffset)
                        }
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().y  // Y축 속도(px/s)
                        val thresholdVelocity = 1000f  // 임계 속도 (px/s)
                        Log.d("=-=-=", "velocity: $velocity")
                        val range = initialOffset - minOffset
                        val thresholdPosition = range * 0.2f

                        val targetOffset = when {
                            // 빠르게 위로 스와이프 → 최대 크기
                            velocity < -thresholdVelocity -> minOffset
                            // 빠르게 아래로 스와이프 → 초기 크기
                            velocity > thresholdVelocity -> initialOffset
                            // 현재 높이가 최소 높이이고, 30% 이상 올라갔으면 최대 크기
                            currentOffset == initialOffset && animatableOffset.value < initialOffset - thresholdPosition -> minOffset
                            // 현재 높이가 최대 높이이고, 30% 이상 내려갔으면 초기 크기
                            currentOffset == minOffset && animatableOffset.value > minOffset + thresholdPosition -> initialOffset
                            else -> if (initialOffset - currentOffset < currentOffset - minOffset) initialOffset else minOffset
                        }

                        currentOffset = targetOffset
                        Log.d("ResizeBar", "currentOffset: $currentOffset")

                        scope.launch {
                            animatableOffset.animateTo(
                                targetOffset, spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                )
                            )
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(5.dp)
                .size(width = 80.dp, height = 5.dp)
                .background(
                    Color.Gray.copy(alpha = 0.6f),
                    RoundedCornerShape(50)
                )
                .align(Alignment.TopCenter)
        )
    }
}

private fun calculateMonthDifference(currentDate: LocalDate, targetDate: LocalDate): Int {
    val currentMonth = currentDate.monthValue
    val targetMonth = targetDate.monthValue
    val currentYear = currentDate.year
    val targetYear = targetDate.year

    var monthDifference = currentMonth - targetMonth
    val yearDifference = currentYear - targetYear

    if (yearDifference > 0) {
        monthDifference += yearDifference * 12
    }

    if (monthDifference < 0) {
        monthDifference += 12
    }

    return monthDifference % 12
}

@Preview(widthDp = 100, heightDp = 100)
@Composable
fun ShrimpIconWithBoxPreview() {
    Column {
        IconWithPolygon(
            painterResource(id = R.drawable.ic_pearl2),
            listOf(
                SolidColor(ColorCrawl),
                SolidColor(ColorBackStroke),
                SolidColor(ColorBreastStroke),
                SolidColor(ColorButterfly),
                SolidColor(ColorKickBoard),
                Brush.verticalGradient(
                    Pair(0f, ColorMixStart),
                    Pair(1f, ColorMixEnd)
                )
            ), 30.dp, 30.dp
        )

        IconWithPolygon(
            painterResource(id = R.drawable.ic_pearl2),
            listOf(
                SolidColor(ColorCrawl),
                SolidColor(ColorBackStroke),
                SolidColor(ColorBreastStroke),
                SolidColor(ColorButterfly),
                SolidColor(ColorKickBoard),
                Brush.verticalGradient(
                    Pair(0f, ColorMixStart),
                    Pair(1f, ColorMixEnd)
                )
            ), 30.dp, 30.dp, false
        )
    }
}

@Composable
fun IconWithPolygon(
    painter: Painter,
    brushList: List<Brush>,
    diameter: Dp,
    iconSize: Dp,
    isRotate: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(diameter + iconSize)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        val radius = with(LocalDensity.current) { diameter.toPx() / 2 }
        val offsetAngle = 360 / brushList.size.toFloat()

        brushList.forEachIndexed { index, brush ->
            val angle = index * offsetAngle
            val radian = Math.toRadians(angle.toDouble())
            val offsetX = radius * cos(radian).toFloat()
            val offsetY = radius * sin(radian).toFloat()

            Icon(
                painter = painter,
                contentDescription = "graph",
                modifier = Modifier
                    .size(iconSize)
                    .offset(offsetX.toDp(), offsetY.toDp())
                    .graphicsLayer(
                        rotationZ = if (isRotate) angle + 90f else 0f, // 이미지 여백을 위해 기본으로 20도 돌림
                        compositingStrategy = CompositingStrategy.Offscreen
                    )
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(
                                brush,
                                blendMode = BlendMode.SrcAtop
                            )
                        }
                    },
                tint = Color.Unspecified
            )
        }

        val firstBrush = brushList.firstOrNull()
        firstBrush?.let {
            val radian = Math.toRadians(0.0)
            val offsetX = radius * cos(radian).toFloat()
            val offsetY = radius * sin(radian).toFloat()

            Icon(
                painter = painter,
                contentDescription = "graph",
                modifier = Modifier
                    .size(iconSize)
                    .offset(offsetX.toDp(), offsetY.toDp())
                    .graphicsLayer(
                        rotationZ = if (isRotate) 90f else 0f,
                        compositingStrategy = CompositingStrategy.Offscreen
                    )
                    .drawWithCache {
                        onDrawWithContent {
                            // 왼쪽 절반 그리기
                            val (width, height) = if (isRotate) {
                                (size.width / 2) to size.height
                            } else {
                                size.width to size.height / 2
                            }

                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = width,
                                bottom = height
                            ) {
                                rotate(0f, center) {
                                    this@onDrawWithContent.drawContent()
                                }
                            }

                            // 오른쪽 절반 그리기 (겹치는 부분만)
                            drawRect(
                                firstBrush,
                                size = size,
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    },
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun HexagonCircleGraph(
    brushList: List<Brush>,
    size: Dp,
    radius: Dp,
    circleRadius: Dp,
    blendMode: BlendMode = BlendMode.Luminosity
) {
    Canvas(
        modifier = Modifier
            .size(size)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val diameter = radius * 2
        val offsetX = sqrt(diameter.value.pow(2) - radius.value.pow(2)).dp

        // 첫 번째 줄 (1개)
        drawCircle(
            brush = brushList[0],
            radius = circleRadius.toPx(),
            center = Offset(center.x, center.y - diameter.toPx()),
            blendMode = blendMode
        )

        // 두 번째 줄 (2개)
        drawCircle(
            brush = brushList[5],
            radius = circleRadius.toPx(),
            center = Offset(
                center.x - offsetX.toPx(),
                center.y - radius.toPx()
            ),
            blendMode = blendMode
        )

        drawCircle(
            brush = brushList[1],
            radius = circleRadius.toPx(),
            center = Offset(
                center.x + offsetX.toPx(),
                center.y - radius.toPx()
            ),
            blendMode = blendMode
        )

        // 세 번째 줄 (2개)
        drawCircle(
            brush = brushList[4],
            radius = circleRadius.toPx(),
            center = Offset(
                center.x - offsetX.toPx(),
                center.y + radius.toPx()
            ),
            blendMode = blendMode
        )

        drawCircle(
            brush = brushList[2],
            radius = circleRadius.toPx(),
            center = Offset(
                center.x + offsetX.toPx(),
                center.y + radius.toPx()
            ),
            blendMode = blendMode
        )

        // 네 번째 줄 (1개)
        drawCircle(
            brush = brushList[3],
            radius = circleRadius.toPx(),
            center = Offset(center.x, center.y + diameter.toPx()),
            blendMode = blendMode
        )
    }
}

fun Float.toDp() = (this / Resources.getSystem().displayMetrics.density).dp

@Preview
@Composable
fun DetailViewPreview() {
//    Box(){
//    CalendarDetailView(
//        modifier = Modifier.align(Alignment.BottomCenter),
//        viewModel = PreviewViewmodel(),
//        currentDate = Instant.now().truncatedTo(ChronoUnit.DAYS),
//        initialHeight = 230
//    )}
}

class PreviewViewmodel {
    val popupUiState = mutableStateOf(PopupUiState.NONE)
    val currentDetailRecord: StateFlow<List<DetailRecordWithHR?>> =
        MutableStateFlow(
            listOf(
                DetailRecordWithHR(
                    DetailRecord(
                        id = "123123",
                        startTime = Instant.now().minusSeconds(22324L),
                        endTime = Instant.now(),
                        activeTime = "PT1H6M7.515S",
                        distance = "1200",
                        energyBurned = "364.23143454",
                        minHeartRate = 140L,
                        maxHeartRate = 190L,
                        avgHeartRate = 160L,
                        poolLength = 25,
                        crawl = 0,
                        backStroke = 0,
                        breastStroke = 0,
                        butterfly = 0,
                        kickBoard = 0,
                        mixed = 0
                    ), emptyList<HeartRateSample>()
                )
            )
        )

    private val _currentModifyRecord =
        MutableStateFlow<DetailRecord?>(null)

    fun setModifyRecord(record: DetailRecord?) {
        _currentModifyRecord.value = record
    }
}

fun Duration.toCustomTimeString(): String {
    val hours = this.inWholeHours
    val minutes = this.inWholeMinutes % 60
    val seconds = this.inWholeSeconds % 60

    val parts = mutableListOf<String>()
    if (hours > 0) parts.add("${hours}시간")
    if (minutes > 0) parts.add("${minutes}분")
    if (seconds > 0) parts.add("${seconds}초")

    return if (parts.isNotEmpty()) parts.joinToString(" ") else "기록 없음"
}

// 시스템 설정과 상관 없이 text 크기 고정
val Dp.toSp: TextUnit @Composable get() = with(LocalDensity.current) { this@toSp.toSp() }

fun distributeDistance(distances: Map<Brush, Int>, size: Int = 6): List<Brush> {
    val total = distances.values.sum() // 전체 합
    val proportions = distances.mapValues { (it.value * size).toDouble() / total } // 비율 계산

    val intParts = proportions.mapValues { it.value.toInt() } // 정수 부분 할당
    var remaining = size - intParts.values.sum() // 남은 개수

    // 결과 리스트
    val resultList = mutableListOf<Brush>()

    // 정수 개수만큼 먼저 추가
    for ((key, count) in intParts) {
        repeat(count) { resultList.add(key) }
    }

    // 남은 개수를 가장 비율이 높은 순으로 채워 넣기
    val sortedEntries = proportions.entries.sortedByDescending { it.value % 1 } // 소수점 부분 기준 정렬
    for ((key, _) in sortedEntries) {
        if (remaining > 0) {
            resultList.add(key)
            remaining--
        } else break
    }

    val groupedList = resultList.sortedBy { resultList.indexOf(it) }.toMutableList()

    val shift = Random.nextInt(0, size)
//    val finalList = groupedList.drop(shift) + groupedList.take(shift)

//    return finalList
    return groupedList
}