package kr.ilf.kshoong.ui

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.I
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.ilf.kshoong.ui.theme.ColorCalendarDateBg
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgEnd
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgStart
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBorder
import kr.ilf.kshoong.ui.theme.ColorCalendarOnDateBg
import java.util.Calendar

@Composable
fun SwimCalendarView() {
    val calendar = remember { Calendar.getInstance() }
    val currentYear = remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    val currentMonth = remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    val currentDate = remember { mutableIntStateOf(calendar.get(Calendar.DATE)) }
    val currentDayOfWeek = remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_WEEK)) }


    val (selectedDateIndex, setSelectedDateIndex) = remember { mutableIntStateOf(currentDate.intValue) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "${currentMonth.intValue.toString()}월",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            for (week in 1..4) {
                Row(Modifier.padding(horizontal = 5.dp, vertical = 2.5.dp)) {
                    for (dayOfWeek in 1..7) {
//                    val dateIndex = (week - 1) * 7 + dayOfWeek + currentDate.intValue - currentDayOfWeek.intValue // 이번 주부터
//                    val dateIndex = (week - 1) * 7 + dayOfWeek - firstDayOfWeekInMonth + 1 // 이번 달 1일부터
                        val dateIndex =
                            (week - 1 - 3) * 7 + dayOfWeek + currentDate.intValue - currentDayOfWeek.intValue  // 이번 주까지


                        SwimCalendarItem(
                            Modifier.weight(1f),
                            dateIndex,
                            currentDate.intValue,
                            currentMonth.intValue,
                            preDaysInMonth,
                            daysInMonth,
                            selectedDateIndex
                        ) {
                            setSelectedDateIndex(dateIndex)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwimCalendarItem(
    modifier: Modifier,
    dateIndex: Int,
    currentDate: Int,
    currentMonth: Int,
    preDaysInMonth: Int,
    daysInMonth: Int,
    selectedDateIndex: Int,
    onClick: () -> Unit = {}
) {
// 날짜 계산
    val date = when {
        dateIndex < 1 -> preDaysInMonth + dateIndex
        dateIndex in 1..daysInMonth -> dateIndex
        else -> dateIndex - daysInMonth // dateIndex > daysInMonth
    }

    val borderColor = if (selectedDateIndex == dateIndex) {
        ColorCalendarItemBorder
    } else {
        Color.Transparent
    }

    val dateBgColor = if (date == currentDate) {
        ColorCalendarOnDateBg
    } else if (dateIndex < 1 || dateIndex > daysInMonth) {
        Color.Transparent
    } else {
        ColorCalendarDateBg
    }

    Box(
        modifier = modifier
            .alpha(if (dateIndex < 1 || dateIndex > daysInMonth) 0.7f else 1f)
            .padding(horizontal = 2.5.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        ColorCalendarItemBgStart,
                        ColorCalendarItemBgEnd
                    )
                ), shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "$date", onClick = onClick
            )
            .padding(5.dp),
        contentAlignment = Alignment.Center,
    ) {

        // 그래프 컬럼
        Column {

        }

        // 날짜 박스
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(15.dp)
                .border(
                    1.dp,
                    if (date == currentDate) ColorCalendarItemBorder else Color.Transparent,
                    RoundedCornerShape(5.dp)
                )
                .background(dateBgColor, RoundedCornerShape(5.dp))
        ) {
            Text(
                modifier = Modifier
                    .alpha(if (dateIndex < 1 || dateIndex > daysInMonth) 0.4f else 1f)
                    .wrapContentSize()
                    .align(Alignment.Center),
                text = date.toString(),
                fontSize = 10.sp,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun SwimCalendarViewPreView() {
    SwimCalendarView()
}