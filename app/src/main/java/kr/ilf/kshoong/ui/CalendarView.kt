package kr.ilf.kshoong.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.ilf.kshoong.ui.theme.ColorCalendarDateBg
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgEnd
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgStart
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBorder
import kr.ilf.kshoong.ui.theme.ColorCalendarOnDateBg
import kr.ilf.kshoong.ui.theme.DailyGraphEnd
import kr.ilf.kshoong.ui.theme.DailyGraphStart
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import kr.ilf.kshoong.viewmodel.UiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun CalendarView(
    viewModel: SwimmingViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    val today by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val selectedMonth = remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val selectedDateStr = remember { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }
    val pagerState = rememberPagerState(0, pageCount = { 12 }) // 12달 간의 달력 제공

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }.distinctUntilChanged()
            .collect {
                if (pagerState.isScrollInProgress) {
                    viewModel.uiState.value = UiState.SCROLLING
                } else {
                    viewModel.uiState.value = UiState.COMPLETE
                }
            }
    }

    CalendarHeaderView(currentMonth)

    LaunchedEffect(pagerState.currentPage) {
        currentMonth = today.withDayOfMonth(1).minusMonths(pagerState.currentPage.toLong())
        viewModel.updateDailyRecords(currentMonth)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, shape = RoundedCornerShape(10.dp))
            .padding(vertical = 5.dp),
        key = { today.minusMonths(it.toLong()) },
        reverseLayout = true
    ) {
        val month = today.minusMonths(it.toLong())
        val context = LocalContext.current

        MonthView(viewModel, month, selectedMonth, selectedDateStr, today) { newMonth ->
            when {
                newMonth.isAfter(today) -> {
                    Toast.makeText(context, "오늘 이후는 선택할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }

                newMonth.withDayOfMonth(1).isBefore(today.withDayOfMonth(1).minusMonths(11L)) -> {
                    Toast.makeText(context, "12달보다 이전은 선택할 수 없습니다", Toast.LENGTH_SHORT).show()
                }

                newMonth.month == currentMonth.month -> {
                    selectedMonth.value = newMonth
                    selectedDateStr.value = newMonth.dayOfMonth.toString()

                    viewModel.findDetailRecord(
                        newMonth
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC)
                    )
                }

                else -> {
                    selectedMonth.value = newMonth
                    selectedDateStr.value = newMonth.dayOfMonth.toString()

                    viewModel.findDetailRecord(
                        newMonth
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC)
                    )

                    val diffMonth =
                        ChronoUnit.MONTHS.between(newMonth.withDayOfMonth(1), currentMonth).toInt()
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(coroutineScope.coroutineContext) {
                            val target = pagerState.currentPage + diffMonth

                            pagerState.animateScrollToPage(target)
                        }
                    }
                }
            }
        }
    }
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(Color.Transparent)
    ) {
        // 날짜 표시
        var dayCounter = 1

        // 주 단위로 날짜를 표시
        for (week in 0..5) { // 최대 6주까지 표시
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 2.5.dp)
            ) {
                val dayViewModifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.5.dp)
                    .height(70.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                ColorCalendarItemBgStart,
                                ColorCalendarItemBgEnd
                            )
                        ), shape = RoundedCornerShape(8.dp)
                    )

                for (day in 0..6) {
                    if (week == 0 && day < firstDayOfWeek) {
                        // 이전 달 날짜 표시
                        val prevDay = daysInPrevMonth - firstDayOfWeek + day + 1

                        DayView(
                            modifier = Modifier
                                .alpha(0.5f)
                                .then(dayViewModifier),
                            viewModel = viewModel,
                            month = month.minusMonths(1L).withDayOfMonth(prevDay),
                            day = prevDay.toString(),
                            selectedDate = selectedDateStr,
                            today = today,
                            onDateClick = onDateClick
                        )

                    } else if (dayCounter > daysInMonth) {
                        // 다음 달 날짜 표시
                        DayView(
                            modifier = Modifier
                                .alpha(0.5f)
                                .then(dayViewModifier),
                            viewModel = viewModel,
                            month = month.plusMonths(1L).withDayOfMonth(dayCounter - daysInMonth),
                            day = (dayCounter - daysInMonth).toString(),
                            selectedDate = selectedDateStr,
                            today = today,
                            onDateClick = onDateClick
                        )

                        dayCounter++
                    } else {
                        // 이번 달 날짜 표시
                        if (week > 0 || day >= firstDayOfWeek) {
                            val sameDate = dayCounter.toString() == selectedDateStr.value
                            val sameMonth = month.month == selectedMonth.value.month
                            val borderColor = if (sameDate && sameMonth) {
                                ColorCalendarItemBorder
                            } else {
                                Color.Transparent
                            }

                            DayView(
                                modifier = dayViewModifier.border(
                                    1.5.dp,
                                    borderColor,
                                    RoundedCornerShape(8.dp)
                                ),
                                viewModel = viewModel,
                                month = month.withDayOfMonth(dayCounter),
                                day = dayCounter.toString(),
                                selectedDate = selectedDateStr,
                                today = today,
                                onDateClick = onDateClick
                            )

                            dayCounter++
                        }
                    }
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
    selectedDate: MutableState<String>,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    val thisDate = month.withDayOfMonth(day.toInt())

    Box(modifier = modifier
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onDateClick(thisDate) }
        )
        .padding(5.dp)) {
        val dateBorderColor = if (today == thisDate) ColorCalendarItemBorder else Color.Transparent
        val dateBgColor = if (today == thisDate) ColorCalendarOnDateBg else ColorCalendarDateBg

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            val dailyRecords by viewModel.dailyRecords.collectAsState()
            val dailyRecord = remember {
                derivedStateOf {
                    dailyRecords[thisDate.atStartOfDay().toInstant(ZoneOffset.UTC)]
                }
            }

//            val boxWidthsTemp =
//                rememberUpdatedState(newValue = dailyRecord.value?.totalDistance?.let { totalDistance ->
//                    (0..(totalDistance.toInt().div(1000) ?: 0)).map { i ->
//                        if (totalDistance.toInt() - (i * 1000) >= 1000) 1f else (totalDistance.toInt() - (i * 1000)) / 1000f
//                    }
//                } ?: emptyList())
//
//            val boxWidths = remember { mutableStateOf<List<Float>>(emptyList()) }
//
//            LaunchedEffect(viewModel.uiState.value) {
//                if (viewModel.uiState.value == UiState.COMPLETE) {
//                    boxWidths.value = boxWidthsTemp.value
//                }
//            }

            val boxWidths = remember {
                derivedStateOf {
                    dailyRecord.value?.totalDistance?.let { totalDistance ->
                        (0..(totalDistance.toInt().div(1000) ?: 0)).map { i ->
                            if (totalDistance.toInt() - (i * 1000) >= 1000) 1f else (totalDistance.toInt() - (i * 1000)) / 1000f
                        }
                    } ?: emptyList()
                }
            }

            boxWidths.value.forEach {
                Box(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .fillMaxWidth(it)
                        .height(8.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                Pair(0f, ColorMixStart),
                                Pair(1f, ColorMixEnd)
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                        .align(Alignment.Start)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarHeaderView(
    currentMonth: LocalDate
) {
    val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
    // 년, 월
    Text(
        text = currentMonth.format(monthFormatter),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(top = 15.dp, start = 5.dp, end = 5.dp, bottom = 5.dp)
            .background(Color.White, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp),
        textAlign = TextAlign.Center
    )

    // 요일 헤더
    Row(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp, top = 5.dp)
            .background(Color.White, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "일",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            color = Color.Red
        )

        listOf("월", "화", "수", "목", "금").forEach {
            Text(
                text = it,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "토",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            color = Color.Blue
        )
    }
}

@Composable
fun CalendarDetailView(viewModel: SwimmingViewModel, currentDate: Instant) {

    Column(
        Modifier
            .padding(12.5.dp, 0.dp, 12.5.dp, 65.dp)
            .navigationBarsPadding()
            .fillMaxSize()
            .background(ColorCalendarItemBgStart, shape = RoundedCornerShape(10.dp))
            .padding(5.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val detailRecord by viewModel.currentDetailRecord.collectAsState()
        detailRecord.forEach {
            Text(text = it!!.detailRecord.distance.toString() ?: "기록 없음")
        }
    }
}