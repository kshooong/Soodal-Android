package kr.ilf.kshoong.ui

import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kr.ilf.kshoong.MainActivity
import kr.ilf.kshoong.data.SwimData
import kr.ilf.kshoong.ui.theme.ColorCalendarDateBg
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgEnd
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgStart
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBorder
import kr.ilf.kshoong.ui.theme.ColorCalendarOnDateBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private fun convertDPtoPX(context: Context, dp: Int): Int {
    val density = context.resources.displayMetrics.density
    return (dp.toFloat() * density).roundToInt()
}

@Composable
fun SwimCalendarView4(data: HashMap<String, SwimData>) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd-E", Locale.getDefault()) }
    val today = remember { Calendar.getInstance() }
    val todayStr = remember { dateFormat.format(today.time) }

    val (currentYear, setCurrentYear) = remember { mutableIntStateOf(today[Calendar.YEAR]) }
    val (currentMonth, setCurrentMonth) = remember { mutableIntStateOf(today[Calendar.MONTH]) }

    val lazyDataList = remember {
        val weekList = mutableListOf<List<SwimData>>()
        val listCalendar = Calendar.getInstance()
        val dayOffset = 7 - listCalendar[Calendar.DAY_OF_WEEK]
        listCalendar.add(Calendar.DATE, dayOffset)

        for (week in 0 until 150) {
            val dateList = mutableListOf<SwimData>()

            for (day in 0 until 7) {
                val date = dateFormat.format(listCalendar.time)
                val swimData = data[date] ?: SwimData(date, 0, 0, 0, 0, 0)
                dateList.add(swimData)
                listCalendar.add(Calendar.DATE, -1)
            }

            weekList.add(dateList)
        }

        weekList
    }


    val listState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val lastVisibleItemIndex by remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index } }

    var centerItemIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(listState.isScrollInProgress) {
        Log.d("listState.isScrollInProgress = ", listState.isScrollInProgress.toString())
        if (!listState.isScrollInProgress) {
            centerItemIndex = snapshotFlow {
                Log.d("listState.isScrollInProgress2 = ", listState.isScrollInProgress.toString())
                calculateCenterDate(firstVisibleItemIndex, lastVisibleItemIndex)
            }.first()
        }
    }

    val context = LocalContext.current

    LaunchedEffect(centerItemIndex) {
        snapshotFlow {
            Log.d("snapshotFlow", listState.isScrollInProgress.toString())
            listState.isScrollInProgress
        }
            .distinctUntilChanged()
            .filter { !it } // isScrollInProgress가 false일 때만 필터링ㅠ
            .collect {
                snapToNearestItem(context, listState)
            }
    }



    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .background(Color.White),
        reverseLayout = true
    ) {
        val onCenterDateChanged = { year: Int, month: Int ->
            setCurrentYear(year)
            setCurrentMonth(month)
        }

        itemsIndexed(items = lazyDataList) { index, dateList ->
            WeekRow(
                dataIndex = index,
                dateList = dateList,
                listState = listState,
                todayStr = todayStr,
                dateFormat = dateFormat,
                currentYear = currentYear,
                currentMonth = currentMonth,
                centerItemIndex = centerItemIndex,
                onCenterDateChanged = onCenterDateChanged,
                onClickDate = { year, month ->
                    setCurrentYear(year)
                    setCurrentMonth(month)
                }
            )
        }
    }
}

// 중앙 날짜 계산 함수
private fun calculateCenterDate(
    firstVisibleItemIndex: Int,
    lastVisibleItemIndex: Int?
): Int {
    val centerItemIndex = if (lastVisibleItemIndex != null) {
        ((firstVisibleItemIndex.plus(lastVisibleItemIndex)).div(2))
    } else {
        -1
    }
    return centerItemIndex
}

// 아이템 스냅 함수
private suspend fun snapToNearestItem(context: Context, listState: LazyListState) {
    val dp40ToPx = convertDPtoPX(context, 40)
    val targetIndex = if (listState.firstVisibleItemScrollOffset < dp40ToPx) {
        listState.firstVisibleItemIndex
    } else {
        listState.firstVisibleItemIndex + 1
    }
    listState.animateScrollToItem(targetIndex)
}

// WeekRow Composable 함수
@Composable
private fun WeekRow(
    dataIndex: Int,
    dateList: List<SwimData>,
    listState: LazyListState,
    todayStr: String,
    dateFormat: SimpleDateFormat,
    currentYear: Int,
    currentMonth: Int,
    centerItemIndex: Int?,
    onCenterDateChanged: (Int, Int) -> Unit,
    onClickDate: (Int, Int) -> Unit
) {
    if (centerItemIndex == dataIndex)
        LaunchedEffect(centerItemIndex) {
            snapshotFlow { listState.isScrollInProgress }
                .distinctUntilChanged()
                .filter { !it }
                .collect {
                    dateList.first().date.split("-").let {
                        it[1].toInt().let { month ->
                            if (month != currentMonth) {
                                onCenterDateChanged(it[0].toInt(), month)
                            }
                        }
                    }
                }

        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 2.5.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        dateList.reversed().forEach { data ->
            DayItem(
                modifier = Modifier.weight(1f),
                data = data,
                todayStr = todayStr,
                currentYear = currentYear,
                currentMonth = currentMonth,
                onClickDate = onClickDate
            )
        }
    }
}

@Composable
private fun DayItem(
    modifier: Modifier,
    data: SwimData,
    todayStr: String,
    currentYear: Int,
    currentMonth: Int,
    onClickDate: (Int, Int) -> Unit
) {
    val dayInfo = data.date.split("-") // YYYY-MM-DD-요일
    val isCurrentMonth = currentYear == dayInfo[0].toInt() && currentMonth == dayInfo[1].toInt()

    val alpha: Float
    val borderColor: Color
    val dateBgColor: Color

    if (isCurrentMonth) {
        alpha = 1f

        if (todayStr == data.date) {
            borderColor = ColorCalendarItemBorder
            dateBgColor = ColorCalendarOnDateBg
        } else {
            borderColor = Color.Transparent
            dateBgColor = ColorCalendarDateBg
        }
    } else {
        alpha = 0.5f
        borderColor = Color.Transparent
        dateBgColor = Color.Transparent
    }

    Box(
        modifier
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClickDate(dayInfo[0].toInt(), dayInfo[1].toInt()) }
            )
            .padding(5.dp),
    ) {

        // 그래프 컬럼 (필요에 따라 추가)
        Column {
            // ...
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
                text = dayInfo[2], // 날짜만 표시
                fontSize = 10.sp,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun SwimCalendarView4Preview() {
    SwimCalendarView4(MainActivity.data)
}