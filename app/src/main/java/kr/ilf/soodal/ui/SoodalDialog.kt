package kr.ilf.soodal.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * 공통 Dialog
 *
 * @param isVisible Dialog 표시 여부
 * @param title Dialog 제목
 * @param text Dialog 내용
 * @param confirmText confirm 버튼 텍스트
 * @param dismissText dismiss 버튼 텍스트
 * @param onDismissRequest dismiss 콜백
 * @param onConfirm confirm 클릭 콜백
 */
@Composable
fun SoodalDialog(
    isVisible: Boolean = false,
    title: String,
    text: String,
    dismissText: String,
    confirmText: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                    }
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(dismissText)
                }
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = text)
            }
        )
    }
}