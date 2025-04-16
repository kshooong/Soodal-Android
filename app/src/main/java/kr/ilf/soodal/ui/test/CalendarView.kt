package kr.ilf.soodal.ui.test

import android.content.res.Resources
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kr.ilf.soodal.ui.distributeDistance
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
import kr.ilf.soodal.ui.theme.notoSansKr
import kr.ilf.soodal.ui.toDp
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
    contentsBg: Color,
    viewModel: SwimmingViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var calendarMode by viewModel.calendarUiState

    val today by remember { mutableStateOf(LocalDate.now()) }
    val todayWeek = remember { today.minusDays(today.dayOfWeek.value - 3L) } // 오늘이 있는 주의 수요일
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
        Log.d("weekPagerState", "currentPage: ${weekPagerState.currentPage}")
    }

    LaunchedEffect(monthPagerState.currentPage) {
        if ((calendarMode == CalendarUiState.WEEK_MODE)) {
            currentWeek = todayWeek
                .minusWeeks(monthPagerState.currentPage.toLong())
            val monthDifference = calculateMonthDifference(todayWeek, currentWeek)
            currentMonth = today.withDayOfMonth(1).minusMonths(monthDifference.toLong())
        } else {
            currentMonth = today.withDayOfMonth(1).minusMonths(monthPagerState.currentPage.toLong())

            // 선택된 날이 이번 달에 있으면 그 날짜가 속한 주를 currentWeek으로 설정
            if (selectedMonth.value.year == currentMonth.year && selectedMonth.value.month == currentMonth.month) {
                val selectedDate = selectedMonth.value.withDayOfMonth(selectedDateStr.value.toInt())
                val selectedWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value - 3L)
                currentWeek = selectedWeek
            } else {
                // 바뀐 월의 첫 번째 주를 currentWeek으로 설정
                val tempWeek = currentMonth.minusDays(currentMonth.dayOfWeek.value - 3L)
                currentWeek = if (tempWeek.dayOfMonth > 4) tempWeek.plusWeeks(1L) else tempWeek
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
        Button(onClick = {
            if (calendarMode == CalendarUiState.WEEK_MODE) {
                calendarMode = CalendarUiState.TO_MONTH
            } else if (calendarMode == CalendarUiState.MONTH_MODE) {
                calendarMode = CalendarUiState.TO_WEEK
            }
        }) { Text("Mode") }

        HorizontalPager(
            state = if (calendarMode == CalendarUiState.WEEK_MODE) weekPagerState else monthPagerState,
            userScrollEnabled = calendarMode == CalendarUiState.WEEK_MODE || calendarMode == CalendarUiState.MONTH_MODE,
            modifier = Modifier
                .padding(horizontal = 5.dp)
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
                val isCurrentWeek = week == currentWeek
                val daysInMonth = week.lengthOfMonth()
                val firstDayOfMonth = week.withDayOfMonth(1)
                val firstDayOfWeek =
                    (firstDayOfMonth.dayOfWeek.value % 7) // 0: Sunday, 6: Saturday
                val prevMonth = week.minusMonths(1)
                val daysInPrevMonth = prevMonth.lengthOfMonth()
                val weekOfMonth = getWeekOfMonth(week) - 1

                val dayCounter = (1 + weekOfMonth * 7 - firstDayOfWeek).coerceAtLeast(1)

                WeekView(
                    weekOfMonth,
                    firstDayOfWeek,
                    daysInPrevMonth,
                    viewModel,
                    week,
                    today,
                    { Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show() },
                    dayCounter,
                    daysInMonth,
                    selectedDateStr,
                    selectedMonth,
                    isCurrentWeek,
                    {}
                )
            } else {
                val month = today.minusMonths(it.toLong())

                MonthView(
                    viewModel,
                    month,
                    selectedMonth,
                    selectedDateStr,
                    today
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

                            currentWeek = clickedDate.minusDays(clickedDate.dayOfWeek.value - 3L)

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

                            currentWeek = clickedDate.minusDays(clickedDate.dayOfWeek.value - 3L)
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

    val offset = firstDayOfWeek.value

    return (dayOfMonth + offset - 1) / 7 + 1
}

@Composable
fun MonthView(
    viewModel: SwimmingViewModel,
    month: LocalDate,
    selectedMonth: MutableState<LocalDate>,
    selectedDateStr: MutableState<String>,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.withDayOfMonth(1)
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7) // 0: Sunday, 6: Saturda
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(Color.Transparent)
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

                    currentWeek.dayOfMonth < 4 && week == (getWeekOfMonth(selectedDate) - 1)

                }

            WeekView(
                week,
                firstDayOfWeek,
                daysInPrevMonth,
                viewModel,
                month,
                today,
                onDateClick,
                dayCounter,
                daysInMonth,
                selectedDateStr,
                selectedMonth,
                isCurrentWeek,
                updateDayCounter = { dayCounter = it }
            )
        }
    }
}

@Composable
private fun WeekView(
    week: Int,
    firstDayOfWeek: Int,
    daysInPrevMonth: Int,
    viewModel: SwimmingViewModel,
    month: LocalDate,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    dayCounterStart: Int,
    daysInMonth: Int,
    selectedDateStr: MutableState<String>,
    selectedMonth: MutableState<LocalDate>,
    isCurrentWeek: Boolean,
    updateDayCounter: (Int) -> Unit
) {
    var dayCounter = dayCounterStart
    val calendarUiState by viewModel.calendarUiState

    var height by remember { mutableStateOf(if (isCurrentWeek || calendarUiState == CalendarUiState.MONTH_MODE || calendarUiState == CalendarUiState.WEEK_MODE) 70.dp else 0.dp) }
    var paddingV by remember { mutableStateOf(if (isCurrentWeek || calendarUiState == CalendarUiState.MONTH_MODE || calendarUiState == CalendarUiState.WEEK_MODE) 2.5.dp else 0.dp) }
    var alpha by remember { mutableFloatStateOf(if (isCurrentWeek || calendarUiState == CalendarUiState.MONTH_MODE || calendarUiState == CalendarUiState.WEEK_MODE) 1f else 0f) }

    LaunchedEffect(calendarUiState) {
        when (calendarUiState) {
            CalendarUiState.MONTH_MODE, CalendarUiState.TO_MONTH, CalendarUiState.WEEK_MODE -> {
                height = 70.dp
                paddingV = 2.5.dp
                alpha = 1f
            }

            CalendarUiState.TO_WEEK -> if (isCurrentWeek) {
                height = 70.dp
                paddingV = 2.5.dp
                alpha = 1f
            } else {
                height = 0.dp
                paddingV = 0.dp
                alpha = 0f
            }
        }
    }

    val animatedPadding by animateDpAsState(paddingV, animationSpec = tween())
    val animatedAlpha by animateFloatAsState(alpha, animationSpec = tween())
    val animatedHeight by animateDpAsState(
        height,
        animationSpec = tween(),
        finishedListener = { _ ->
            viewModel.animationCount.intValue += 1
        })

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                if (!isCurrentWeek) Color.Transparent else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 7.5.dp, vertical = animatedPadding),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val dayViewModifier = Modifier
            .weight(1f)
            .height(animatedHeight)
            .alpha(animatedAlpha)

        for (day in 0..6) {
            if (week == 0 && day < firstDayOfWeek) {
                // 이전 달 날짜 표시
                val prevDay = daysInPrevMonth - firstDayOfWeek + day + 1

                DayView(
                    modifier = dayViewModifier.background(
                        ColorCalendarItemBgDis, shape = RoundedCornerShape(8.dp)
                    ),
                    viewModel = viewModel,
                    month = month.minusMonths(1L).withDayOfMonth(prevDay),
                    day = prevDay.toString(),
                    today = today,
                    isThisMonth = false,
                    onDateClick = onDateClick
                )

            } else if (dayCounter > daysInMonth) {
                // 다음 달 날짜 표시
                DayView(
                    modifier = dayViewModifier.background(
                        ColorCalendarItemBgDis, shape = RoundedCornerShape(8.dp)
                    ),
                    viewModel = viewModel,
                    month = month.plusMonths(1L).withDayOfMonth(dayCounter - daysInMonth),
                    day = (dayCounter - daysInMonth).toString(),
                    today = today,
                    isThisMonth = false,
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
                            .background(
                                bgColor, shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.5.dp,
                                borderColor,
                                RoundedCornerShape(8.dp)
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
fun DayView(
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
        .padding(5.dp))
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

        // 날짜 박스
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(15.dp)
                .border(1.dp, dateBorderColor, RoundedCornerShape(5.dp))
                .background(Color.Transparent, RoundedCornerShape(5.dp))
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
                color = dateTextColor
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

            val brushList = remember {
                derivedStateOf {
                    dailyRecord.value?.let { record ->
                        // 거리 정보를 리스트로 변환
                        val distances = mapOf(
                            SolidColor(colorCrawl) to record.crawl,
                            SolidColor(colorBackStroke) to record.backStroke,
                            SolidColor(colorBreastStroke) to record.breastStroke,
                            SolidColor(colorButterfly) to record.butterfly,
                            SolidColor(colorKickBoard) to record.kickBoard,
                            Brush.verticalGradient(
                                Pair(0f, colorMixStart),
                                Pair(1f, colorMixEnd)
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

        val totalDistance = remember { derivedStateOf { currentMonthTotal.totalDistance ?: "0" } }
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