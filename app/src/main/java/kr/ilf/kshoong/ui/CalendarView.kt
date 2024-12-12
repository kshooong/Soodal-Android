package kr.ilf.kshoong.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.ilf.kshoong.ui.theme.ColorCalendarDateBg
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgEnd
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgStart
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBorder
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun CalendarView(
    viewModel: SwimmingViewModel,
    navController: NavHostController,
    onDateClick: (Instant?) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val today by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val currentDateStr = remember { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }
    val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
    val pagerState = rememberPagerState(0, pageCount = { 12 }) // 12달 간의 달력 제공

    Text(
        text = currentMonth.format(monthFormatter),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(10.dp),
        textAlign = TextAlign.Center
    )

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        reverseLayout = true
    ) { pager ->
        val month = today.minusMonths(pager.toLong())

        LaunchedEffect(pagerState.currentPage) {
            // 현재 페이지가 변경될 때마다 실행할 코드
            currentMonth = today.minusMonths(pagerState.currentPage.toLong())
        }

        MonthView(month, currentMonth, currentDateStr, onDateClick) { action ->
            CoroutineScope(Dispatchers.Main).launch {
                withContext(coroutineScope.coroutineContext) {
                    val target = if (action == "prev") {
                        if (pagerState.currentPage != 12)
                            pagerState.currentPage + 1
                        else pagerState.currentPage
                    } else {
                        if (pagerState.currentPage != 0)
                            pagerState.currentPage - 1
                        else pagerState.currentPage
                    }

                    pagerState.animateScrollToPage(target)
                }
            }
        }
    }
}

@Composable
fun MonthView(
    month: LocalDate,
    currentMonth: LocalDate,
    currentDate: MutableState<String>,
    onDateClick: (Instant?) -> Unit,
    onMonthChange: (String) -> Unit
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
            .background(Color.White)
    ) {
        // 요일 헤더
        Row {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

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
                    .height(80.dp)
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
                            month = month.minusMonths(1L),
                            day = prevDay.toString(),
                            currentDate = currentDate
                        ) { instant ->
                            onDateClick(instant)
                            onMonthChange("prev")
                        }

                    } else if (dayCounter > daysInMonth) {
                        // 다음 달 날짜 표시
                        DayView(
                            modifier = Modifier
                                .alpha(0.5f)
                                .then(dayViewModifier),
                            month = month.plusMonths(1L),
                            day = (dayCounter - daysInMonth).toString(),
                            currentDate = currentDate,
                        ) { instant ->
                            onDateClick(instant)
                            onMonthChange("next")
                        }

                        dayCounter++
                    } else {
                        // 이번 달 날짜 표시
                        if (week > 0 || day >= firstDayOfWeek) {
                            val sameDate = dayCounter.toString() == currentDate.value
                            val sameMonth = month.month == currentMonth.month
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
                                month = month,
                                day = dayCounter.toString(),
                                currentDate = currentDate,
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
    month: LocalDate,
    day: String,
    currentDate: MutableState<String>,
    onDateClick: (Instant?) -> Unit
) {
    Box(modifier = modifier
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                currentDate.value = day

                onDateClick(
                    month
                        .withDayOfMonth(day.toInt())
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                )
            }
        )
        .padding(5.dp)) {
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            // 내용
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(15.dp)
                .border(1.dp, ColorCalendarItemBorder, RoundedCornerShape(5.dp))
                .background(ColorCalendarDateBg, RoundedCornerShape(5.dp))
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
fun CalendarDetailView(viewModel: SwimmingViewModel, currentDate: Instant) {
    Surface(
        Modifier
            .fillMaxSize()
            .background(Color.Cyan)
    ) {
        val detailRecord by viewModel.currentDetailRecord.collectAsState()
        detailRecord.forEach {
            Text(text = it?.distance.toString() ?: "기록 없음")
        }
    }
}
