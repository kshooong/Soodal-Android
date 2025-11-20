package kr.ilf.soodal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.ilf.soodal.Destination
import kr.ilf.soodal.R
import kr.ilf.soodal.ui.theme.ColorBottomBar
import kr.ilf.soodal.ui.theme.ColorBottomBarButton
import kr.ilf.soodal.ui.theme.ColorBottomBarButtonActive

@Composable
fun BottomBarView(
    modifier: Modifier,
    currentDestination: State<String?>,
    onCalendarClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    Box(modifier) {
        Row(
            modifier = Modifier.navigationBarsPadding().then(modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val buttonModifier = Modifier
                .padding(horizontal = 2.5.dp)
                .height(60.dp)
                .weight(1f)

            BottomBarButton(
                modifier = buttonModifier,
                stringResource(R.string.bottom_label_calendar),
                stringResource(R.string.bottom_description_calendar_bottom_menu),
                onClick = onCalendarClick,
                isActivated = Destination.Calendar.route == currentDestination.value,
                R.drawable.ic_calendar,
                R.drawable.ic_calendar_active
            )

            BottomBarButton(
                modifier = buttonModifier,
                stringResource(R.string.bottom_label_settings),
                stringResource(R.string.bottom_description_settings_bottom_menu),
                onClick = onSettingClick,
                isActivated = Destination.Settings.route == currentDestination.value,
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
    description: String,
    onClick: () -> Unit,
    isActivated: Boolean = false,
    imageResource: Int,
    imageResourceActive: Int,

    ) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
//        shape = RectangleShape,
//        contentPadding = PaddingValues(),
        enabled = isActivated.not(),
        colors = IconButtonColors(
            Color.Transparent,
            ColorBottomBarButton,
            Color.Transparent,
            ColorBottomBarButtonActive
        ),
//        interactionSource = NoRippleInteractionSource()
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(imageResource),
//                imageVector = ImageVector.vectorResource(id = if (isActivated) imageResourceActive else imageResource),
                contentDescription = description,
                modifier = Modifier.size(19.dp),
                tint = if (isActivated) ColorBottomBarButtonActive else ColorBottomBarButton
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
            onCalendarClick = {},
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