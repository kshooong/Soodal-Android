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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kr.ilf.soodal.ui.theme.ColorGroupDivider
import kr.ilf.soodal.ui.theme.ColorItemDivider
import kr.ilf.soodal.viewmodel.SettingsViewModel
import kr.ilf.soodal.viewmodel.SettingsViewModelFactory

// msms lazy필요?, setting 클래스 만들어서 forEach로 변경?, 알림 권한 확인 후 알림 비활성화 로직 추가
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
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

                ItemDivider()

                SwitchSettingItem(
                    enabled = notificationsEnabled,
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
                GroupDivider()

                TextSettingItem(
                    title = stringResource(R.string.settings_label_app_version),
                    subtitle = BuildConfig.VERSION_NAME,
                )
            }

            // 오픈소스 라이선스
            item {
                ItemDivider()

                TextSettingItem(
                    title = openSourceLicensesStr,
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
 * @param enabled 설정 활성화 여부
 * @param title 설정 이름
 * @param checked 설정 상태
 * @param onClick 설정 클릭 시 실행할 콜백
 * @param onCheckedChanged 스위치 상태 변경(스위치 클릭) 시 실행할 콜백
 */
@Composable
private fun SwitchSettingItem(
    enabled: Boolean = true,
    title: String,
    checked: Boolean,
    onClick: (Boolean) -> Unit,
    onCheckedChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(enabled = enabled) { onClick(checked) } // 클릭 시 현재 상태 이용 가능성을 위해 not() 하지않음
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val alpha = if (enabled) 1f else 0.38f

        Text(text = title, modifier = Modifier.alpha(alpha))
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChanged,
            modifier = Modifier.height(60.dp)
        )
    }
}

@Composable
private fun TextSettingItem(
    enabled: Boolean = true,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    val alpha = if (enabled) 1f else 0.38f
    val clickable = enabled && onClick != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(enabled = clickable, onClick = onClick ?: {})
            .padding(horizontal = 16.dp)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title)
            subtitle?.let {
                Text(
                    subtitle,
                    modifier = Modifier.padding(start = 1.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ItemDivider() {
    HorizontalDivider(
        Modifier
            .padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = ColorItemDivider
    )
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = ColorGroupDivider
    )
}

/**
 * 앱의 설정 화면으로 이동하는 Intent 생성
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