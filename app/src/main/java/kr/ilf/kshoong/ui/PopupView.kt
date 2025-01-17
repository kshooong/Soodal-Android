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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kr.ilf.kshoong.Destination
import kr.ilf.kshoong.viewmodel.PopupUiState
import kr.ilf.kshoong.viewmodel.SwimmingViewModel

@Composable
fun PopupView(modifier: Modifier, viewModel: SwimmingViewModel, navController: NavHostController) {
        ModifyRecordPopup(
            modifier = modifier
                .shadow(5.dp, shape = RoundedCornerShape(30.dp))
                .scrollable(rememberScrollState(), Orientation.Vertical)
                .background(Color.White, shape = RoundedCornerShape(30.dp)),
            visible = viewModel.popupUiState.value == PopupUiState.MODIFY,
            onClickDone = {
                viewModel.popupUiState.value = PopupUiState.NONE
                navController.navigate(Destination.Calendar.route) {
                    launchSingleTop = true
                }
            },
            onClickCancel = {
                viewModel.popupUiState.value = PopupUiState.NONE
            })
}

@Composable
fun ModifyRecordPopup(
    modifier: Modifier,
    visible: Boolean,
    onClickDone: () -> Unit,
    onClickCancel: () -> Unit
) {
    AnimatedVisibility(
        visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = modifier
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            ) {
                Text(text = "수정창")
                Button(onClick = onClickDone) {
                    Text(text = "저장")
                }
                Button(onClick = onClickCancel) {
                    Text(text = "취소")
                }
            }
        }
    }
}