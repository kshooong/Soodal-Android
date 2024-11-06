package kr.ilf.kshoong.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.ilf.kshoong.ui.theme.KshoongTheme
import java.util.Calendar

@Composable
fun SwimCalendarView() {
    val calendar = remember { Calendar.getInstance() }
    val year = remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    val month = remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }

    val (daysInMonth, firstDayOfWeekInMonth, preDaysInMonth) = remember(year, month) {
        with(Calendar.getInstance()) {
            set(year.intValue, month.intValue, 1)

            Triple(
                getActualMaximum(Calendar.DAY_OF_MONTH),
                get(Calendar.DAY_OF_WEEK),
                let {
                    set(year.intValue, month.intValue - 1, 1)
                    get(Calendar.DAY_OF_WEEK)
                }
            )
        }
    }
    Column {
        for (week in 1..4) {
            Row(Modifier.fillMaxWidth().wrapContentHeight(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (dyaOfWeek in 1..7) {
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .weight(1f)
                            .height(100.dp)
                    ) { SwimCalendarItem() }
                }
            }
        }
    }
}

@Composable
private fun SwimCalendarItem() {

}

@Preview(showBackground = true)
@Composable
fun Preview() {
    SwimCalendarView()
}