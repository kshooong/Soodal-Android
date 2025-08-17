package kr.ilf.soodal.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kr.ilf.soodal.R
import kr.ilf.soodal.database.entity.DetailRecordWithHR
import kr.ilf.soodal.ui.theme.ColorBackStroke
import kr.ilf.soodal.ui.theme.ColorBreastStroke
import kr.ilf.soodal.ui.theme.ColorButterfly
import kr.ilf.soodal.ui.theme.ColorCrawl
import kr.ilf.soodal.ui.theme.ColorKickBoard
import kr.ilf.soodal.ui.theme.ColorMixEnd
import kr.ilf.soodal.ui.theme.ColorMixStart
import kr.ilf.soodal.ui.theme.ColorTextDefault
import kr.ilf.soodal.ui.theme.SkyBlue6
import kotlin.math.max
import kotlin.time.Duration

@Composable
fun DetailDataView(
    detailRecordWithHR: DetailRecordWithHR,
    isDetail: Boolean = false
) {
    val detailRecord = detailRecordWithHR.detailRecord

    val activeTime = detailRecord.activeTime?.let { Duration.parse(it) } ?: Duration.ZERO
    val calories = detailRecord.energyBurned?.toDouble() ?: 0.0
    val maxHR = detailRecord.maxHeartRate?.toInt() ?: 0
    val minHR = detailRecord.minHeartRate?.toInt() ?: 0

    val animationSpec = spring(
        visibilityThreshold = Int.VisibilityThreshold, stiffness = Spring.StiffnessLow
    )
    val animatedCrawl by animateIntAsState(detailRecord.crawl, animationSpec)
    val animatedBackStroke by animateIntAsState(detailRecord.backStroke, animationSpec)
    val animatedBreastStroke by animateIntAsState(detailRecord.breastStroke, animationSpec)
    val animatedButterfly by animateIntAsState(detailRecord.butterfly, animationSpec)
    val animatedKickBoard by animateIntAsState(detailRecord.kickBoard, animationSpec)
    val animatedMixed by animateIntAsState(detailRecord.mixed, animationSpec)

    val distanceList = listOf(
        Triple(detailRecord.crawl, animatedCrawl, SolidColor(ColorCrawl)),
        Triple(detailRecord.backStroke, animatedBackStroke, SolidColor(ColorBackStroke)),
        Triple(
            detailRecord.breastStroke,
            animatedBreastStroke,
            SolidColor(ColorBreastStroke)
        ),
        Triple(detailRecord.butterfly, animatedButterfly, SolidColor(ColorButterfly)),
        Triple(
            detailRecord.mixed, animatedMixed, Brush.verticalGradient(
                Pair(0f, ColorMixStart),
                Pair(1f, ColorMixEnd)
            )
        ),
        Triple(detailRecord.kickBoard, animatedKickBoard, SolidColor(ColorKickBoard))
    )

    Column {
        Row(
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.calendar_label_duration),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
                Text(activeTime.toCustomTimeString(), color = ColorTextDefault)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.calendar_label_max_heart_rate),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
                Text(maxHR.toString() + "bpm", color = ColorTextDefault)
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.calendar_label_calories),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
                Text(calories.toInt().toString() + "kcal", color = ColorTextDefault)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.calendar_label_min_heart_rate),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
                Text(minHR.toString() + "bpm", color = ColorTextDefault)
            }
        }
    }

    Column(
        Modifier
            .padding(top = 5.dp)
            .fillMaxWidth()
            .background(
                SkyBlue6, shape = RoundedCornerShape(15.dp)
            )
            .padding(8.dp)
    ) {
        var refValue by remember { mutableIntStateOf(1000) }
        val animatedRefVal by animateIntAsState(
            refValue, spring(
                visibilityThreshold = Int.VisibilityThreshold,
                stiffness = 200f
            )
        )

        distanceList.filter {
            it.first != 0
        }.sortedByDescending {
            it.first
        }.forEachIndexed { i, it ->
            if (i == 0) refValue = max(1000, it.first)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .height(30.dp)
                        .fillMaxWidth(it.second / animatedRefVal.toFloat())
                        .background(
                            it.third,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(end = 7.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (it.first >= 75) Text(
                        it.second.toString(),
                        lineHeight = 14.dp.toSp,
                        fontSize = 14.dp.toSp,
                        color = ColorTextDefault
                    )
                }

                if (it.first < 75) Text(
                    it.second.toString(),
                    lineHeight = 14.dp.toSp,
                    fontSize = 14.dp.toSp,
                    color = ColorTextDefault
                )
            }
        }
    }
}
