package kr.ilf.soodal.ui

import android.content.res.Resources
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.ilf.soodal.R
import kr.ilf.soodal.database.entity.DailyRecord
import kr.ilf.soodal.database.entity.DetailRecord
import kr.ilf.soodal.database.entity.DetailRecordWithHR
import kr.ilf.soodal.database.entity.HeartRateSample
import kr.ilf.soodal.ui.theme.ColorBackStroke
import kr.ilf.soodal.ui.theme.ColorBackStrokeSecondary
import kr.ilf.soodal.ui.theme.ColorBreastStroke
import kr.ilf.soodal.ui.theme.ColorBreastStrokeSecondary
import kr.ilf.soodal.ui.theme.ColorButterfly
import kr.ilf.soodal.ui.theme.ColorButterflySecondary
import kr.ilf.soodal.ui.theme.ColorCalDate
import kr.ilf.soodal.ui.theme.ColorCalDateDis
import kr.ilf.soodal.ui.theme.ColorCalItemBg
import kr.ilf.soodal.ui.theme.ColorCalItemBgDis
import kr.ilf.soodal.ui.theme.ColorCalSelectedBg
import kr.ilf.soodal.ui.theme.ColorCalSelectedBorder
import kr.ilf.soodal.ui.theme.ColorCalSelectedBorderSecondary
import kr.ilf.soodal.ui.theme.ColorCalToday
import kr.ilf.soodal.ui.theme.ColorCrawl
import kr.ilf.soodal.ui.theme.ColorCrawlSecondary
import kr.ilf.soodal.ui.theme.ColorKickBoard
import kr.ilf.soodal.ui.theme.ColorKickBoardSecondary
import kr.ilf.soodal.ui.theme.ColorMixEnd
import kr.ilf.soodal.ui.theme.ColorMixEndSecondary
import kr.ilf.soodal.ui.theme.ColorMixStart
import kr.ilf.soodal.ui.theme.ColorMixStartSecondary
import kr.ilf.soodal.ui.theme.ColorTextDefault
import kr.ilf.soodal.ui.theme.notoSansKr
import kr.ilf.soodal.viewmodel.CalendarUiState
import kr.ilf.soodal.viewmodel.CalendarViewModel
import kr.ilf.soodal.viewmodel.PopupUiState
import kr.ilf.soodal.viewmodel.UiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration

val selectedMonthSaver =
    mapSaver(
        save = { mapOf("selectedMonth" to it) },
        restore = { it["selectedMonth"] as LocalDate })

@Composable
fun CalendarView(
    modifier: Modifier,
    headerHeight: Dp,
    weekHeight: Dp,
    spacing: Dp,
    contentsBg: Color,
    viewModel: CalendarViewModel
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
        if (calendarMode in setOf(CalendarUiState.WEEK_MODE, CalendarUiState.TO_MONTH)) {
            currentWeek = todayWeek.minusWeeks(weekPagerState.currentPage.toLong())

            val monthDifference = calculateMonthDifference(today, currentWeek)
            currentMonth = today.withDayOfMonth(1).minusMonths(monthDifference.toLong())
            coroutineScope.launch {
                monthPagerState.scrollToPage(monthDifference)
            }
        }
    }

    LaunchedEffect(monthPagerState.currentPage) {
        if (calendarMode in setOf(CalendarUiState.MONTH_MODE, CalendarUiState.TO_WEEK)) {
            currentMonth = today.withDayOfMonth(1).minusMonths(monthPagerState.currentPage.toLong())

            val selectedDate = selectedMonth.value.withDayOfMonth(selectedDateStr.value.toInt())
            val minusDays = selectedDate.dayOfWeek.value % 7 - 3L
            val selectedWeek = selectedDate.minusDays(minusDays)

            currentWeek = when {
                selectedMonth.value.year == currentMonth.year && selectedMonth.value.month == currentMonth.month -> selectedWeek
                selectedWeek.plusDays(3L).month == currentMonth.month || selectedWeek.minusDays(3L).month == currentMonth.month -> selectedWeek
                else -> currentMonth.minusDays(currentMonth.dayOfWeek.value % 7 - 3L)
            }

            val weekTarget = ChronoUnit.WEEKS.between(currentWeek, todayWeek).toInt()
            coroutineScope.launch {
                initialWeekPage = weekTarget
                weekPagerState.scrollToPage(weekTarget)
            }
        }

        viewModel.updateDailyRecords()
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CalendarHeaderView(viewModel, headerHeight, spacing, contentsBg)

        HorizontalPager(
            state = if (calendarMode == CalendarUiState.WEEK_MODE) weekPagerState else monthPagerState,
            userScrollEnabled = calendarMode == CalendarUiState.WEEK_MODE || calendarMode == CalendarUiState.MONTH_MODE,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(contentsBg, shape = RoundedCornerShape(10.dp))
                .padding(vertical = 5.dp, horizontal = 5.dp),
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
                        .padding(horizontal = 2.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
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
                    spacing,
                    {}
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
                        else -> {
                            selectedMonth.value = clickedDate
                            selectedDateStr.value = clickedDate.dayOfMonth.toString()

                            viewModel.findDetailRecord(
                                clickedDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant()
                            )

                            val monthDifference = calculateMonthDifference(today, clickedDate)
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
                    weekHeight,
                    spacing,
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
    viewModel: CalendarViewModel,
    month: LocalDate,
    selectedMonth: MutableState<LocalDate>,
    selectedDateStr: MutableState<String>,
    today: LocalDate,
    weekHeight: Dp,
    spacing: Dp,
    onDateClick: (LocalDate) -> Unit
) {
    val density = LocalDensity.current

    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.withDayOfMonth(1)
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7) // 0: Sunday, 6: Saturda
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()
    val selectedDate =
        selectedMonth.value.withDayOfMonth(selectedDateStr.value.toInt())

    val currentWeek by viewModel.currentWeek
    val currentWeekCount = remember(currentWeek, selectedDate, viewModel.currentMonth.value) {
        // msms 조건 재확인 필요 일단은 됨
        if (viewModel.currentMonth.value.month == month.month)
            if (currentWeek.month == month.month && currentWeek.year == month.year) {
                getWeekOfMonth(currentWeek) - 1
            } else if (currentWeek.plusDays(3L).month == month.month) {
                getWeekOfMonth(currentWeek.plusDays(3L)) - 1
            } else if (currentWeek.minusDays(3L).month == month.month) {
                getWeekOfMonth(currentWeek.minusDays(3L)) - 1
            } else {
                getWeekOfMonth(selectedDate) - 1
            }
        else {
            0
        }
    }

    val calendarMode by viewModel.calendarUiState
    val offset by remember {
        mutableIntStateOf(
            if (calendarMode == CalendarUiState.MONTH_MODE || calendarMode == CalendarUiState.WEEK_MODE) 0 else with(
                density
            ) {
                (weekHeight + spacing).toPx().roundToInt() * currentWeekCount
            }) // dp 를 px로 변환 시 소수점 차이로 offset값이 안맞아서 반올림 후 곱함
    }

    val animatedOffset = remember {
        Animatable(
            initialValue = offset,
            Int.VectorConverter,
            Int.VisibilityThreshold
        )
    }
    LaunchedEffect(calendarMode) {
        when (calendarMode) {
            CalendarUiState.MONTH_MODE, CalendarUiState.TO_MONTH, CalendarUiState.WEEK_MODE -> animatedOffset.animateTo(
                0,
                tween(500)
            )

            CalendarUiState.TO_WEEK -> {
                val targetOffset =
                    with(density) { (weekHeight + spacing).toPx().roundToInt() * currentWeekCount }
                animatedOffset.animateTo(targetOffset, tween(500))
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .wrapContentSize()
            .clipToBounds()
            .offset { IntOffset(0, -animatedOffset.value) }
            .background(Color.Transparent),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // 날짜 표시
        var dayCounter = 1

        // 주 단위로 날짜를 표시
        for (week in 0..5) { // 최대 6주까지 표시
            // msms 조건 재확인 필요 일단은 됨
            val isCurrentWeek =
                if (viewModel.currentMonth.value.month == month.month) {
                    if (currentWeek.month == month.month && currentWeek.year == month.year) {
                        week == (getWeekOfMonth(currentWeek) - 1)
                    } else if (currentWeek.plusDays(3L).month == month.month) {
                        week == (getWeekOfMonth(currentWeek.plusDays(3L)) - 1)
                    } else if (currentWeek.minusDays(3L).month == month.month) {
                        week == (getWeekOfMonth(currentWeek.minusDays(3L)) - 1)
                    } else {
                        week == (getWeekOfMonth(selectedDate) - 1)
                    }
                } else false

            WeekView(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
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
                spacing,
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
    viewModel: CalendarViewModel,
    month: LocalDate,
    today: LocalDate,
    dayCounterStart: Int,
    daysInMonth: Int,
    selectedDateStr: MutableState<String>,
    selectedMonth: MutableState<LocalDate>,
    isCurrentWeek: Boolean,
    dayHeight: Dp,
    spacing: Dp,
    updateDayCounter: (Int) -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    var dayCounter = dayCounterStart

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        val calendarMode by viewModel.calendarUiState

        for (day in 0..6) {
            if (week == 0 && day < firstDayOfWeek) {
                // 이전 달 날짜 표시
                val preMonth = month.minusMonths(1L)
                val prevDay = daysInPrevMonth - firstDayOfWeek + day + 1

                val isNotMonthMode = calendarMode != CalendarUiState.MONTH_MODE
                val isActive = isNotMonthMode && isCurrentWeek
                val sameDate = prevDay.toString() == selectedDateStr.value
                val sameMonth = preMonth.month == selectedMonth.value.month

                val bgColor = when {
                    sameDate && sameMonth && isActive -> ColorCalSelectedBg
                    isActive -> ColorCalItemBg
                    else -> ColorCalItemBgDis
                }
                val borderColor =
                    if (sameDate && sameMonth) ColorCalSelectedBorderSecondary else Color.Transparent

                val animatedBgColor by animateColorAsState(bgColor, animationSpec = tween())

                DayView(
                    viewModel = viewModel,
                    height = dayHeight,
                    backgroundColor = animatedBgColor,
                    borderColor = borderColor,
                    month = preMonth.withDayOfMonth(prevDay),
                    day = prevDay.toString(),
                    today = today,
                    isActive = isActive,
                    onDateClick = onDateClick
                )

            } else if (dayCounter > daysInMonth) {
                val nextMonth = month.plusMonths(1L)
                val nextDay = dayCounter - daysInMonth

                val isNotMonthMode = calendarMode != CalendarUiState.MONTH_MODE
                val isActive = isNotMonthMode && isCurrentWeek
                val sameDate = nextDay.toString() == selectedDateStr.value
                val sameMonth = nextMonth.month == selectedMonth.value.month

                val bgColor = when {
                    sameDate && sameMonth && isActive -> ColorCalSelectedBg
                    isActive -> ColorCalItemBg
                    else -> ColorCalItemBgDis
                }
                val borderColor =
                    if (sameDate && sameMonth) ColorCalSelectedBorderSecondary else Color.Transparent

                val animatedBgColor by animateColorAsState(bgColor, animationSpec = tween())

                // 다음 달 날짜 표시
                DayView(
                    viewModel = viewModel,
                    height = dayHeight,
                    backgroundColor = animatedBgColor,
                    borderColor = borderColor,
                    month = nextMonth.withDayOfMonth(nextDay),
                    day = nextDay.toString(),
                    today = today,
                    isActive = isActive,
                    onDateClick = onDateClick
                )

                updateDayCounter(++dayCounter)
            } else {
                // 이번 달 날짜 표시
                if (week > 0 || day >= firstDayOfWeek) {
                    val sameDate = dayCounter.toString() == selectedDateStr.value
                    val sameMonth = month.month == selectedMonth.value.month
                    val borderColor = if (sameDate && sameMonth) {
                        ColorCalSelectedBorder
                    } else {
                        Color.Transparent
                    }

                    val bgColor = if (sameDate && sameMonth) {
                        ColorCalSelectedBg
                    } else {
                        ColorCalItemBg
                    }

                    DayView(
                        viewModel = viewModel,
                        height = dayHeight,
                        backgroundColor = bgColor,
                        borderColor = borderColor,
                        month = month.withDayOfMonth(dayCounter),
                        day = dayCounter.toString(),
                        today = today,
                        isActive = true,
                        onDateClick = onDateClick
                    )

                    updateDayCounter(++dayCounter)
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayView(
    viewModel: CalendarViewModel,
    height: Dp,
    backgroundColor: Color,
    borderColor: Color,
    month: LocalDate,
    day: String,
    today: LocalDate,
    isActive: Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    val calendarMode by viewModel.calendarUiState
    val thisDate = month.withDayOfMonth(day.toInt())

    val dailyRecords by viewModel.dailyRecords.collectAsState()
    val dailyRecord by remember {
        derivedStateOf {
            dailyRecords[thisDate.atStartOfDay().atZone(ZoneId.systemDefault())]
        }
    }

    val modifier = Modifier
        .weight(1f)
        .height(height)
        .background(backgroundColor, shape = RoundedCornerShape(14.dp))
        .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            enabled = calendarMode in setOf(CalendarUiState.MONTH_MODE, CalendarUiState.WEEK_MODE),
            indication = null,
            onClick = { onDateClick(thisDate) }
        )
        .padding(0.5.dp)

    Box(modifier = modifier) {
        val dateBorderColor =
//            if (today == thisDate) ColorCalSelectedBorder else
            Color.Transparent
        val dateBgColor =
//            if (isActive)
//                if (today == thisDate) ColorCalTodayBg else ColorCalDateBg
//            else
            Color.Transparent
        val dateTextColor =
            if (isActive)
                if (today == thisDate) ColorCalToday else ColorCalDate
            else ColorCalDateDis

        val animatedTextColor by animateColorAsState(dateTextColor, animationSpec = tween())

        // 총 거리
        dailyRecord?.let {
            Row(
                modifier = Modifier.align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    ImageBitmap.imageResource(R.drawable.ic_swimming2),
                    contentDescription = "수영 거리",
                    modifier = Modifier.size(7.5.dp),
                    alpha = if (isActive) 1f else 0.7f
                )
                Spacer(Modifier.width(1.dp))
                Text(
                    text = it.totalDistance!!,
                    color = Color.Gray.copy(alpha = if (isActive) 1f else 0.7f),
                    fontFamily = notoSansKr,
                    fontSize = 7.5.dp.toSp,
                    lineHeight = 7.5.dp.toSp
                )
            }
        }

        // msms 아이콘 테스트
        val rId = listOf(
            R.drawable.ic_calorie,
            R.drawable.ic_calorie2,
            R.drawable.ic_calorie3,
            R.drawable.ic_calorie4,
            R.drawable.ic_calorie5,
            R.drawable.ic_calorie6,
            R.drawable.ic_calorie7,
            R.drawable.ic_calorie8,
            R.drawable.ic_calorie9,
            R.drawable.ic_calorie10,
            R.drawable.ic_calorie11,
            R.drawable.ic_calorie12,
        )[viewModel.testState.value]

        // 총 칼로리 소모
        dailyRecord?.let {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    ImageBitmap.imageResource(rId),
                    contentDescription = "칼로리",
                    modifier = Modifier.size(7.5.dp),
                    alpha = if (isActive) 1f else 0.7f
                )
                Spacer(Modifier.width(1.dp))
                Text(
                    text = "${it.totalEnergyBurned!!.toFloat().roundToInt()}",
                    color = Color.Gray.copy(alpha = if (isActive) 1f else 0.7f),
                    fontFamily = notoSansKr,
                    fontSize = 7.5.dp.toSp,
                    lineHeight = 7.5.dp.toSp
                )
            }
        }

        // 날짜 박스
        Row(
            modifier = Modifier
                .offset(y = (-0.5).dp)
                .align(Alignment.Center)
                .border(1.dp, dateBorderColor, RoundedCornerShape(5.dp))
                .background(dateBgColor, RoundedCornerShape(5.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (calendarMode == CalendarUiState.WEEK_MODE && day == "1")
                Text(
                    modifier = Modifier
                        .wrapContentSize(),
                    text = "${month.month.value}.",
                    fontSize = 8.sp,
                    lineHeight = 8.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = notoSansKr,
                    color = animatedTextColor
                )

            Text(
                modifier = Modifier
                    .wrapContentSize(),
                text = day,
                fontSize = 10.sp,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center,
                fontFamily = notoSansKr,
                color = animatedTextColor
            )
        }

        Column(
            modifier = Modifier
                .offset(y = (-0.5).dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val colorCrawl = if (isActive) ColorCrawl else ColorCrawlSecondary
            val colorBackStroke = if (isActive) ColorBackStroke else ColorBackStrokeSecondary
            val colorBreastStroke =
                if (isActive) ColorBreastStroke else ColorBreastStrokeSecondary
            val colorButterfly = if (isActive) ColorButterfly else ColorButterflySecondary
            val colorKickBoard = if (isActive) ColorKickBoard else ColorKickBoardSecondary
            val colorMixStart = if (isActive) ColorMixStart else ColorMixStartSecondary
            val colorMixEnd = if (isActive) ColorMixEnd else ColorMixEndSecondary

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

            val brushList by remember(isActive) {
                derivedStateOf {
                    dailyRecord?.let { record ->
                        // 거리 정보를 리스트로 변환
                        if (record.totalDistance == "0") {
                            return@let emptyList<Brush>()
                        }

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
                    } ?: emptyList()

                }
            }

            if (brushList.isNotEmpty()) {
                IconWithPolygon(
                    painterResource(id = R.drawable.ic_pearl2),
                    brushList,
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
    viewModel: CalendarViewModel,
    height: Dp,
    weekSpacing: Dp,
    contentsBg: Color
) {
    val calendarMode by viewModel.calendarUiState
    val currentMonth by viewModel.currentMonth
    val currentMonthTotal by viewModel.currentMonthTotal.collectAsState()
    val pattern = stringResource(R.string.calendar_date_format_year_month)
    val monthFormatter = DateTimeFormatter.ofPattern(pattern)

    val coroutineScope = rememberCoroutineScope()
    val animatedProgress = remember { Animatable(0f) }
    var isMonthModeUi by remember { mutableStateOf(true) }

    LaunchedEffect(calendarMode) {
        isMonthModeUi = when (calendarMode) {
            CalendarUiState.MONTH_MODE, CalendarUiState.TO_MONTH -> true
            CalendarUiState.TO_WEEK, CalendarUiState.WEEK_MODE -> false
        }

        val targetProgress = when (calendarMode) {
            CalendarUiState.MONTH_MODE, CalendarUiState.TO_MONTH -> 0f
            CalendarUiState.TO_WEEK, CalendarUiState.WEEK_MODE -> 1f
        }

        coroutineScope.launch {
            animatedProgress.animateTo(targetValue = targetProgress, tween(500))
        }
    }

    Column(
        Modifier
            .height(height)
            // msms 아이콘 테스트
            .clickable {
                if (viewModel.testState.value == 11) viewModel.testState.value =
                    0 else viewModel.testState.value += 1
            },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .graphicsLayer {
                    val scale = 1f - (animatedProgress.value * 0.1f)
                    scaleX = scale
                    scaleY = scale
                }
                .wrapContentWidth()
                .animateContentSize(tween(500))
        ) {
            // 년, 월
            val rowModifier =
                if (isMonthModeUi) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()
            Row(
                rowModifier,
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
                    textAlign = TextAlign.Center,
                    color = ColorTextDefault
                )

                if (isMonthModeUi) {
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
            }
        }

        // 요일 헤더
        Row(
            modifier = Modifier
                .padding(start = 5.dp, end = 5.dp, top = 5.dp)
                .background(contentsBg, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(weekSpacing)
        ) {
            Text(
                text = stringResource(R.string.calendar_label_sunday),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = Color.Red
            )

            listOf(
                stringResource(R.string.calendar_label_monday),
                stringResource(R.string.calendar_label_tuesday),
                stringResource(R.string.calendar_label_wednesday),
                stringResource(R.string.calendar_label_thursday),
                stringResource(R.string.calendar_label_friday)
            ).forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = ColorTextDefault
                )
            }

            Text(
                text = stringResource(R.string.calendar_label_saturday),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = Color.Blue
            )
        }
    }
}

@Composable
fun CalendarDetailView(
    modifier: Modifier,
    viewModel: CalendarViewModel,
    resizeBar: @Composable (Modifier) -> Unit
) {
    var calendarMode by viewModel.calendarUiState

    Box(modifier = modifier.clipToBounds()) {
        AnimatedContent(
            targetState = calendarMode in setOf(
                CalendarUiState.WEEK_MODE,
                CalendarUiState.TO_WEEK
            ),
            label = "CalendarDetailView",
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { targetState ->

            if (calendarMode == CalendarUiState.MONTH_MODE) {
                resizeBar(Modifier)
            }

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
            val scrollableState = rememberScrollState()
            val columnModifier = remember(calendarMode) {
                Modifier
                    .padding(top = 15.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top, calendarMode != CalendarUiState.WEEK_MODE)
                    .padding(horizontal = 10.dp)
                    .then(
                        if (calendarMode == CalendarUiState.WEEK_MODE) {
                            Modifier.verticalScroll(scrollableState)
                        } else {
                            Modifier
                        }
                    )
            }

            LaunchedEffect(detailRecords) {
                viewModel.calculateTotalDetailRecord()
            }

            Column(
                columnModifier,
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
    onExpandClick: () -> Unit = {},
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
            color = ColorTextDefault
        )

        Button(
            modifier = Modifier.size(30.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(),
            onClick = onExpandClick
        ) {
            Image(
                ImageBitmap.imageResource(R.drawable.ic_detail),
                contentDescription = "상세 보기",
                Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
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
            color = ColorTextDefault
        )

        Text(
            text = detailRecordWithHR.detailRecord.startTime.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
            style = MaterialTheme.typography.bodySmall,
            color = ColorTextDefault
        )

        Box(modifier = Modifier.size(70.dp, 30.dp)) {
            // 수정버튼
            Button(
                modifier = Modifier.size(30.dp).align(Alignment.CenterStart),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(),
                onClick = onModifyClick
            ) {
                Image(
                    ImageBitmap.imageResource(R.drawable.ic_pencil),
                    contentDescription = "영법 수정",
                    Modifier
                        .size(24.dp)
                        .padding(3.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            // 닫기버튼
            Button(
                modifier = Modifier.size(30.dp).align(Alignment.CenterEnd),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(),
                onClick = onCloseClick
            ) {
                Image(
                    ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }

    DetailDataView(detailRecordWithHR, true)
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
                contentDescription = "",
                modifier = Modifier
                    .size(iconSize)
                    .offset(offsetX.toDp(), offsetY.toDp())
                    .focusable(false)
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
                contentDescription = "grap",
                modifier = Modifier
                    .size(iconSize)
                    .offset(offsetX.toDp(), offsetY.toDp())
                    .focusable(false)
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
//
//    )}
}

class PreviewViewmodel() : CalendarViewModel {
    override val testState: MutableState<Int>
        get() {
            TODO()
        }
    override val uiState: MutableState<UiState>
        get() = TODO("Not yet implemented")
    override val calendarUiState: MutableState<CalendarUiState>
        get() = TODO("Not yet implemented")
    override val popupUiState = mutableStateOf(PopupUiState.NONE)
    override val healthPermissions: Set<String>
        get() = TODO("Not yet implemented")
    override val hasAllPermissions: MutableState<Boolean>
        get() = TODO("Not yet implemented")
    override val currentMonth: MutableState<LocalDate>
        get() = TODO("Not yet implemented")
    override val currentWeek: MutableState<LocalDate>
        get() = TODO("Not yet implemented")
    override val currentMonthTotal: StateFlow<DailyRecord>
        get() = TODO("Not yet implemented")
    override val dailyRecords: StateFlow<MutableMap<ZonedDateTime, DailyRecord>>
        get() = TODO("Not yet implemented")
    override val currentDetailRecords: StateFlow<List<DetailRecordWithHR>>
        get() = TODO("Not yet implemented")
    override val currentModifyRecord: StateFlow<DetailRecord?>
        get() = TODO("Not yet implemented")
    override val newRecords: StateFlow<MutableMap<String, DetailRecord>>
        get() = TODO("Not yet implemented")
    override val totalDetailRecordWithHR: StateFlow<DetailRecordWithHR?>
        get() = TODO("Not yet implemented")

    override fun initSwimmingData(onSyncComplete: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun updateDailyRecords(month: LocalDate) {
        TODO("Not yet implemented")
    }

    override fun findDetailRecord(date: Instant) {
        TODO("Not yet implemented")
    }

    override fun calculateTotalDetailRecord(detailRecords: List<DetailRecordWithHR>) {
        TODO("Not yet implemented")
    }

    override fun checkAndShowNewRecordPopup() {
        TODO("Not yet implemented")
    }

    override fun resetDetailRecord() {
        TODO("Not yet implemented")
    }

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

    override fun setModifyRecord(record: DetailRecord?) {
        _currentModifyRecord.value = record
    }

    override fun modifyDetailRecord(record: DetailRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun removeNewRecord(id: String) {
        TODO("Not yet implemented")
    }

    override fun checkPermissions(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setChangeToken(token: String?) {
        TODO("Not yet implemented")
    }

    override fun testNewSessionPopup() {
        TODO("Not yet implemented")
    }
}

@Composable
fun Duration.toCustomTimeString(): String {
    LocalContext.current
    val hours = this.inWholeHours
    val minutes = this.inWholeMinutes % 60
    val seconds = this.inWholeSeconds % 60

    val parts = mutableListOf<String>()
    if (hours > 0) parts.add("${hours}h")
    if (minutes > 0) parts.add("${minutes}m")
    if (seconds > 0) parts.add("${seconds}s")

    return if (parts.isNotEmpty()) parts.joinToString(" ") else stringResource(R.string.calendar_label_no_record)
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