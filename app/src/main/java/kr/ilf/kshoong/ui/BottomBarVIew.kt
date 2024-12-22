package kr.ilf.kshoong.ui

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.ilf.kshoong.R

@Composable
fun BottomBarView(modifier: Modifier, onCalenderClick: () -> Unit, onDetailClick: () -> Unit) {
    Box(modifier) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                modifier = Modifier
                    .size(30.dp),
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

            Button(
                modifier = Modifier
                    .size(30.dp),
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

            Button(
                modifier = Modifier
                    .size(30.dp),
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