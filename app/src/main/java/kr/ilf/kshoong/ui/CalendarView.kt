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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kr.ilf.kshoong.ui.theme.ColorCalendarDateBg
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgEnd
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgStart
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBorder
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarView(
    viewModel: SwimmingViewModel,
    navController: NavHostController,
    onDateClick: (String) -> Unit
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var currentDate by remember { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }
    val pagerState = rememberPagerState(0, pageCount = { 12 }) // 12달 간의 달력 제공

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        reverseLayout = true
    ) { pager ->
        val month = currentMonth.minusMonths(pager.toLong())
        MonthView(month, currentMonth, currentDate, onDateClick) { newMonth ->
            currentMonth = newMonth

        }
    }
}

@Composable
fun MonthView(
    month: LocalDate,
    currentMonth: LocalDate,
    currentDate: String,
    onDateClick: (String) -> Unit,
    onMonthChange: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.withDayOfMonth(1)
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7) // 0: Sunday, 6: Saturda
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월")

    Column(
        modifier = Modifier
            .width(screenWidth)
            .background(Color.White)
    ) {
        Text(
            text = month.format(monthFormatter),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
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
                for (day in 0..6) {
                    if (week == 0 && day < firstDayOfWeek) {
                        // 이전 달 날짜 표시
                        val prevDay = daysInPrevMonth - firstDayOfWeek + day + 1

                        DayView(Modifier
                            .alpha(0.7f)
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
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onDateClick("") }
                            )
                            .padding(5.dp), prevDay.toString(), onDateClick)
                    } else if (dayCounter > daysInMonth) {
                        // 다음 달 날짜 표시
                        DayView(
                            Modifier
                                .weight(1f)
                                .alpha(0.7f)
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
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onDateClick("") }
                                )
                                .padding(5.dp),
                            (dayCounter - daysInMonth).toString(),
                            onDateClick
                        )

                        dayCounter++
                    } else {
                        // 이번 달 날짜 표시
                        if (week > 0 || day >= firstDayOfWeek) {
                            val sameDate = dayCounter.toString() == currentDate
                            val sameMonth = month.month == currentMonth.month
                            var borderColor = Color.Transparent
                            if (sameDate && sameMonth) {
                                borderColor = ColorCalendarItemBorder
                            }

                            DayView(Modifier
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
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onDateClick("") }
                                )
                                .border(
                                    1.5.dp,
                                    borderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(5.dp), dayCounter.toString(), onDateClick)

                            dayCounter++
                        } else {
                            // 빈 공간
                            DayView(Modifier
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
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onDateClick("") }
                                )
                                .padding(5.dp), "", { })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayView(modifier: Modifier, day: String, onDateClick: (String) -> Unit) {
    Box(modifier = modifier) {
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

    }
}