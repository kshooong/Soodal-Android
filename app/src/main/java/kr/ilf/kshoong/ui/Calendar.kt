package kr.ilf.kshoong.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun SwimCalendarView(calendar: Calendar) {
    val currentYear = remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    val currentMonth = remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    val currentDate = remember { mutableIntStateOf(calendar.get(Calendar.DATE)) }
    val currentDayOfWeek = remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_WEEK)) }

    // 당월 일수 , 당월 첫날의 요일, 전월 일수
    val (daysInMonth, firstDayOfWeekInMonth, preDaysInMonth) = remember(currentYear, currentMonth) {
        with(Calendar.getInstance()) {
            set(currentYear.intValue, currentMonth.intValue, 1)

            return@with Triple(
                getActualMaximum(Calendar.DAY_OF_MONTH),
                get(Calendar.DAY_OF_WEEK),
                let {
                    set(currentYear.intValue, currentMonth.intValue - 1, 1)
                    getActualMaximum(Calendar.DAY_OF_MONTH)
                })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        for (week in 1..4) {
            Row {
                for (dayOfWeek in 1..7) {
                    var backgroundColor = Color.Gray
//                    val dateIndex = (week - 1) * 7 + dayOfWeek + currentDate.intValue - currentDayOfWeek.intValue
                    val dateIndex = (week - 1) * 7 + dayOfWeek - firstDayOfWeekInMonth + 1
                    val date = when {
                        dateIndex < 1 -> {preDaysInMonth + dateIndex}
                        dateIndex in 1..daysInMonth ->{ backgroundColor = Color.White; dateIndex}
                        else -> {dateIndex - daysInMonth }// dateIndex > daysInMonth
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .border(1.dp, Color.Black, shape = RoundedCornerShape(10.dp))
                            .background(backgroundColor, shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        SwimCalendarItem(date)
                    }
                }
            }
        }
    }
}

@Composable
private fun SwimCalendarItem(date: Int) {
    Text(modifier = Modifier.wrapContentSize(), text = date.toString(), fontSize = 20.sp)
}

@Preview
@Composable
fun SwimCalendarViewPreView() {
    SwimCalendarView(calendar = Calendar.getInstance())
}