package kr.ilf.kshoong.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import kr.ilf.kshoong.Destination
import kr.ilf.kshoong.viewmodel.PopupUiState
import kr.ilf.kshoong.viewmodel.SwimmingViewModel

@Composable
fun PopupView(modifier: Modifier, viewModel: SwimmingViewModel, navController: NavHostController) {
    Box(modifier = modifier) {
        ModifyRecordPopup(
            modifier = Modifier.align(Alignment.BottomEnd),
            viewModel.popupUiState.value == PopupUiState.MODIFY,
            {
                viewModel.popupUiState.value = PopupUiState.NONE
                navController.navigate(Destination.Calendar.route) {
                    launchSingleTop = true
                    popUpTo(navController.currentDestination?.route!!) {
                        inclusive = true
                    }
                }
            },
            {
                viewModel.popupUiState.value = PopupUiState.NONE
                navController.navigate(Destination.Home.route) {
                    launchSingleTop = true
                    popUpTo(navController.currentDestination?.route!!) {
                        inclusive = true
                    }
                }
            })
    }
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
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
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