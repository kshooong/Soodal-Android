package kr.ilf.kshoong.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kr.ilf.kshoong.Destination
import kr.ilf.kshoong.database.entity.DetailRecordWithHeartRateSample
import kr.ilf.kshoong.viewmodel.PopupUiState
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.sin

@Composable
fun PopupView(modifier: Modifier, viewModel: SwimmingViewModel, navController: NavHostController) {
    ModifyRecordPopup(
        modifier = modifier
            .shadow(5.dp, shape = RoundedCornerShape(30.dp))
            .scrollable(rememberScrollState(), Orientation.Vertical)
            .background(Color.White, shape = RoundedCornerShape(30.dp))
            .padding(10.dp),
        visible = viewModel.popupUiState.value == PopupUiState.MODIFY,
        onClickDone = {
            viewModel.popupUiState.value = PopupUiState.NONE
            navController.navigate(Destination.Calendar.route) {
                launchSingleTop = true
            }
        },
        onClickCancel = {
            viewModel.popupUiState.value = PopupUiState.NONE
        },
        viewModel.currentDetailRecord.collectAsState().value
    )
}

@Composable
fun ModifyRecordPopup(
    modifier: Modifier,
    visible: Boolean,
    onClickDone: () -> Unit,
    onClickCancel: () -> Unit,
    records: List<DetailRecordWithHeartRateSample?>
) {
    AnimatedVisibility(
        visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Column(
            modifier = modifier
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                records.forEach { record ->
                    val rowModifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                    val textModifier = Modifier.width(50.dp)
                    val textFieldModifier = Modifier.width(200.dp)
                    val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
                    val startTime = record!!.detailRecord.startTime.atZone(ZoneId.systemDefault())
                        .format(formatter)
                    val endTime =
                        record.detailRecord.endTime.atZone(ZoneId.systemDefault()).format(formatter)

                    val crawl = remember { mutableIntStateOf(record.detailRecord.crawl) }
                    val back = remember { mutableIntStateOf(record.detailRecord.backStroke) }
                    val breast = remember { mutableIntStateOf(record.detailRecord.breastStroke) }
                    val butterfly = remember { mutableIntStateOf(record.detailRecord.butterfly) }
                    val kick = remember { mutableIntStateOf(record.detailRecord.kickBoard) }
                    val mixed = remember { mutableIntStateOf(record.detailRecord.mixed) }

                    Column(
                        rowModifier
                    ) {
                        Text(text = "수영 시간 $startTime ~ $endTime")
                        Text(text = "총 거리 ${record.detailRecord.distance}")

                        Row(
                            rowModifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = textModifier,
                                text = "자유형",
                                textAlign = TextAlign.Center
                            )
                            TextField(
                                modifier = textFieldModifier,
                                value = crawl.intValue.toString(),
                                onValueChange = {
                                    crawl.intValue = if (it.toIntOrNull() != null) it.toInt() else 0
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        Row(
                            rowModifier
                        ) {
                            Text(modifier = textModifier, text = "배영")
                            TextField(
                                modifier = textFieldModifier,
                                value = back.intValue.toString(),
                                onValueChange = {
                                    back.intValue = if (it.toIntOrNull() != null) it.toInt() else 0
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        Row(
                            rowModifier
                        ) {
                            Text(modifier = textModifier, text = "평영")
                            TextField(
                                modifier = textFieldModifier,
                                value = breast.intValue.toString(),
                                onValueChange = {
                                    breast.intValue =
                                        if (it.toIntOrNull() != null) it.toInt() else 0
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                        }

                        Row(
                            rowModifier
                        ) {
                            Text(modifier = textModifier, text = "접영")
                            TextField(
                                modifier = textFieldModifier,
                                value = butterfly.intValue.toString(),
                                onValueChange = {
                                    butterfly.intValue =
                                        if (it.toIntOrNull() != null) it.toInt() else 0
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        Row(
                            rowModifier
                        ) {
                            Text(modifier = textModifier, text = "혼영")
                            TextField(
                                modifier = textFieldModifier,
                                value = mixed.intValue.toString(),
                                onValueChange = {
                                    mixed.intValue = if (it.toIntOrNull() != null) it.toInt() else 0
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                            )
                        }

                        Row(
                            rowModifier
                        ) {
                            Text(modifier = textModifier, text = "킥판")
                            TextField(
                                modifier = textFieldModifier,
                                value = kick.intValue.toString(),
                                onValueChange = {
                                    kick.intValue = if (it.toIntOrNull() != null) it.toInt() else 0
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            Row(Modifier.align(Alignment.End)) {
                Button(onClick = {

                    onClickDone()
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