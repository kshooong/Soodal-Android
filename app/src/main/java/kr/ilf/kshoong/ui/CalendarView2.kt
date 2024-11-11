package kr.ilf.kshoong.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.ilf.kshoong.ui.theme.ColorCalendarDateBg
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgEnd
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgStart
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBorder
import kr.ilf.kshoong.ui.theme.ColorCalendarOnDateBg

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


fun convertDPtoPX(context: Context, dp: Int): Int {
    val density = context.resources.displayMetrics.density
    return Math.round(dp.toFloat() * density)
}

@Composable
fun SwimCalendarView2() {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd-E", Locale.getDefault()) }
    val todayStr = remember { dateFormat.format(System.currentTimeMillis()) }
    val todayCalendar = remember { Calendar.getInstance() }
    val (currentYear, setCurrentYear) = remember { mutableIntStateOf(todayCalendar[Calendar.YEAR]) }
    val (currentMonth, setCurrentMonth) = remember { mutableIntStateOf(todayCalendar[Calendar.MONTH]) }

    val listState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val lastVisibleItemIndex by remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index } }
    val firstVisibleItemScrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

    val centerItemIndex by remember {
        derivedStateOf {
            if (lastVisibleItemIndex != null) {
                (firstVisibleItemIndex + lastVisibleItemIndex!!) / 2 - 1
            } else {
                -1 // 화면에 아이템이 없는 경우
            }
        }
    }

    val dp40ToPx = convertDPtoPX(LocalContext.current, 40)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && firstVisibleItemScrollOffset < dp40ToPx) {
            listState.animateScrollToItem(firstVisibleItemIndex)
        } else {
            listState.animateScrollToItem(firstVisibleItemIndex + 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(425.dp)
            .background(Color.White),
        reverseLayout = true
    ) {
        items(count = 150) { count ->
            val weekOffset = -count
            val weekCalendar = todayCalendar.clone() as Calendar
            weekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            weekCalendar.add(Calendar.WEEK_OF_YEAR, weekOffset)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 2.5.dp)
            ) {
                for (i in 0 until 7) {
                    val dateCalendar = weekCalendar.clone() as Calendar
                    dateCalendar.add(Calendar.DAY_OF_WEEK, i)

                    val dayStr = dateFormat.format(dateCalendar.time)

                    if (centerItemIndex == count && i == 3 && !listState.isScrollInProgress) {
                        setCurrentYear(dateCalendar[Calendar.YEAR])
                        setCurrentMonth(dateCalendar[Calendar.MONTH])
                    }

                    val alpha =
                        if (dateCalendar[Calendar.YEAR] == currentYear && dateCalendar[Calendar.MONTH] == currentMonth) {
                            1f
                        } else {
                            0.6f
                        }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(alpha)
                            .padding(horizontal = 2.5.dp)
                            .height(80.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        ColorCalendarItemBgStart,
                                        ColorCalendarItemBgEnd
                                    )
                                ), shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(onClick = {
                                setCurrentYear(dateCalendar[Calendar.YEAR])
                                setCurrentMonth(dateCalendar[Calendar.MONTH])
                            })
                            .padding(5.dp),
                    ) {
                        val borderColor = if (dayStr == todayStr) {
                            ColorCalendarItemBorder
                        } else {
                            Color.Transparent
                        }

                        val dateBgColor = if (dayStr == todayStr) {
                            ColorCalendarOnDateBg
                        } else {
                            ColorCalendarDateBg
                        }

                        // 그래프 컬럼
                        Column {

                        }

                        // 날짜 박스
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(15.dp)
                                .border(1.dp, borderColor, RoundedCornerShape(5.dp))
                                .background(dateBgColor, RoundedCornerShape(5.dp))
                        ) {
                            Text(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.Center),
                                text = dateCalendar.get(Calendar.DATE).toString(),
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

        }

    }
}

@Preview
@Composable
fun SwimCalendarView2Preview() {
    SwimCalendarView2()
}