package kr.ilf.soodal.ui

import android.app.Activity
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
import kr.ilf.soodal.ui.theme.ColorTextButton
import kr.ilf.soodal.ui.theme.ColorTextDefault
import kr.ilf.soodal.viewmodel.PopupUiState
import kr.ilf.soodal.viewmodel.CalendarViewModelImpl
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun PopupView(viewModel: CalendarViewModelImpl, navController: NavHostController) {
    val context = LocalContext.current
    var popupUiState by viewModel.popupUiState

    // msms onClick-- -> on--Click 으로 통일
    NewSessionsPopup(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .widthIn(300.dp)
            .fillMaxWidth(0.8f)
            .shadow(5.dp, shape = RoundedCornerShape(25.dp))
            .background(Color.White, shape = RoundedCornerShape(25.dp))
            .padding(10.dp),
        visible = popupUiState in setOf(
            PopupUiState.NEW_RECORD,
            PopupUiState.NEW_RECORD_MODIFY
        ),
        newMap = viewModel.newRecords.collectAsState().value,
        onClickModify = {
            popupUiState = PopupUiState.NEW_RECORD_MODIFY
            viewModel.setModifyRecord(it)
        },
        onClickClose = { popupUiState = PopupUiState.NONE },
        dimmed = popupUiState == PopupUiState.NEW_RECORD
    )

    ModifyRecordPopup(
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 5.dp)
            .shadow(
                5.dp,
                shape = RoundedCornerShape(30.dp).copy(
                    bottomStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp)
                )
            )
            .background(
                Color.White, shape = ShapeDefaults.ExtraLarge.copy(
                    bottomStart = CornerSize(0.0.dp),
                    bottomEnd = CornerSize(0.0.dp)
                )
            )
            .navigationBarsPadding(),
        visible = popupUiState in setOf(
            PopupUiState.MODIFY,
            PopupUiState.NEW_RECORD_MODIFY
        ),
        onClickDone = { record ->
            viewModel.modifyDetailRecord(record)

            if (popupUiState == PopupUiState.NEW_RECORD_MODIFY) {
                viewModel.removeNewRecord(record.id)

                viewModel.newRecords.value.ifEmpty {
                    popupUiState = PopupUiState.NONE
                    return@ModifyRecordPopup
                }

                popupUiState = PopupUiState.NEW_RECORD
            } else {
                popupUiState = PopupUiState.NONE
            }
//            navController.navigate(Destination.Calendar.route) {
//                launchSingleTop = true
//            }
        },
        onClickCancel = {
            if (popupUiState == PopupUiState.NEW_RECORD_MODIFY) {
                popupUiState = PopupUiState.NEW_RECORD
            } else {
                popupUiState = PopupUiState.NONE
            }
        },
        viewModel.currentModifyRecord.collectAsState().value
    )

    AppFinishPopup(
        modifier = Modifier
            .fillMaxSize(),
        visible = popupUiState == PopupUiState.APP_FINISH,
        onClickDone = { (context as Activity).finishAndRemoveTask() },
        onClickCancel = { popupUiState = PopupUiState.NONE }
    )
}

@Composable
fun NewSessionsPopup(
    modifier: Modifier,
    visible: Boolean,
    newMap: Map<String, DetailRecord>,
    onClickModify: (detailRecord: DetailRecord) -> Unit,
    onClickClose: () -> Unit,
    dimmed: Boolean = true
) {
    Dimmed(visible && dimmed)

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

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.popup_message_new_swim_record_available)
            )

            newMap.forEach { (id, record) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorCalItemBg, shape = RoundedCornerShape(10.dp)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        Text(
                            text = record.startTime.atZone(ZoneId.systemDefault())
                                .format(formatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        )
                        Text(
                            "${record.distance ?: 0}m",
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 16.sp,
                            color = ColorTextDefault
                        )
                    }
                    IconButton(onClick = { onClickModify(record) }) {
                        Icon(
                            modifier = Modifier.padding(),
                            imageVector = ImageVector.vectorResource(R.drawable.ic_modify),
                            contentDescription = stringResource(R.string.description_modify_stroke_button)
                        )
                    }
                }
            }
            Button(onClick = onClickClose) {
                Text(text = stringResource(R.string.label_close))
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
    Dimmed(visible)

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
                    .background(ColorCalItemBg, shape = MaterialTheme.shapes.large)
                    .padding(10.dp, 2.dp, 10.dp, 4.dp)
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
                val poolLength = record.poolLength
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(10.dp, 10.dp, 10.dp, 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${record.distance}m",
                                modifier = Modifier.alignByBaseline(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Medium,
                                color = ColorTextDefault
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

                        Row(
                            Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    modifier = Modifier.offset(y = 3.dp),
                                    text = stringResource(R.string.popup_label_remaining_distance),
                                    style = MaterialTheme.typography.labelSmall,
                                    lineHeight = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Normal
                                )

                                Row {
                                    Text(
                                        modifier = Modifier
                                            .alignByBaseline()
                                            .padding(end = 6.dp),
                                        text = "${usefulDistance}m",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ColorTextDefault
                                    )
                                    Text(
                                        modifier = Modifier.alignByBaseline(),
                                        text = "${usefulDistance / poolLength}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ColorTextDefault
                                    )
                                    Text(
                                        modifier = Modifier.alignByBaseline(),
                                        text = pluralStringResource(
                                            R.plurals.label_slash_laps_format,
                                            totalDistance / poolLength,
                                            totalDistance / poolLength
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal
                                    )

                                }
                            }

                            if (usefulDistance <= 0)
                                Text(
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .weight(1f),
                                    text = stringResource(R.string.popup_message_no_remaining_distance_guidance),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.End,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Normal
                                )
                        }
                    }

                    Column(
                        Modifier
                            .padding(top = 3.dp)
                            .clip(MaterialTheme.shapes.large)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DistanceBlock(
                            rowModifier,
                            crawl,
                            usefulDistance + crawl.intValue,
                            totalDistance,
                            poolLength,
                            stringResource(R.string.label_freestyle),
                            "Freestyle(Crawl)",
                            ColorCrawl,
                            ColorCrawl,
                            ColorCrawl.copy(0.3f)
                        )
                        DistanceBlock(
                            rowModifier,
                            back,
                            usefulDistance + back.intValue,
                            totalDistance,
                            poolLength,
                            stringResource(R.string.label_backstroke),
                            "Backstroke",
                            ColorBackStroke,
                            ColorBackStroke,
                            ColorBackStroke.copy(0.3f)
                        )
                        DistanceBlock(
                            rowModifier,
                            breast,
                            usefulDistance + breast.intValue,
                            totalDistance,
                            poolLength,
                            stringResource(R.string.label_breaststroke),
                            "Breaststroke",
                            ColorBreastStroke,
                            ColorBreastStroke,
                            ColorBreastStroke.copy(0.3f)
                        )
                        DistanceBlock(
                            rowModifier,
                            butterfly,
                            usefulDistance + butterfly.intValue,
                            totalDistance,
                            poolLength,
                            stringResource(R.string.label_butterfly),
                            "Butterfly",
                            ColorButterfly,
                            ColorButterfly,
                            ColorButterfly.copy(0.3f)
                        )
                        DistanceBlock(
                            rowModifier,
                            mixed,
                            usefulDistance + mixed.intValue,
                            totalDistance,
                            poolLength,
                            stringResource(R.string.label_mixed),
                            "Mixed",
                            ColorMixStart,
                            ColorMixStart,
                            ColorMixStart.copy(0.3f)
                        )
                        DistanceBlock(
                            rowModifier,
                            kick,
                            usefulDistance + kick.intValue,
                            totalDistance,
                            poolLength,
                            stringResource(R.string.label_kickboard),
                            "KickBoard",
                            ColorKickBoard,
                            ColorKickBoard,
                            ColorKickBoard.copy(0.3f)
                        )
                    }
                }

                Row(
                    Modifier
                        .padding(bottom = 10.dp)
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(vertical = 5.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val context = LocalContext.current
                    val newDistance by remember { derivedStateOf { crawl.intValue + back.intValue + breast.intValue + butterfly.intValue + kick.intValue + mixed.intValue } }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = onClickCancel,
                        shape = MaterialTheme.shapes.large,
                        colors = buttonColors(
                            containerColor = ColorCalItemBg,
                            contentColor = Color.Gray,
                            disabledContainerColor = Color.Transparent,
                        )
                    ) {
                        Text(text = stringResource(R.string.label_cancel))
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        enabled = record.distance == newDistance.toString(),
                        onClick = {
                            val detailRecord = record.copy(
                                crawl = crawl.intValue,
                                backStroke = back.intValue,
                                breastStroke = breast.intValue,
                                butterfly = butterfly.intValue,
                                kickBoard = kick.intValue,
                                mixed = mixed.intValue
                            )
                            CoroutineScope(Dispatchers.Default).launch {
                                onClickDone(
                                    detailRecord
                                )
                            }
                        },
                        shape = MaterialTheme.shapes.large,
                        colors = buttonColors(
                            containerColor = ColorCalItemBg,
                            contentColor = ColorTextButton,
                            disabledContainerColor = Color.Transparent,
                        )
                    ) {
                        Text(text = stringResource(R.string.label_save))
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
 * @param title title(영법)
 * @param titleEng title 영어
 * @param thumbColor Slider Thumb(Handle) 색상
 * @param activeTrackColor Slider 활성화된 Track 색상
 * @param inactiveTrackColor Slider 비활성화된 Track 색상
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistanceBlock(
    modifier: Modifier,
    distance: MutableIntState,
    limitDistance: Int,
    maxDistance: Int,
    poolLength: Int,
    title: String,
    titleEng: String,
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
        // 헤더
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )

                // 거리
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = distance.intValue.toString() + "m",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    color = ColorTextDefault
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 영어 title
                Text(
                    text = titleEng,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                )

                // 구간
                Text(
                    text = pluralStringResource(
                        R.plurals.label_laps_format,
                        distance.intValue / poolLength,
                        distance.intValue / poolLength
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                )
            }
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
                    contentDescription = stringResource(R.string.description_decrease_distance_button),
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
                                        .wrapContentSize(Alignment.Center),
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
                    contentDescription = stringResource(R.string.description_increase_distance_button),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun AppFinishPopup(
    modifier: Modifier,
    visible: Boolean,
    onClickDone: () -> Unit,
    onClickCancel: () -> Unit
) {
    Dimmed(visible, onClickOutside = onClickCancel, dismissOnClickOutside = true)

    AnimatedVisibility(
        visible,
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
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(
                            topStart = 30.dp,
                            topEnd = 30.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .padding(20.dp)
                    .navigationBarsPadding(),
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
                            contentDescription = stringResource(R.string.label_cancel),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.popup_message_ask_exit_soodal),
                        fontSize = 20.sp,
                        color = ColorTextDefault
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = { onClickDone() }) {
                    Text(text = stringResource(R.string.popup_label_exit))
                }
            }
        }
    }
}

/**
 * Dimmed
 *
 * @param visible Dimmed 표시 여부
 * @param onClickOutside Dimmed 영역 클릭 시 실행할 함수
 * @param dismissOnClickOutside Dimmed 영역 클릭 시 함수 실행 여부
 * @param changeStatusBarOnDimmed Dimmed 표시 시 StatusBar 색상 변경 여부
 */
@Composable
fun Dimmed(
    visible: Boolean,
    onClickOutside: () -> Unit = {},
    dismissOnClickOutside: Boolean = false,
    changeStatusBarOnDimmed: Boolean = true,
) {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    // StatusBar 색상 변경
    if (changeStatusBarOnDimmed && window != null) {
        DisposableEffect(visible) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            var originalIsLightStatusBar: Boolean? = null

            if (visible) {
                originalIsLightStatusBar = insetsController.isAppearanceLightStatusBars
                insetsController.isAppearanceLightStatusBars = false
            }

            onDispose {
                originalIsLightStatusBar?.let {
                    insetsController.isAppearanceLightStatusBars = it
                }
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = dismissOnClickOutside,
                    onClick = onClickOutside
                )
        )
    }
}

@Composable
@Preview
fun ModifyRecordPopupPreview() {
    val detailRecord = DetailRecord(
        id = "ididid",
        startTime = Instant.now().minusSeconds(3600),
        endTime = Instant.now().minusSeconds(600),
        activeTime = "PT50M",
        distance = "1000",
        energyBurned = "436",
        minHeartRate = 86L,
        maxHeartRate = 182L,
        avgHeartRate = 120L,
        poolLength = 25,
        crawl = 300,
        backStroke = 200,
        breastStroke = 400,
        butterfly = 50,
        kickBoard = 50,
        mixed = 0
    )

    ModifyRecordPopup(
        modifier = Modifier
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
        visible = true,
        onClickDone = {},
        onClickCancel = {},
        record = detailRecord
    )
}

@Composable
@Preview
fun DistanceRowPreview() {
    val distance = remember { mutableIntStateOf(350) }
    Column {
        DistanceBlock(modifier = Modifier.fillMaxWidth(), distance, 800, 1025, 25, "sds", "Asd")
    }
}