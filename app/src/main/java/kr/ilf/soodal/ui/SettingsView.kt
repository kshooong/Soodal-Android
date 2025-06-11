package kr.ilf.soodal.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kr.ilf.soodal.BuildConfig
import kr.ilf.soodal.R
import kr.ilf.soodal.SoodalApplication
import kr.ilf.soodal.viewmodel.SettingsViewModel
import kr.ilf.soodal.viewmodel.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            (LocalContext.current.applicationContext as SoodalApplication).settingsRepository
        )
    )
) {
    val context = LocalContext.current
    val notificationPermissionDialogVisible by viewModel.notificationPermissionDialogVisible

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_label_settings)) },
                navigationIcon = {
                    // msms onClickBack 파라미터로 변경
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val openSourceLicensesStr = stringResource(R.string.settings_label_open_source_licences)

        OssLicensesMenuActivity.setActivityTitle(openSourceLicensesStr)

        val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
        val newSessionNotificationsEnabled by viewModel.newSessionNotificationsEnabled.collectAsState()

        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 알림
            item {
                val permissionLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                        if (granted) {
                            viewModel.setNotificationEnabled(true)
                        } else {
                            viewModel.setNotificationPermissionDialogVisible(true)
                        }
                    }

                val onCheckedChanged = remember {
                    onCheckedChanged@{ checked: Boolean ->
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val permission = Manifest.permission.POST_NOTIFICATIONS
                            val permissionGranted =
                                context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

                            if (!permissionGranted) {
                                permissionLauncher.launch(permission)

                                return@onCheckedChanged
                            }
                        }

                        viewModel.setNotificationEnabled(checked)
                    }
                }

                SwitchSettingItem(
                    title = stringResource(R.string.settings_label_notification),
                    checked = notificationsEnabled,
                    onClick = { checked -> onCheckedChanged(!checked) },
                    onCheckedChanged = onCheckedChanged
                )
            }

            // 새 기록 알림
            item {
                val onCheckedChanged = remember {
                   { checked: Boolean ->
                        viewModel.setNewSessionNotificationsEnabled(checked)
                    }
                }

                Spacer(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color.Gray)
                )

                SwitchSettingItem(
                    title = stringResource(R.string.settings_label_new_session_notification),
                    checked = newSessionNotificationsEnabled,
                    onClick = { checked ->
                        onCheckedChanged(!checked)
                    },
                    onCheckedChanged = onCheckedChanged
                )
            }

            // 앱 버전
            item {
                Spacer(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color.Gray)
                )

                TextSettingItem(
                    title = stringResource(R.string.settings_label_app_version),
                    subtitle = BuildConfig.VERSION_NAME,
                    onClick = { }
                )
            }

            // 오픈소스 라이선스
            item {
                Spacer(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color.Gray)
                )

                TextSettingItem(
                    title = openSourceLicensesStr,
                    subtitle = "",
                    onClick = {
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    }
                )
            }
        }
    }

    SoodalDialog(
        isVisible = notificationPermissionDialogVisible,
        title = stringResource(R.string.app_name),
        text = stringResource(R.string.dialog_message_notifications_permission_required),
        confirmText = stringResource(R.string.label_setting),
        dismissText = stringResource(R.string.label_cancel),
        onDismissRequest = { viewModel.setNotificationPermissionDialogVisible(false) }
    ) {
        context.startActivity(getAppNotificationSettingIntent(context))
        viewModel.setNotificationPermissionDialogVisible(false)
    }
}

/**
 * 스위치를 포함한 설정
 *
 * @param title 설정 이름
 * @param checked 설정 상태
 * @param onClick 설정 클릭 시 실행할 콜백
 * @param onCheckedChanged 스위치 상태 변경(스위치 클릭) 시 실행할 콜백
 */
@Composable
fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    onClick: (Boolean) -> Unit,
    onCheckedChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(checked) } // 클릭 시 현재 상태 이용 가능성을 위해 not() 하지않음
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChanged)
    }
}

@Composable
fun TextSettingItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * 앱의 설정 화면으로 이동하는 Intent 생성
 * 사용자가 "다시 묻지 않음"을 선택한 후 권한을 허용하도록 유도할 때 사용합니다.
 *
 * @param context Context
 * @return 앱 설정 화면으로 가는 Intent
 */
private fun getAppSettingsIntent(context: Context): Intent {
    return Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
}

/**
 * 앱의 알림 설정 화면으로 이동하는 Intent 생성
 *
 * @param context Context
 * @return 앱 알림 설정 화면으로 가는 Intent
 */
private fun getAppNotificationSettingIntent(context: Context): Intent {
    return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
}