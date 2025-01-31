package kr.ilf.kshoong.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kr.ilf.kshoong.database.entity.DetailRecord
import kr.ilf.kshoong.viewmodel.PopupUiState
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PopupView(modifier: Modifier, viewModel: SwimmingViewModel, navController: NavHostController) {
    val context = LocalContext.current

    ModifyRecordPopup(
        modifier = modifier
            .shadow(5.dp, shape = RoundedCornerShape(30.dp))
            .scrollable(rememberScrollState(), Orientation.Vertical)
            .background(Color.White, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .padding(10.dp),
        visible = viewModel.popupUiState.value == PopupUiState.MODIFY,
        onClickDone = { record ->
            viewModel.updateDetailRecord(record)

            viewModel.popupUiState.value = PopupUiState.NONE
//            navController.navigate(Destination.Calendar.route) {
//                launchSingleTop = true
//            }
        },
        onClickCancel = {
            viewModel.popupUiState.value = PopupUiState.NONE
        },
        viewModel.currentModifyRecord.collectAsState().value
    )

    AppFinishPopup(
        modifier = modifier,
        visible = viewModel.popupUiState.value == PopupUiState.APP_FINISH,
        onClickDone = { (context as Activity).finish() },
        onClickCancel = { viewModel.popupUiState.value = PopupUiState.NONE },
        null
    )
}

@Composable
fun ModifyRecordPopup(
    modifier: Modifier,
    visible: Boolean,
    onClickDone: (DetailRecord) -> Unit,
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
                val textModifier = Modifier.width(50.dp)
                val textFieldModifier = Modifier.width(200.dp)
                val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
                val startTime = record.startTime.atZone(ZoneId.systemDefault())
                    .format(formatter)
                val endTime =
                    record.endTime.atZone(ZoneId.systemDefault()).format(formatter)

                val crawl = remember { mutableIntStateOf(record.crawl) }
                val back = remember { mutableIntStateOf(record.backStroke) }
                val breast = remember { mutableIntStateOf(record.breastStroke) }
                val butterfly = remember { mutableIntStateOf(record.butterfly) }
                val kick = remember { mutableIntStateOf(record.kickBoard) }
                val mixed = remember { mutableIntStateOf(record.mixed) }

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {

                    Column(
                        rowModifier
                    ) {
                        Text(text = "수영 시간 $startTime ~ $endTime")
                        Text(text = "총 거리 ${record.distance}")

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
                            onClickDone(detailRecord)
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
fun AppFinishPopup(
    modifier: Modifier,
    visible: Boolean,
    onClickDone: () -> Unit,
    onClickCancel: () -> Unit,
    record: DetailRecord?
) {
    // dimmed
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .background(Color.Black.copy(0.2f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onClickCancel() }
        )
    }

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
                    .shadow(8.dp, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
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
                    Button(
                        modifier = Modifier.size(20.dp),
                        onClick = { onClickCancel() },
                        contentPadding = PaddingValues()
                    ) {
                        Text(text = "X", fontSize = 14.sp, lineHeight = 14.sp)
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