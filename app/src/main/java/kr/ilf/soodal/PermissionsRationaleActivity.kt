package kr.ilf.soodal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.ilf.soodal.ui.theme.SoodalTheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoodalTheme {
                PrivacyPolicyScreen(Modifier.navigationBarsPadding().statusBarsPadding())
            }
        }
    }
}

/**
 * 개인정보 처리방침 전체 화면
 */
@Composable
fun PrivacyPolicyScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState) // 스크롤 가능하게 만듭니다.
        ) {
            // 상단 여백
            Spacer(modifier = Modifier.height(24.dp))

            // 메인 타이틀
            Text(
                text = "개인정보 처리방침",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Soodal 앱의 개인정보 처리방침입니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 각 항목을 별도의 Composable로 분리하여 재사용 및 가독성을 높입니다.
            PolicyItem(
                title = "1. 개인정보의 처리 목적",
                content = "회사는 다음의 목적을 위해 헬스커넥트를 통해 개인정보를 수집 및 처리합니다.\n" +
                        "• 건강 데이터 기반 서비스 제공: 헬스커넥트로부터 연동된 수영 활동 데이터(수영 거리, 시간 등)를 앱 내 화면에 표시하여 사용자의 운동 기록을 확인하고 관리할 수 있도록 지원합니다.\n\n" +
                        "수집된 정보는 오직 사용자 기기의 내부 저장소에만 저장되며, 서비스 제공 목적 이외의 용도로는 사용되지 않습니다."
            )
            PolicyItem(
                title = "2. 처리하는 개인정보의 항목",
                content = "회사는 서비스 제공을 위해 다음 항목의 정보를 헬스커넥트로부터 연동 받아 처리합니다.\n" +
                        "• 건강 및 신체 활동 정보: 수영 거리, 운동 시간, 칼로리 소모량 등 (단, 이 정보는 사용자의 기기에만 저장되며 서버로 전송되지 않습니다)"
            )
            PolicyItem(
                title = "3. 개인정보의 처리 및 보유 기간",
                content = "사용자의 건강 및 위치 정보는 앱이 설치된 사용자 기기의 내부 저장소에만 보유되며, 사용자가 앱을 삭제하거나 데이터를 직접 삭제하기 전까지 보관됩니다. 데이터는 보유 기간 경과 또는 처리 목적 달성 시 지체 없이 파기됩니다."
            )
            PolicyItem(
                title = "4. 개인정보의 제3자 제공 및 위탁",
                content = "회사는 사용자의 개인정보를 제3자에게 제공하거나 외부에 위탁하지 않습니다. 수집된 모든 정보는 사용자 기기에만 머무릅니다."
            )
            PolicyItem(
                title = "5. 개인정보의 파기 절차 및 파기 방법",
                content = "사용자가 앱에서 데이터를 삭제하거나 앱을 제거할 때, 저장소에 저장된 개인정보는 복구할 수 없는 방식으로 영구 파기됩니다."
            )
            PolicyItem(
                title = "6. 정보주체와 법정대리인의 권리·의무 및 그 행사방법",
                content = "사용자(정보주체)는 언제든지 본인의 개인정보를 열람, 정정, 삭제할 수 있으며, 헬스커넥트 앱 설정에서 언제든지 본 앱의 데이터 접근 권한을 철회할 수 있습니다."
            )
            PolicyItem(
                title = "7. 개인정보 처리방침 변경에 관한 사항", // 원본 텍스트에 7번이 없었으나 순서를 맞추기 위해 추가했습니다.
                content = "이 개인정보 처리방침은 관련 법령 및 지침의 변경 또는 회사 정책의 변경에 따라 변경될 수 있으며, 변경 시 앱 내 공지사항 등을 통해 고지합니다."
            )

            // 하단 여백
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 제목과 내용을 표시하는 재사용 가능한 항목 Composable
 * @param title 항목의 제목
 * @param content 항목의 내용
 */
@Composable
fun PolicyItem(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp // 글자 크기를 약간 조정하여 가독성 향상
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp // 줄 간격을 넓혀 읽기 편하게 만듭니다.
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SoodalTheme {
        PrivacyPolicyScreen()
    }
}