package kr.ilf.kshoong.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.ilf.kshoong.R

@Composable
fun BottomBarView(modifier: Modifier, onCalenderClick: () -> Unit, onDetailClick: () -> Unit) {
    Box(modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .padding(top = 30.dp),
            shadowElevation = 10.dp,
            color = Color.White,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {

            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier
                        .size(40.dp),
                    onClick = { onCalenderClick() },
                    shape = RectangleShape,
                    contentPadding = PaddingValues(),
                    colors = ButtonColors(Color.White, Color.White, Color.White, Color.White)
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.union),
                        contentDescription = ""
                    )
                }

                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight()
                        .background(Color.Transparent)
                )

                Button(
                    modifier = Modifier
                        .size(40.dp),
                    onClick = { onDetailClick() },
                    shape = RectangleShape,
                    contentPadding = PaddingValues(),
                    colors = ButtonColors(Color.White, Color.White, Color.White, Color.White)
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.union),
                        contentDescription = ""
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .size(80.dp)
                .align(Alignment.Center),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 5.dp
        ) {
            Button(
                modifier = Modifier
                    .padding(5.dp)
                    .size(70.dp)
                    .background(
                        Brush.verticalGradient(
                            Pair(0f, Color(0xFF86F4DA)),
                            (Pair(1f, Color(0xFFA2E3FF)))
                        ), shape = CircleShape
                    )
                    .align(Alignment.Center),
                onClick = { onCalenderClick() },
                shape = CircleShape,
                contentPadding = PaddingValues(),
                colors = ButtonColors(Color.Transparent, Color.White, Color.White, Color.White),
            ) {}
        }
    }
}

@Preview()
@Composable
fun BottomBarViewPreview() {

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Spacer(modifier = Modifier.weight(1f))
        BottomBarView(modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .background(Color.White), onCalenderClick = {}, onDetailClick = {})// 상단 공간 확보
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