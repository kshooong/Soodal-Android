package kr.ilf.kshoong.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.ilf.kshoong.Destination
import kr.ilf.kshoong.NoRippleInteractionSource
import kr.ilf.kshoong.R
import kr.ilf.kshoong.ui.theme.ColorBottomBar
import kr.ilf.kshoong.ui.theme.ColorBottomBarButton
import kr.ilf.kshoong.ui.theme.ColorBottomBarButtonActive

@Composable
fun BottomBarView(
    modifier: Modifier,
    currentDestination: State<String?>,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onShopClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    Box(modifier) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            BottomBarButton(
                modifier = Modifier
                    .size(34.dp),
                "마이룸",
                onClick = onHomeClick,
                isActivated = Destination.Home.route == currentDestination.value,
                R.drawable.ic_home,
                R.drawable.ic_home_active
            )

            BottomBarButton(
                modifier = Modifier
                    .size(34.dp),
                "캘린더",
                onClick = onCalendarClick,
                isActivated = Destination.Calendar.route == currentDestination.value,
                R.drawable.ic_calendar,
                R.drawable.ic_calendar_active
            )

            BottomBarButton(
                modifier = Modifier
                    .size(34.dp),
                "상점",
                onClick = onShopClick,
                isActivated = Destination.Shop.route == currentDestination.value,
                R.drawable.ic_shop,
                R.drawable.ic_shop_active
            )

            BottomBarButton(
                modifier = Modifier
                    .size(34.dp),
                "설정",
                onClick = onSettingClick,
                isActivated = Destination.Setting.route == currentDestination.value,
                R.drawable.ic_setting,
                R.drawable.ic_setting_active
            )
        }
    }
}

@Composable
private fun BottomBarButton(
    modifier: Modifier,
    title: String,
    onClick: () -> Unit,
    isActivated: Boolean = false,
    imageResource: Int,
    imageResourceActive: Int
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RectangleShape,
        contentPadding = PaddingValues(),
        enabled = isActivated.not(),
        colors = ButtonColors(
            Color.Transparent,
            ColorBottomBarButton,
            Color.Transparent,
            ColorBottomBarButtonActive
        ),
        interactionSource = NoRippleInteractionSource()
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally){
            Icon(
                imageVector = ImageVector.vectorResource(id = if (isActivated) imageResourceActive else imageResource),
                contentDescription = "home",
                modifier = Modifier.size(19.dp),
            )
            Text(text = title, style = MaterialTheme.typography.labelSmall)
        }
    }

}


@Preview()
@Composable
fun BottomBarViewPreview() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        BottomBarView(modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(ColorBottomBar),
            remember { mutableStateOf("calendar") },
            onHomeClick = {},
            onCalendarClick = {},
            onShopClick = {},
            onSettingClick = {})// 상단 공간 확보
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.Transparent)
        ) {
            // 하단 메뉴 아이템들
        }
    }
}