package kr.ilf.soodal.ui

import android.content.res.Resources
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.ilf.soodal.R
import kr.ilf.soodal.database.entity.DetailRecord
import kr.ilf.soodal.database.entity.DetailRecordWithHR
import kr.ilf.soodal.database.entity.HeartRateSample
import kr.ilf.soodal.ui.theme.ColorBackStroke
import kr.ilf.soodal.ui.theme.ColorBackStrokeSecondary
import kr.ilf.soodal.ui.theme.ColorBreastStroke
import kr.ilf.soodal.ui.theme.ColorBreastStrokeSecondary
import kr.ilf.soodal.ui.theme.ColorButterfly
import kr.ilf.soodal.ui.theme.ColorButterflySecondary
import kr.ilf.soodal.ui.theme.ColorCalendarDate
import kr.ilf.soodal.ui.theme.ColorCalendarDateBg
import kr.ilf.soodal.ui.theme.ColorCalendarDateBgDis
import kr.ilf.soodal.ui.theme.ColorCalendarDateDis
import kr.ilf.soodal.ui.theme.ColorCalendarItemBg
import kr.ilf.soodal.ui.theme.ColorCalendarItemBgDis
import kr.ilf.soodal.ui.theme.ColorCalendarOnItemBg
import kr.ilf.soodal.ui.theme.ColorCalendarOnItemBorder
import kr.ilf.soodal.ui.theme.ColorCalendarToday
import kr.ilf.soodal.ui.theme.ColorCalendarTodayBg
import kr.ilf.soodal.ui.theme.ColorCrawl
import kr.ilf.soodal.ui.theme.ColorCrawlSecondary
import kr.ilf.soodal.ui.theme.ColorKickBoard
import kr.ilf.soodal.ui.theme.ColorKickBoardSecondary
import kr.ilf.soodal.ui.theme.ColorMixEnd
import kr.ilf.soodal.ui.theme.ColorMixEndSecondary
import kr.ilf.soodal.ui.theme.ColorMixStart
import kr.ilf.soodal.ui.theme.ColorMixStartSecondary
import kr.ilf.soodal.ui.theme.SkyBlue6
import kr.ilf.soodal.ui.theme.notoSansKr
import kr.ilf.soodal.viewmodel.PopupUiState
import kr.ilf.soodal.viewmodel.SwimmingViewModel
import kr.ilf.soodal.viewmodel.UiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration

val selectedMonthSaver =
    mapSaver(save = { mapOf("selectedMonth" to it) },
        restore = { it["selectedMonth"] as LocalDate })

@Composable
fun CalendarView(
    modifier: Modifier,
    contentsBg: Color,
    viewModel: SwimmingViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    val today by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by viewModel.currentMonth
    val selectedMonth = rememberSaveable(stateSaver = selectedMonthSaver) {
        mutableStateOf(
            LocalDate.now().withDayOfMonth(1)
        )
    }
    val selectedDateStr =
        rememberSaveable() { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }
    val pagerState = rememberPagerState(0, pageCount = { 12 }) // 12달 간의 달력 제공
    pagerState.currentPageOffsetFraction

    // 최초 진입 시 DetailRecord 조회, 새 데이터 확인 / dispose 시 데이터 초기화
    DisposableEffect(Unit) {
        val selectedInstant = selectedMonth.value.withDayOfMonth(selectedDateStr.value.toInt())
            .atStartOfDay(ZoneId.systemDefault()).toInstant()
        viewModel.findDetailRecord(selectedInstant)
        viewModel.checkAndShowNewRecordPopup()

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
        viewModel.updateDailyRecords()
    }

    Column(modifier = modifier) {
        CalendarHeaderView(viewModel, contentsBg)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .background(contentsBg, shape = RoundedCornerShape(10.dp))
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

        // 날짜 박스
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(15.dp)
                .border(1.dp, dateBorderColor, RoundedCornerShape(5.dp))
                .background(Color.Transparent, RoundedCornerShape(5.dp))
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center),
                text = day, // 날짜만 표시
                fontSize = 10.sp,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center,
                fontFamily = notoSansKr,
                color = dateTextColor
            )
        }

        val dailyRecords by viewModel.dailyRecords.collectAsState()
        val dailyRecord = remember {
            derivedStateOf {
                dailyRecords[thisDate.atStartOfDay().atZone(ZoneId.systemDefault())]
            }
        }

        dailyRecord.value?.let {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = dailyRecord.value!!.totalDistance!!,
                color = Color.Gray,
                fontFamily = notoSansKr,
                fontSize = 8.sp,
                lineHeight = 8.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val colorCrawl = if (isThisMonth) ColorCrawl else ColorCrawlSecondary
            val colorBackStroke = if (isThisMonth) ColorBackStroke else ColorBackStrokeSecondary
            val colorBreastStroke =
                if (isThisMonth) ColorBreastStroke else ColorBreastStrokeSecondary
            val colorButterfly = if (isThisMonth) ColorButterfly else ColorButterflySecondary
            val colorKickBoard = if (isThisMonth) ColorKickBoard else ColorKickBoardSecondary
            val colorMixStart = if (isThisMonth) ColorMixStart else ColorMixStartSecondary
            val colorMixEnd = if (isThisMonth) ColorMixEnd else ColorMixEndSecondary

            val brushList = remember {
                derivedStateOf {
                    dailyRecord.value?.let { record ->
                        // 거리 정보를 리스트로 변환
                        val distances = mapOf(
                            SolidColor(colorCrawl) to record.crawl,
                            SolidColor(colorBackStroke) to record.backStroke,
                            SolidColor(colorBreastStroke) to record.breastStroke,
                            SolidColor(colorButterfly) to record.butterfly,
                            SolidColor(colorKickBoard) to record.kickBoard,
                            Brush.verticalGradient(
                                Pair(0f, colorMixStart),
                                Pair(1f, colorMixEnd)
                            ) to record.mixed
                        )

                        val ratioList = distributeDistance(distances, 8)
                        ratioList
                    } ?: emptyList<Brush>()

                }
            }
//            val blendMode = BlendMode.Luminosity
//                val blendMode = BlendMode.Color
//                val blendMode = BlendMode.Hue

//            HexagonCircleGraph(70.dp, 10.dp, 15.dp, BlendMode.Luminosity)
//            HexagonCircleGraph(70.dp, 10.dp, 15.dp, BlendMode.Color)
//            HexagonCircleGraph(70.dp, 10.dp, 15.dp, BlendMode.Hue)

            if (brushList.value.isNotEmpty()) {

//                HexagonCircleGraph(
//                    brushList.value,
//                    36.dp,
//                    5.dp,
//                    8.dp,
//                    BlendMode.Luminosity
//                )
                IconWithPolygon(
                    painterResource(id = R.drawable.ic_pearl2),
                    brushList.value,
                    28.dp,
                    16.dp,
                    false
                )
            }
        }
    }
}

@Composable
private fun CalendarHeaderView(
    viewModel: SwimmingViewModel,
    contentsBg: Color
) {
    val currentMonth by viewModel.currentMonth
    val currentMonthTotal by viewModel.currentMonthTotal.collectAsState()
    val monthFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
    // 년, 월
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = currentMonth.format(monthFormatter),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(top = 10.dp, start = 5.dp, end = 5.dp)
                .background(contentsBg, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 15.dp),
            textAlign = TextAlign.Center
        )

        val totalDistance = remember { derivedStateOf { currentMonthTotal.totalDistance ?: "0" } }
        val totalCaloriesBurned =
            remember { derivedStateOf { currentMonthTotal.totalEnergyBurned ?: "0" } }

        Column(Modifier.wrapContentSize(), verticalArrangement = Arrangement.Center) {
            Text(
                totalDistance.value + "m",
                color = Color.Gray,
                fontFamily = notoSansKr,
                fontSize = 12.sp,
                lineHeight = 12.sp,
            )
            Text(
                totalCaloriesBurned.value.toFloat().roundToInt().toString() + " kcal",
                color = Color.Gray,
                fontFamily = notoSansKr,
                fontSize = 12.sp,
                lineHeight = 12.sp,
            )
        }

    }

    // 요일 헤더
    Row(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp, top = 5.dp)
            .background(contentsBg, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "일",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            color = Color.Red
        )

        listOf("월", "화", "수", "목", "금").forEach {
            Text(
                text = it,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "토",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
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
    resizeBar: @Composable (Modifier) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        // 조절 바
        resizeBar(Modifier)

        // 데이터
        Column(
            Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
//                .verticalScroll(rememberScrollState())
                .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
        ) {
            // 거리, 시간, 칼로리, 최고 심박, 최저 심박
            val detailRecords by viewModel.currentDetailRecords.collectAsState()

            // 새 화면
            var totalDistance by remember { mutableIntStateOf(0) }
            var totalTime by remember { mutableStateOf(Duration.ZERO) }
            var totalCalories by remember { mutableDoubleStateOf(0.0) }
            var totalMinHR by remember { mutableIntStateOf(Int.MAX_VALUE) }
            var totalMaxHR by remember { mutableIntStateOf(0) }
            var totalCrawl by remember { mutableIntStateOf(0) }
            var totalBackStroke by remember { mutableIntStateOf(0) }
            var totalBreastStroke by remember { mutableIntStateOf(0) }
            var totalButterfly by remember { mutableIntStateOf(0) }
            var totalKickBoard by remember { mutableIntStateOf(0) }
            var totalMixed by remember { mutableIntStateOf(0) }

            val animationSpec = spring(
                visibilityThreshold = Int.VisibilityThreshold,
                stiffness = Spring.StiffnessLow
            )

            val animatedCrawl by animateIntAsState(totalCrawl, animationSpec)
            val animatedBackStroke by animateIntAsState(totalBackStroke, animationSpec)
            val animatedBreastStroke by animateIntAsState(totalBreastStroke, animationSpec)
            val animatedButterfly by animateIntAsState(totalButterfly, animationSpec)
            val animatedKickBoard by animateIntAsState(totalKickBoard, animationSpec)
            val animatedMixed by animateIntAsState(totalMixed, animationSpec)

            LaunchedEffect(detailRecords) {
                var tempTotalDistance = totalDistance
                var tempTotalTime = totalTime
                var tempTotalCalories = totalCalories
                var tempTotalMinHR = totalMinHR
                var tempTotalMaxHR = totalMaxHR
                var tempTotalCrawl = totalCrawl
                var tempTotalBackStroke = totalBackStroke
                var tempTotalBreastStroke = totalBreastStroke
                var tempTotalButterfly = totalButterfly
                var tempTotalKickBoard = totalKickBoard
                var tempTotalMixed = totalMixed

                detailRecords.forEachIndexed { index, (record, sample) ->
                    if (index == 0) {
                        tempTotalDistance = 0
                        tempTotalTime = Duration.ZERO
                        tempTotalCalories = 0.0
                        tempTotalMinHR = Int.MAX_VALUE
                        tempTotalMaxHR = 0
                        tempTotalCrawl = 0
                        tempTotalBackStroke = 0
                        tempTotalBreastStroke = 0
                        tempTotalButterfly = 0
                        tempTotalKickBoard = 0
                        tempTotalMixed = 0
                    }

                    tempTotalDistance += record.distance?.toInt() ?: 0
                    tempTotalTime += record.activeTime?.let { Duration.parse(it) } ?: Duration.ZERO
                    tempTotalCalories += record.energyBurned?.toDouble() ?: 0.0
                    tempTotalMinHR = min(totalMinHR, record.minHeartRate?.toInt() ?: 0)
                    tempTotalMaxHR = maxOf(totalMaxHR, record.maxHeartRate?.toInt() ?: 0)
                    tempTotalCrawl += record.crawl
                    tempTotalBackStroke += record.backStroke
                    tempTotalBreastStroke += record.breastStroke
                    tempTotalButterfly += record.butterfly
                    tempTotalKickBoard += record.kickBoard
                    tempTotalMixed += record.mixed
                }

                totalDistance = tempTotalDistance
                totalTime = tempTotalTime
                totalCalories = tempTotalCalories
                totalMinHR = tempTotalMinHR
                totalMaxHR = tempTotalMaxHR
                totalCrawl = tempTotalCrawl
                totalBackStroke = tempTotalBackStroke
                totalBreastStroke = tempTotalBreastStroke
                totalButterfly = tempTotalButterfly
                totalKickBoard = tempTotalKickBoard
                totalMixed = tempTotalMixed
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = totalDistance.toString() + "m",
                    fontSize = 36.dp.toSp,
                    lineHeight = 36.dp.toSp,
                )
                // 임시 수정버튼
                detailRecords.forEach {
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
            }

            Column {
                Row(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("시간")
                        Text(totalTime.toCustomTimeString())
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("칼로리")
                        Text(totalCalories.toInt().toString())
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("최대 심박")
                        Text(totalMaxHR.toString())
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("최소 심박")
                        Text(totalMinHR.toString())
                    }
                }
            }

            Column(
                Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth()
                    .background(
                        SkyBlue6, shape = RoundedCornerShape(15.dp)
                    )
                    .padding(8.dp)
            ) {
                var refValue by remember { mutableIntStateOf(1000) }
                val animatedRefVal by animateIntAsState(
                    refValue, spring(
                        visibilityThreshold = Int.VisibilityThreshold,
                        stiffness = 200f
                    )
                )

                listOf(
                    Triple(totalCrawl, animatedCrawl, SolidColor(ColorCrawl)),
                    Triple(totalBackStroke, animatedBackStroke, SolidColor(ColorBackStroke)),
                    Triple(totalBreastStroke, animatedBreastStroke, SolidColor(ColorBreastStroke)),
                    Triple(totalButterfly, animatedButterfly, SolidColor(ColorButterfly)),
                    Triple(
                        totalMixed, animatedMixed, Brush.verticalGradient(
                            Pair(0f, ColorMixStart),
                            Pair(1f, ColorMixEnd)
                        )
                    ),
                    Triple(totalKickBoard, animatedKickBoard, SolidColor(ColorKickBoard))
                ).filter {
                    it.first != 0
                }.sortedByDescending {
                    it.first
                }.forEachIndexed { i, it ->
                    if (i == 0) refValue = max(1000, it.first)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .height(30.dp)
                                .fillMaxWidth(it.second / animatedRefVal.toFloat())
                                .background(
                                    it.third,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(end = 7.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (it.first >= 75) Text(
                                it.second.toString(),
                                lineHeight = 14.dp.toSp,
                                fontSize = 14.dp.toSp,
                                color = Color.Black.copy(0.8f)
                            )
                        }

                        if (it.first < 75) Text(
                            it.second.toString(),
                            lineHeight = 14.dp.toSp,
                            fontSize = 14.dp.toSp,
                            color = Color.Black.copy(0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResizeBar(
    modifier: Modifier = Modifier,
    detailHeight: MutableState<Dp>,
    initialHeight: Dp,
    maxHeight: Dp
) {
    val velocityTracker = remember { VelocityTracker() } // 속도 추적기

    Box(
        modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position) // 위치 기록

                        // 현재 높이 조절
                        detailHeight.value = max(
                            initialHeight, // 최소 크기 제한
                            detailHeight.value - dragAmount.y.toDp()
                        )
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().y  // Y축 속도(px/s)
                        val thresholdVelocity = 500f  // 임계 속도 (px/s)
                        val extendHeight = detailHeight.value - initialHeight
                        val halfPoint = (maxHeight - initialHeight) / 2

                        detailHeight.value = when {
                            // 빠르게 위로 스와이프 → 최대 크기
                            velocity < -thresholdVelocity -> maxHeight
                            // 빠르게 아래로 스와이프 → 초기 크기
                            velocity > thresholdVelocity -> initialHeight
                            // 절반 이상이면 최대 크기, 아니면 초기 크기로 스냅
                            extendHeight > halfPoint -> maxHeight
                            else -> initialHeight
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(5.dp)
                .size(width = 80.dp, height = 5.dp)
                .background(
                    Color.Gray.copy(alpha = 0.6f),
                    RoundedCornerShape(50)
                )
                .align(Alignment.TopCenter)
        )
    }
}

/*@Preview
@Composable
fun LineGraph() {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineSeries { series(5, 6, 5, 2, 11, 8, 5, 2, 15, 11, 8, 13, 12, 10, 2, 7) }
        }
    }
    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer,
        modifier = Modifier.fillMaxSize(),
    )
}*/

@Preview(widthDp = 100, heightDp = 100)
@Composable
fun ShrimpIconWithBoxPreview() {
    Column {
        IconWithPolygon(
            painterResource(id = R.drawable.ic_pearl2),
            listOf(
                SolidColor(ColorCrawl),
                SolidColor(ColorBackStroke),
                SolidColor(ColorBreastStroke),
                SolidColor(ColorButterfly),
                SolidColor(ColorKickBoard),
                Brush.verticalGradient(
                    Pair(0f, ColorMixStart),
                    Pair(1f, ColorMixEnd)
                )
            ), 30.dp, 30.dp
        )

        IconWithPolygon(
            painterResource(id = R.drawable.ic_pearl2),
            listOf(
                SolidColor(ColorCrawl),
                SolidColor(ColorBackStroke),
                SolidColor(ColorBreastStroke),
                SolidColor(ColorButterfly),
                SolidColor(ColorKickBoard),
                Brush.verticalGradient(
                    Pair(0f, ColorMixStart),
                    Pair(1f, ColorMixEnd)
                )
            ), 30.dp, 30.dp, false
        )
    }
}

@Composable
fun IconWithPolygon(
    painter: Painter,
    brushList: List<Brush>,
    diameter: Dp,
    iconSize: Dp,
    isRotate: Boolean = true
) {
    Box(modifier = Modifier.size(diameter + iconSize), contentAlignment = Alignment.Center) {
        val radius = with(LocalDensity.current) { diameter.toPx() / 2 }
        val offsetAngle = 360 / brushList.size.toFloat()

        brushList.forEachIndexed { index, brush ->
            val angle = index * offsetAngle
            val radian = Math.toRadians(angle.toDouble())
            val offsetX = radius * cos(radian).toFloat()
            val offsetY = radius * sin(radian).toFloat()

            Icon(
                painter = painter,
                contentDescription = "graph",
                modifier = Modifier
                    .size(iconSize)
                    .offset(offsetX.toDp(), offsetY.toDp())
                    .graphicsLayer(
                        rotationZ = if (isRotate) angle + 90f else 0f, // 이미지 여백을 위해 기본으로 20도 돌림
                        compositingStrategy = CompositingStrategy.Offscreen
                    )
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(
                                brush,
                                blendMode = BlendMode.SrcAtop
                            )
                        }
                    },
                tint = Color.Unspecified
            )
        }

        val firstBrush = brushList.firstOrNull()
        firstBrush?.let {
            val radian = Math.toRadians(0.0)
            val offsetX = radius * cos(radian).toFloat()
            val offsetY = radius * sin(radian).toFloat()

            Icon(
                painter = painter,
                contentDescription = "graph",
                modifier = Modifier
                    .size(iconSize)
                    .offset(offsetX.toDp(), offsetY.toDp())
                    .graphicsLayer(
                        rotationZ = if (isRotate) 90f else 0f,
                        compositingStrategy = CompositingStrategy.Offscreen
                    )
                    .drawWithCache {
                        onDrawWithContent {
                            // 왼쪽 절반 그리기
                            val (width, height) = if (isRotate) {
                                (size.width / 2) to size.height
                            } else {
                                size.width to size.height / 2
                            }

                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = width,
                                bottom = height
                            ) {
                                rotate(0f, center) {
                                    this@onDrawWithContent.drawContent()
                                }
                            }

                            // 오른쪽 절반 그리기 (겹치는 부분만)
                            drawRect(
                                firstBrush,
                                size = size,
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    },
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun HexagonCircleGraph(
    brushList: List<Brush>,
    size: Dp,
    radius: Dp,
    circleRadius: Dp,
    blendMode: BlendMode = BlendMode.Luminosity
) {
    Canvas(
        modifier = Modifier
            .size(size)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val diameter = radius * 2
        val offsetX = sqrt(diameter.value.pow(2) - radius.value.pow(2)).dp

        // 첫 번째 줄 (1개)
        drawCircle(
            brush = brushList[0],
            radius = circleRadius.toPx(),
            center = Offset(center.x, center.y - diameter.toPx()),
            blendMode = blendMode
        )

        // 두 번째 줄 (2개)
        drawCircle(
            brush = brushList[5],
            radius = circleRadius.toPx(),
            center = Offset(
                center.x - offsetX.toPx(),
                center.y - radius.toPx()
            ),
            blendMode = blendMode
        )

        drawCircle(
            brush = brushList[1],
            radius = circleRadius.toPx(),
            center = Offset(
                center.x + offsetX.toPx(),
                center.y - radius.toPx()
            ),
            blendMode = blendMode
        )

        // 세 번째 줄 (2개)
        drawCircle(
            brush = brushList[4],
            radius = circleRadius.toPx(),
            center = Offset(
                center.x - offsetX.toPx(),
                center.y + radius.toPx()
            ),
            blendMode = blendMode
        )

        drawCircle(
            brush = brushList[2],
            radius = circleRadius.toPx(),
            center = Offset(
                center.x + offsetX.toPx(),
                center.y + radius.toPx()
            ),
            blendMode = blendMode
        )

        // 네 번째 줄 (1개)
        drawCircle(
            brush = brushList[3],
            radius = circleRadius.toPx(),
            center = Offset(center.x, center.y + diameter.toPx()),
            blendMode = blendMode
        )
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
    val currentDetailRecord: StateFlow<List<DetailRecordWithHR?>> =
        MutableStateFlow(
            listOf(
                DetailRecordWithHR(
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

// 시스템 설정과 상관 없이 text 크기 고정
val Dp.toSp: TextUnit @Composable get() = with(LocalDensity.current) { this@toSp.toSp() }

fun distributeDistance(distances: Map<Brush, Int>, size: Int = 6): List<Brush> {
    val total = distances.values.sum() // 전체 합
    val proportions = distances.mapValues { (it.value * size).toDouble() / total } // 비율 계산

    val intParts = proportions.mapValues { it.value.toInt() } // 정수 부분 할당
    var remaining = size - intParts.values.sum() // 남은 개수

    // 결과 리스트
    val resultList = mutableListOf<Brush>()

    // 정수 개수만큼 먼저 추가
    for ((key, count) in intParts) {
        repeat(count) { resultList.add(key) }
    }

    // 남은 개수를 가장 비율이 높은 순으로 채워 넣기
    val sortedEntries = proportions.entries.sortedByDescending { it.value % 1 } // 소수점 부분 기준 정렬
    for ((key, _) in sortedEntries) {
        if (remaining > 0) {
            resultList.add(key)
            remaining--
        } else break
    }

    val groupedList = resultList.sortedBy { resultList.indexOf(it) }.toMutableList()

    val shift = Random.nextInt(0, size)
//    val finalList = groupedList.drop(shift) + groupedList.take(shift)

//    return finalList
    return groupedList
}