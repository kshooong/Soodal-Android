package kr.ilf.soodal.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kr.ilf.soodal.BuildConfig
import kr.ilf.soodal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current as Activity
        val openSourceLicensesStr = stringResource(R.string.settings_label_open_source_licences)

        OssLicensesMenuActivity.setActivityTitle(openSourceLicensesStr)

        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                SwitchSettingItem(
                    title = "Enable Notifications",
                    checked = true, // 실제로는 ViewModel에서 상태 관리
                    onCheckedChanged = { /* ViewModel 업데이트 */ }
                )
            }
            item {
                TextSettingItem(
                    title = stringResource(R.string.settings_label_app_version),
                    subtitle = BuildConfig.VERSION_NAME,
                    onClick = { /* 버전 정보 다이얼로그 등 */ }
                )
            }
            item {
                TextSettingItem(
                    title = openSourceLicensesStr,
                    subtitle = "",
                    onClick = {
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))}
                )
            }
        }
    }
}

@Composable
fun SwitchSettingItem(title: String, checked: Boolean, onCheckedChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChanged(!checked) }
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