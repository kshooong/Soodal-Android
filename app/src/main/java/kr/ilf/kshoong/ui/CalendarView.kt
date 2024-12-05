package kr.ilf.kshoong.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun CalendarView(viewModel: SwimmingViewModel, onClickDate: (String) -> Unit) {
    val calendar = remember { Calendar.getInstance() }
    val (currentYear, setCurrentYear) = remember { mutableIntStateOf(calendar[Calendar.YEAR]) }
    val (currentMonth, setCurrentMonth) = remember { mutableIntStateOf(calendar[Calendar.MONTH] + 1) }
    val (currentDate, setCurrentDate) = remember { mutableIntStateOf(calendar[Calendar.DATE]) }


}

@Composable
fun CalendarView() {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val pagerState = rememberPagerState(0, pageCount = { 12 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        reverseLayout = true
    ) { pager ->
        val month = currentMonth.minusMonths(pager.toLong())
        MonthView(month) { newMonth ->
            currentMonth = newMonth

        }
    }
}

@Composable
fun MonthView(month: LocalDate, onMonthChange: (LocalDate) -> Unit) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.withDayOfMonth(1)
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7) // 0: Sunday, 6: Saturday
    val daysInPreviousMonth = firstDayOfWeek
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월")

    Column(
        modifier = Modifier
            .width(screenWidth)
            .padding(16.dp)
            .background(Color.LightGray)
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
            Row(modifier = Modifier.fillMaxWidth()) {
                for (day in 0..6) {
                    if (week == 0 && day < daysInPreviousMonth) {
                        // 이전 달 날짜 표시
                        val prevDay = daysInPrevMonth - daysInPreviousMonth + day + 1
                        Text(
                            text = prevDay.toString(),
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.LightGray)
                                .padding(8.dp),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    } else if (dayCounter > daysInMonth) {
                        // 다음 달 날짜 표시
                        Text(
                            text = (dayCounter - daysInMonth).toString(),
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.LightGray)
                                .padding(8.dp),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        dayCounter++
                    } else {
                        // 이번 달 날짜 표시
                        if (week > 0 || day >= firstDayOfWeek) {
                            Text(
                                text = dayCounter.toString(),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onMonthChange(month)
                                    }
                                    .background(Color.White)
                                    .padding(8.dp),
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            dayCounter++
                        } else {
                            // 빈 공간
                            Text(
                                text = "",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCalendar() {
    MaterialTheme {
        Surface {
            CalendarView()
        }
    }
}