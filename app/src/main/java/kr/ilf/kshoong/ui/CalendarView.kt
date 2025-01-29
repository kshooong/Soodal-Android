package kr.ilf.kshoong.ui

import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.ilf.kshoong.R
import kr.ilf.kshoong.database.entity.DetailRecord
import kr.ilf.kshoong.database.entity.DetailRecordWithHeartRateSample
import kr.ilf.kshoong.database.entity.HeartRateSample
import kr.ilf.kshoong.ui.theme.ColorBackStroke
import kr.ilf.kshoong.ui.theme.ColorBackStrokeSecondary
import kr.ilf.kshoong.ui.theme.ColorBreastStroke
import kr.ilf.kshoong.ui.theme.ColorBreastStrokeSecondary
import kr.ilf.kshoong.ui.theme.ColorButterfly
import kr.ilf.kshoong.ui.theme.ColorButterflySecondary
import kr.ilf.kshoong.ui.theme.ColorCalendarDate
import kr.ilf.kshoong.ui.theme.ColorCalendarDateBg
import kr.ilf.kshoong.ui.theme.ColorCalendarDateBgDis
import kr.ilf.kshoong.ui.theme.ColorCalendarDateDis
import kr.ilf.kshoong.ui.theme.ColorCalendarDetailBg
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBg
import kr.ilf.kshoong.ui.theme.ColorCalendarItemBgDis
import kr.ilf.kshoong.ui.theme.ColorCalendarOnItemBg
import kr.ilf.kshoong.ui.theme.ColorCalendarOnItemBorder
import kr.ilf.kshoong.ui.theme.ColorCalendarToday
import kr.ilf.kshoong.ui.theme.ColorCalendarTodayBg
import kr.ilf.kshoong.ui.theme.ColorCrawl
import kr.ilf.kshoong.ui.theme.ColorCrawlSecondary
import kr.ilf.kshoong.ui.theme.ColorKickBoard
import kr.ilf.kshoong.ui.theme.ColorKickBoardSecondary
import kr.ilf.kshoong.ui.theme.ColorMixEnd
import kr.ilf.kshoong.ui.theme.ColorMixEndSecondary
import kr.ilf.kshoong.ui.theme.ColorMixStart
import kr.ilf.kshoong.ui.theme.ColorMixStartSecondary
import kr.ilf.kshoong.viewmodel.PopupUiState
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import kr.ilf.kshoong.viewmodel.UiState
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.time.Duration

val selectedMonthSaver =
    mapSaver(save = { mapOf("selectedMonth" to it) },
        restore = { it["selectedMonth"] as LocalDate })


@Composable
fun CalendarView(
    modifier: Modifier,
    viewModel: SwimmingViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    val today by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val selectedMonth = rememberSaveable(stateSaver = selectedMonthSaver) {
        mutableStateOf(
            LocalDate.now().withDayOfMonth(1)
        )
    }
    val selectedDateStr =
        rememberSaveable() { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }
    val pagerState = rememberPagerState(0, pageCount = { 12 }) // 12달 간의 달력 제공

    // 최초 진입 시 DetailRecord 조회, dispose 시 데이터 초기화
    DisposableEffect(Unit) {
        val selectedInstant = selectedMonth.value.withDayOfMonth(selectedDateStr.value.toInt())
            .atStartOfDay(ZoneId.systemDefault()).toInstant()
        viewModel.findDetailRecord(selectedInstant)

        onDispose { viewModel.resetDetailRecord() }
    }

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

    LaunchedEffect(pagerState.currentPage) {
        currentMonth = today.withDayOfMonth(1).minusMonths(pagerState.currentPage.toLong())
        viewModel.updateDailyRecords(currentMonth)
    }

    Column(modifier = modifier) {
        CalendarHeaderView(currentMonth)

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

                    newMonth.withDayOfMonth(1)
                        .isBefore(today.withDayOfMonth(1).minusMonths(11L)) -> {
                        Toast.makeText(context, "12달보다 이전은 선택할 수 없습니다", Toast.LENGTH_SHORT).show()
                    }

                    newMonth.month == currentMonth.month -> {
                        selectedMonth.value = newMonth
                        selectedDateStr.value = newMonth.dayOfMonth.toString()

                        viewModel.findDetailRecord(
                            newMonth.atStartOfDay(ZoneOffset.systemDefault()).toInstant()
                        )
                    }

                    else -> {
                        selectedMonth.value = newMonth
                        selectedDateStr.value = newMonth.dayOfMonth.toString()

                        viewModel.findDetailRecord(
                            newMonth.atStartOfDay(ZoneOffset.systemDefault()).toInstant()
                        )

                        val diffMonth =
                            ChronoUnit.MONTHS.between(newMonth.withDayOfMonth(1), currentMonth)
                                .toInt()
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

                        dayCounter++
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


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            val dailyRecords by viewModel.dailyRecords.collectAsState()
            val dailyRecord = remember {
                derivedStateOf {
                    dailyRecords[thisDate.atStartOfDay().atZone(ZoneId.systemDefault())]
                }
            }

            val boxWidths = remember {
                derivedStateOf {
                    dailyRecord.value?.let { record ->
                        if (record.mixed == record.totalDistance?.toInt()) {
                            dailyRecord.value?.totalDistance?.let { totalDistance ->
                                // mixed만 있으면 전체 거리 표시
                                (0..totalDistance.toInt().div(1000)).map { i ->
                                    "mixed" to if (totalDistance.toInt() - (i * 1000) >= 1000) 1f else (totalDistance.toInt() - (i * 1000)) / 1000f
                                }
                            }
                        } else {
                            // 거리 정보를 리스트로 변환
                            val distances = listOf(
                                "crawl" to record.crawl,
                                "back" to record.backStroke,
                                "breast" to record.breastStroke,
                                "butterfly" to record.butterfly,
                                "mixed" to record.mixed,
                                "kickBoard" to record.kickBoard
                            )

                            // 가장 큰 값 4개 찾기
                            val topDistances =
                                distances.sortedByDescending { it.second }.take(4).toSet()

                            // 원래 순서 유지하면서 필터링 및 비율 계산
                            distances.filter { it in topDistances }
                                .map { (type, distance) ->
                                    type to (distance / 500f).coerceAtMost(1f)
                                }
                        }
                    } ?: emptyList()

                }
            }

            boxWidths.value.forEach { (type, widthRatio) ->
                if (widthRatio > 0) {
                    val color = if (isThisMonth) {
                        when (type) {
                            "crawl" -> SolidColor(ColorCrawl)
                            "back" -> SolidColor(ColorBackStroke)
                            "breast" -> SolidColor(ColorBreastStroke)
                            "butterfly" -> SolidColor(ColorButterfly)
                            "kickBoard" -> SolidColor(ColorKickBoard)
                            else -> Brush.verticalGradient(
                                Pair(0f, ColorMixStart),
                                Pair(1f, ColorMixEnd)
                            )
                        }
                    } else {
                        when (type) {
                            "crawl" -> SolidColor(ColorCrawlSecondary)
                            "back" -> SolidColor(ColorBackStrokeSecondary)
                            "breast" -> SolidColor(ColorBreastStrokeSecondary)
                            "butterfly" -> SolidColor(ColorButterflySecondary)
                            "kickBoard" -> SolidColor(ColorKickBoardSecondary)
                            else -> Brush.verticalGradient(
                                Pair(0f, ColorMixStartSecondary),
                                Pair(1f, ColorMixEndSecondary)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(bottom = 1.dp)
                            .fillMaxWidth(widthRatio)
                            .height(10.dp)
                            .background(
                                brush = color,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .align(Alignment.Start),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (widthRatio == 1f) {
                            val distance = when (type) {
                                "crawl" -> dailyRecord.value?.crawl
                                "back" -> dailyRecord.value?.backStroke
                                "breast" -> dailyRecord.value?.breastStroke
                                "butterfly" -> dailyRecord.value?.butterfly
                                "kickBoard" -> dailyRecord.value?.kickBoard
                                else -> dailyRecord.value?.mixed
                            }
                            Text(
                                modifier = Modifier.padding(end = 2.dp),
                                text = distance.toString(),
                                lineHeight = 10.sp,
                                fontSize = 8.sp,
                                color = Color.White
                            )
                        }
                    }
                }
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
                textAlign = TextAlign.Center,
                color = dateTextColor
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
fun CalendarDetailView(
    modifier: Modifier,
//    viewModel: PreviewViewmodel, // Preview 용
    viewModel: SwimmingViewModel,
    currentDate: Instant,
    initialHeight: Int
) {
    val mySaver =
        Saver<Dp, Bundle>(save = { Bundle().apply { putFloat("columnHeight", it.value) } },
            restore = { it.getFloat("columnHeight").dp })
    var columnHeight by rememberSaveable(stateSaver = mySaver) { mutableStateOf(initialHeight.dp) }

    Column(
        Modifier
            .padding(0.dp, 0.dp, 0.dp, 60.dp)
            .then(modifier)
            .fillMaxWidth()
            .height(columnHeight)
            .navigationBarsPadding()
            .background(ColorCalendarDetailBg, shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp))
            .padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 조절 바
        Box(
            modifier
                .fillMaxWidth()
                .height(15.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Column의 높이를 조정
                        columnHeight = max(15.dp.toPx(), columnHeight.toPx() - dragAmount.y).toDp()
                    }
                }, contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 5.dp)
                    .background(Color.Gray.copy(alpha = 0.6f), RoundedCornerShape(50))
            )
        }

        // 데이터
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            val detailRecord by viewModel.currentDetailRecord.collectAsState()
            detailRecord.forEach {
                Column(
                    Modifier
                        .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 5.dp)
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(15.dp))
                        .padding(5.dp)
                ) {
                    val startTime = it!!.detailRecord.startTime.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd(E) HH:mm"))
                    val endTime = it.detailRecord.endTime.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm"))


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = DecimalFormat("#,###").format(it.detailRecord.distance?.toInt()) + "m")
                            Text(
                                text = "$startTime ~ $endTime",
                                fontSize = 10.sp,
                                lineHeight = 10.sp
                            )
                        }

                        Button(
                            modifier = Modifier.height(24.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(),
                            onClick = {
                                viewModel.setModifyRecord(it.detailRecord)
                                viewModel.popupUiState.value = PopupUiState.MODIFY
                            }) {
                            Text(text = "영법 수정", fontSize = 12.sp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        val formattedDuration =
                            Duration.parse(it.detailRecord.activeTime ?: "0").toCustomTimeString()
                        Text(text = "수영시간: ")
                        Text(text = formattedDuration)
                    }
                    Text(text = "거리" + (it.detailRecord.distance?.toString() ?: " 기록 없음"))
                    Text(text = "평균심박" + (it.detailRecord.avgHeartRate?.toString() ?: " 기록 없음"))
                    Text(text = "최고심박" + (it.detailRecord.maxHeartRate?.toString() ?: " 기록 없음"))
                    Text(text = "최저심박" + (it.detailRecord.minHeartRate?.toString() ?: " 기록 없음"))
                    Text(text = "칼로리 소모" + (it.detailRecord.energyBurned?.toString() ?: " 기록 없음"))

                    IconButton(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .size(50.dp)
                            .align(Alignment.End),
                        onClick = {
                            viewModel.setModifyRecord(it.detailRecord)
                            viewModel.popupUiState.value = PopupUiState.MODIFY
                        }) {

                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.btn_edit),
                            modifier = modifier.size(50.dp),
                            contentDescription = "기록 버튼",
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
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
    val currentDetailRecord: StateFlow<List<DetailRecordWithHeartRateSample?>> =
        MutableStateFlow(
            listOf(
                DetailRecordWithHeartRateSample(
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