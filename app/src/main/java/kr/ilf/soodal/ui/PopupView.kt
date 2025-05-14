package kr.ilf.soodal.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
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
import kr.ilf.soodal.viewmodel.PopupUiState
import kr.ilf.soodal.viewmodel.SwimmingViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
            .padding(10.dp),
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
                val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
                val startTime = record.startTime.atZone(ZoneId.systemDefault())
                    .format(formatter)
                val endTime =
                    record.endTime.atZone(ZoneId.systemDefault()).format(formatter)

                val totalDistance = remember { record.distance?.toInt() ?: 0 }
                val crawl = remember { mutableIntStateOf(record.crawl) }
                val back = remember { mutableIntStateOf(record.backStroke) }
                val breast = remember { mutableIntStateOf(record.breastStroke) }
                val butterfly = remember { mutableIntStateOf(record.butterfly) }
                val kick = remember { mutableIntStateOf(record.kickBoard) }
                val mixed = remember { mutableIntStateOf(record.mixed) }

                val usefulDistance by
                remember {
                    derivedStateOf {
                        totalDistance - crawl.intValue - back.intValue - breast.intValue - butterfly.intValue - kick.intValue - mixed.intValue
                    }
                }

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {

                    Column(
                        rowModifier
                    ) {
                        val poolLength = record.poolLength
                        Text(text = "수영 시간 $startTime ~ $endTime")
                        Text(text = "총 거리 ${record.distance}")
                        Text(text = "잔여 거리 $usefulDistance")
                        DistanceRow(
                            rowModifier,
                            crawl,
                            usefulDistance + crawl.intValue,
                            totalDistance,
                            poolLength,
                            "자유형"
                        )
                        DistanceRow(
                            rowModifier,
                            back,
                            usefulDistance + back.intValue,
                            totalDistance,
                            poolLength,
                            "배영"
                        )
                        DistanceRow(
                            rowModifier,
                            breast,
                            usefulDistance + breast.intValue,
                            totalDistance,
                            poolLength,
                            "평영"
                        )
                        DistanceRow(
                            rowModifier,
                            butterfly,
                            usefulDistance + butterfly.intValue,
                            totalDistance,
                            poolLength,
                            "접영"
                        )
                        DistanceRow(
                            rowModifier,
                            mixed,
                            usefulDistance + mixed.intValue,
                            totalDistance,
                            poolLength,
                            "혼영"
                        )
                        DistanceRow(
                            rowModifier,
                            kick,
                            usefulDistance + kick.intValue,
                            totalDistance,
                            poolLength,
                            "킥판"
                        )
                    }
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

@Composable
private fun DistanceRow(
    modifier: Modifier,
    distance: MutableIntState,
    usefulDistance: Int,
    maxDistance: Int,
    poolLength: Int,
    title: String
) {

    Row(modifier) {
        Text(modifier = Modifier.width(50.dp), text = title)
        DistanceSlider(maxDistance, poolLength, usefulDistance, distance)
    }
}

/**
 * DistanceSlider
 *
 * @param maxDistance Slider end 값
 * @param poolLength pool 길이
 * @param limitDistance 선택 가능한 최대 value
 * @param distance TextField 에 표기 되고, 저장될 값
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DistanceSlider(
    maxDistance: Int,
    poolLength: Int,
    limitDistance: Int,
    distance: MutableIntState
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    var sliderValue by remember { mutableFloatStateOf(distance.intValue.toFloat()) }
    val sliderStep by remember { derivedStateOf { (sliderValue / poolLength).toInt() } }

    LaunchedEffect(sliderStep) {
        distance.intValue = sliderStep * poolLength
    }
//
//    TextField(
//        modifier = Modifier.width(100.dp),
//        value = distance.intValue.toString(),
//        onValueChange = { value ->
//            val intValue = value.toIntOrNull() ?: 0
//            sliderValue = intValue.coerceAtLeast(0).toFloat()
//        },
//        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//        singleLine = true
//    )
    IconButton(modifier = Modifier.size(30.dp), onClick = {
        sliderValue =
            (distance.intValue - poolLength).coerceAtLeast(0).toFloat()
    }) {
        Icon(
            ImageBitmap.imageResource(R.drawable.ic_arrow_decrease),
            contentDescription = "거리 감소",
            tint = Color.Unspecified
        )
    }

    Slider(
        value = sliderValue,
        onValueChange = { sliderValue = it },
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.secondary,
            activeTrackColor = MaterialTheme.colorScheme.secondary,
            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
        ),
        valueRange = 0f..maxDistance.toFloat(),
        onValueChangeFinished = {
            val tempDistance = sliderStep * poolLength
            val targetDistance = if (tempDistance > limitDistance) {
                limitDistance.toFloat()
            } else {
                tempDistance.toFloat()
            }

            coroutineScope.launch {
                animate(sliderValue, targetDistance) { value, _ ->
                    sliderValue = value
                }
            }
        },
        interactionSource = interactionSource,
        thumb = {
            Label(label = {
                Box(Modifier.width(50.dp), contentAlignment = Alignment.Center) {
                    PlainTooltip(
                        modifier = Modifier
                            .widthIn(0.dp, 50.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .height(30.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = distance.intValue.toString(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

            }, interactionSource = interactionSource) {
                SliderDefaults.Thumb(
                    modifier = Modifier.height(25.dp),
                    interactionSource = interactionSource
                )
            }
        }
    )

    IconButton(
        modifier = Modifier.size(30.dp),
        onClick = { sliderValue += poolLength }) {
        Icon(
            ImageBitmap.imageResource(R.drawable.ic_arrow_increase),
            contentDescription = "거리 추가",
            tint = Color.Unspecified
        )
    }
}

@Composable
@Preview
fun DistanceRowPreview() {
    val distance = remember { mutableIntStateOf(350) }
    DistanceRow(modifier = Modifier.fillMaxWidth(), distance, 800, 1025, 25, "sds")
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