package kr.ilf.soodal.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.ilf.soodal.R
import kr.ilf.soodal.database.entity.DetailRecord
import kr.ilf.soodal.ui.theme.ColorBackStroke
import kr.ilf.soodal.ui.theme.ColorBreastStroke
import kr.ilf.soodal.ui.theme.ColorButterfly
import kr.ilf.soodal.ui.theme.ColorCalItemBg
import kr.ilf.soodal.ui.theme.ColorCrawl
import kr.ilf.soodal.ui.theme.ColorKickBoard
import kr.ilf.soodal.ui.theme.ColorMixStart
import kr.ilf.soodal.viewmodel.PopupUiState
import kr.ilf.soodal.viewmodel.SwimmingViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun PopupView(modifier: Modifier, viewModel: SwimmingViewModel, navController: NavHostController) {
    val context = LocalContext.current

    // dimm
    AnimatedVisibility(
        visible = viewModel.popupUiState.value != PopupUiState.NONE,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .background(Color.Black.copy(0.2f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { /*viewModel.popupUiState.value = PopupUiState.NONE*/ }
        )
    }

    NewSessionsPopup(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize()
            .widthIn(300.dp)
            .fillMaxWidth(0.8f)
            .shadow(5.dp, shape = RoundedCornerShape(25.dp))
            .background(Color.White, shape = RoundedCornerShape(25.dp))
            .padding(10.dp),
        visible = viewModel.popupUiState.value in setOf(
            PopupUiState.NEW_SESSIONS,
            PopupUiState.NEW_SESSIONS_MODIFY
        ),
        newMap = viewModel.newRecords.collectAsState().value,
        onClickModify = {
            viewModel.popupUiState.value = PopupUiState.NEW_SESSIONS_MODIFY
            viewModel.setModifyRecord(it)
        },
        onClickClose = { viewModel.popupUiState.value = PopupUiState.NONE }
    )

    ModifyRecordPopup(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 5.dp)
            .shadow(5.dp, shape = RoundedCornerShape(30.dp))
            .scrollable(rememberScrollState(), Orientation.Vertical)
            .background(
                Color.White, shape = ShapeDefaults.ExtraLarge.copy(
                    bottomStart = CornerSize(0.0.dp),
                    bottomEnd = CornerSize(0.0.dp)
                )
            )
            .padding(15.dp),
        visible = viewModel.popupUiState.value in setOf(
            PopupUiState.MODIFY,
            PopupUiState.NEW_SESSIONS_MODIFY
        ),
        onClickDone = { record ->
            viewModel.modifyDetailRecord(record)

            if (viewModel.popupUiState.value == PopupUiState.NEW_SESSIONS_MODIFY) {
                viewModel.removeNewRecord(record.id)

                viewModel.newRecords.value.ifEmpty {
                    viewModel.popupUiState.value = PopupUiState.NONE
                    return@ModifyRecordPopup
                }

                viewModel.popupUiState.value = PopupUiState.NEW_SESSIONS
            } else {
                viewModel.popupUiState.value = PopupUiState.NONE
            }
//            navController.navigate(Destination.Calendar.route) {
//                launchSingleTop = true
//            }
        },
        onClickCancel = {
            if (viewModel.popupUiState.value == PopupUiState.NEW_SESSIONS_MODIFY) {
                viewModel.popupUiState.value = PopupUiState.NEW_SESSIONS
            } else {
                viewModel.popupUiState.value = PopupUiState.NONE
            }
        },
        viewModel.currentModifyRecord.collectAsState().value
    )

    AppFinishPopup(
        modifier = modifier,
        visible = viewModel.popupUiState.value == PopupUiState.APP_FINISH,
        onClickDone = { (context as Activity).finishAndRemoveTask() },
        onClickCancel = { viewModel.popupUiState.value = PopupUiState.NONE }
    )
}


@Composable
fun NewSessionsPopup(
    modifier: Modifier,
    visible: Boolean,
    newMap: Map<String, DetailRecord>,
    onClickModify: (detailRecord: DetailRecord) -> Unit,
    onClickClose: () -> Unit
) {
    AnimatedVisibility(
        visible,
        enter = scaleIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

            Text(modifier = Modifier.align(Alignment.CenterHorizontally), text = "새로운 수영 기록이 있어요!")

            newMap.forEach { (id, record) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray.copy(0.3f), shape = RoundedCornerShape(10.dp)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        Text(
                            record.startTime.atZone(ZoneId.systemDefault()).format(formatter),
                            lineHeight = 16.sp
                        )
                        Text("${record.distance ?: 0}m", lineHeight = 16.sp)
                    }
                    IconButton(onClick = { onClickModify(record) }) {
                        Icon(
                            modifier = Modifier.padding(),
                            imageVector = ImageVector.vectorResource(R.drawable.ic_modify),
                            contentDescription = "영법 수정"
                        )
                    }
                }
            }
            Button(onClick = onClickClose) {
                Text(text = "닫기")
            }
        }
    }
}

@Composable
fun ModifyRecordPopup(
    modifier: Modifier,
    visible: Boolean,
    onClickDone: suspend (DetailRecord) -> Unit,
    onClickCancel: () -> Unit,
    record: DetailRecord?
) {
    AnimatedVisibility(
        visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Column(
            modifier = modifier
        ) {
            record?.let {
                val rowModifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(ColorCalItemBg, shape = RoundedCornerShape(10.dp))
                    .padding(5.dp)
                val dateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd (EEE) HH:mm")
                val startTime = record.startTime.atZone(ZoneId.systemDefault())
                val endTime = record.endTime.atZone(ZoneId.systemDefault())
                val startTimeStr = startTime.format(dateTimeFormatter)
                val endTimeStr =
                    if (startTime.truncatedTo(ChronoUnit.DAYS) == endTime.truncatedTo(ChronoUnit.DAYS)) {
                        endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                    } else {
                        endTime.format(dateTimeFormatter)
                    }
                val totalDistance = remember { record.distance?.toInt() ?: 0 }
                val crawl = remember { mutableIntStateOf(record.crawl) }
                val back = remember { mutableIntStateOf(record.backStroke) }
                val breast = remember { mutableIntStateOf(record.breastStroke) }
                val butterfly = remember { mutableIntStateOf(record.butterfly) }
                val kick = remember { mutableIntStateOf(record.kickBoard) }
                val mixed = remember { mutableIntStateOf(record.mixed) }
                val usefulDistance by remember {
                    derivedStateOf {
                        totalDistance - crawl.intValue - back.intValue - breast.intValue - butterfly.intValue - kick.intValue - mixed.intValue
                    }
                }

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val poolLength = record.poolLength
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${record.distance}m",
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.displaySmall
                        )

                        Column(
                            modifier = Modifier.alignBy(LastBaseline),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "$startTimeStr ~ $endTimeStr",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "${poolLength}m",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Text(text = "잔여 거리 $usefulDistance")
                    DistanceRow(
                        rowModifier,
                        crawl,
                        usefulDistance + crawl.intValue,
                        totalDistance,
                        poolLength,
                        "자유형",
                        ColorCrawl,
                        ColorCrawl,
                        ColorCrawl.copy(0.3f)
                    )
                    DistanceRow(
                        rowModifier,
                        back,
                        usefulDistance + back.intValue,
                        totalDistance,
                        poolLength,
                        "배영",
                        ColorBackStroke,
                        ColorBackStroke,
                        ColorBackStroke.copy(0.3f)
                    )
                    DistanceRow(
                        rowModifier,
                        breast,
                        usefulDistance + breast.intValue,
                        totalDistance,
                        poolLength,
                        "평영",
                        ColorBreastStroke,
                        ColorBreastStroke,
                        ColorBreastStroke.copy(0.3f)
                    )
                    DistanceRow(
                        rowModifier,
                        butterfly,
                        usefulDistance + butterfly.intValue,
                        totalDistance,
                        poolLength,
                        "접영",
                        ColorButterfly,
                        ColorButterfly,
                        ColorButterfly.copy(0.3f)
                    )
                    DistanceRow(
                        rowModifier,
                        mixed,
                        usefulDistance + mixed.intValue,
                        totalDistance,
                        poolLength,
                        "혼영",
                        ColorMixStart,
                        ColorMixStart,
                        ColorMixStart.copy(0.3f)
                    )
                    DistanceRow(
                        rowModifier,
                        kick,
                        usefulDistance + kick.intValue,
                        totalDistance,
                        poolLength,
                        "킥판",
                        ColorKickBoard,
                        ColorKickBoard,
                        ColorKickBoard.copy(0.3f)
                    )

                }

                Row(Modifier.align(Alignment.End)) {
                    val context = LocalContext.current
                    Button(onClick = {
                        val newDistance =
                            crawl.intValue + back.intValue + breast.intValue + butterfly.intValue + kick.intValue + mixed.intValue
                        if (record.distance == newDistance.toString()) {
                            val detailRecord = record.copy(
                                crawl = crawl.intValue,
                                backStroke = back.intValue,
                                breastStroke = breast.intValue,
                                butterfly = butterfly.intValue,
                                kickBoard = kick.intValue,
                                mixed = mixed.intValue
                            )
                            CoroutineScope(Dispatchers.Default).launch { onClickDone(detailRecord) }
                        } else {
                            Toast.makeText(context, "총 거리가 다릅니다.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(text = "저장")
                    }
                    Button(onClick = onClickCancel) {
                        Text(text = "취소")
                    }
                }
            }
        }
    }
}

/**
 * DistanceRow
 *
 * @param modifier
 * @param distance 화면에 표기 되는 거리
 * @param limitDistance 선택 가능한 최대 거리(잔여 거리)
 * @param maxDistance Slider의 End 값
 * @param poolLength 수영장 길이
 * @param title
 * @param thumbColor Slider Thumb(Handle) 색상
 * @param activeTrackColor Slider 활성화된 Track 색상
 * @param inactiveTrackColor Slider 비활성화된 Track 색상
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistanceRow(
    modifier: Modifier,
    distance: MutableIntState,
    limitDistance: Int,
    maxDistance: Int,
    poolLength: Int,
    title: String,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    activeTrackColor: Color = MaterialTheme.colorScheme.primary,
    inactiveTrackColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    var sliderValue by remember { mutableFloatStateOf(distance.intValue.toFloat()) }
    val snappedValue by remember { derivedStateOf { (sliderValue / poolLength).toInt() * poolLength } }
    val iconSize = 25.dp

    LaunchedEffect(distance.intValue) {
        coroutineScope.launch {
            animate(
                initialValue = sliderValue,
                targetValue = distance.intValue.toFloat(),
                animationSpec = tween()
            ) { value, _ ->
                sliderValue = value
            }
        }
    }

    Column(modifier) {
        Text(modifier = Modifier.width(50.dp), text = title)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                modifier = Modifier
                    .height(30.dp)
                    .wrapContentSize(Alignment.Center),
                text = distance.intValue.toString() + "m",
            )

            Text(
                modifier = Modifier
                    .height(30.dp)
                    .wrapContentSize(Alignment.Center),
                text = (distance.intValue / poolLength).toString() + "구간",
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier.size(iconSize),
                enabled = distance.intValue > 0,
                colors = IconButtonColors(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.onSecondary
                ),
                onClick = {
                    distance.intValue = (distance.intValue - poolLength).coerceAtLeast(0)
                }) {

                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "거리 감소",
                    modifier = Modifier.fillMaxSize()
                )
            }

            BoxWithConstraints {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it.coerceAtMost(limitDistance.toFloat()) },
                    modifier = Modifier
                        .width(
                            constraints.maxWidth.toFloat().toDp() - iconSize
                        ) // Slider 버그? Row에서 다음 Composable 크기 무시하고 최대크기로 됨
                        .padding(horizontal = 10.dp)
                        .height(40.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = activeTrackColor,
                        inactiveTrackColor = inactiveTrackColor,
                    ),
                    enabled = limitDistance > 0,
                    valueRange = 0f..maxDistance.toFloat(),
                    onValueChangeFinished = {
                        distance.intValue = snappedValue
                    },
                    interactionSource = interactionSource,
                    thumb = {
                        Label(label = {
                            Box(Modifier.width(50.dp), contentAlignment = Alignment.Center) {
                                PlainTooltip(
                                    modifier = Modifier
                                        .widthIn(25.dp, 50.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                        .height(30.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        text = snappedValue.toString(),
                                        modifier = Modifier.widthIn(25.dp, 50.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                        }, interactionSource = interactionSource) {
                            SliderDefaults.Thumb(
                                modifier = Modifier.height(25.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = thumbColor
                                ),
                                interactionSource = interactionSource
                            )
                        }
                    }
                )
            }

            IconButton(
                modifier = Modifier.size(iconSize),
                enabled = distance.intValue < limitDistance,
                colors = IconButtonColors(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.onSecondary
                ),
                onClick = {
                    distance.intValue = (distance.intValue + poolLength).coerceAtMost(limitDistance)
                }) {

                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "거리 추가",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }


}

@Composable
@Preview
fun DistanceRowPreview() {
    val distance = remember { mutableIntStateOf(350) }
    Column {
        DistanceRow(modifier = Modifier.fillMaxWidth(), distance, 800, 1025, 25, "sds")
    }
}

@Composable
fun AppFinishPopup(
    modifier: Modifier,
    visible: Boolean,
    onClickDone: () -> Unit,
    onClickCancel: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Box(modifier, contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .shadow(
                        8.dp,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
                    .scrollable(rememberScrollState(), Orientation.Vertical)
                    .background(Color.White, shape = RoundedCornerShape(30.dp))
                    .padding(20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = { onClickCancel() }
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_close),
                            contentDescription = "취소",
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "앱을 종료하시겠습니까?", fontSize = 20.sp)
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = { onClickDone() }) {
                    Text(text = "종료")
                }
            }
        }
    }
}