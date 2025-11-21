package kr.ilf.soodal.ui

import android.content.Context
import android.widget.TextView
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import kr.ilf.soodal.R
import kr.ilf.soodal.database.entity.DetailRecordWithHR
import kr.ilf.soodal.database.entity.HeartRateSample
import kr.ilf.soodal.ui.theme.ColorBackStroke
import kr.ilf.soodal.ui.theme.ColorBreastStroke
import kr.ilf.soodal.ui.theme.ColorButterfly
import kr.ilf.soodal.ui.theme.ColorCrawl
import kr.ilf.soodal.ui.theme.ColorKickBoard
import kr.ilf.soodal.ui.theme.ColorMixEnd
import kr.ilf.soodal.ui.theme.ColorMixStart
import kr.ilf.soodal.ui.theme.ColorTextDefault
import kr.ilf.soodal.ui.theme.SkyBlue6
import java.time.Instant
import java.time.temporal.ChronoUnit
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
        val refValue = remember(distanceList) {
            val maxValue = distanceList
                .filter { it.first != 0 }
                .maxOfOrNull { it.first } ?: 1000
            max(1000, maxValue)
        }

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

    if (isDetail && detailRecordWithHR.heartRateSamples.size > 1) {
        HeartRateGraphView(
            detailRecordWithHR.detailRecord.startTime,
            detailRecordWithHR.detailRecord.endTime,
            detailRecordWithHR.heartRateSamples
        )
    }
}

/*@Composable
private fun HeartRateGraphView(startTime: Instant, endTime: Instant, hrSample: List<HeartRateSample>) {
    val entries = mutableListOf<Entry>()
    hrSample.forEach {
        val time = it.time.minusMillis(startTime.toEpochMilli())
        entries.add(Entry(time.epochSecond.toFloat(), it.beatsPerMinute.toFloat()))
    }

    val max = endTime.minusMillis(startTime.toEpochMilli()).epochSecond

    val yValues = entries.map { it.y }
    val maxY = yValues.max()
    val minY = yValues.min()


    val dataSet = LineDataSet(entries, "hr").apply {
        mode = LineDataSet.Mode.CUBIC_BEZIER
        setDrawCircles(false)
        setDrawValues(true)
        lineWidth = 1.5f

        valueFormatter = object : ValueFormatter() {
            override fun getPointLabel(entry: Entry?): String {
                return if (entry != null && (entry.y == maxY || entry.y == minY)) {
                    entry.y.toInt().toString() // 정수로 표시
                } else {
                    "" // 그 외에는 표시 안 함
                }
            }
        }
    }

    val lineData = LineData(dataSet)

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        factory = { context ->
            LineChart(context).apply {
                data = lineData
                description.isEnabled = false
                isDragEnabled = false
                isDoubleTapToZoomEnabled = false
                isScaleXEnabled = false
                isScaleYEnabled = false
                setPinchZoom(false)

                setDrawGridBackground(false)
                legend.isEnabled = false

                axisLeft.isEnabled = false
                axisRight.axisMaximum = (maxY + 5).coerceAtLeast(0f)
                axisRight.axisMinimum = minY - 5
                axisRight.setLabelCount(3, true)

                xAxis.axisMinimum = 0f
                xAxis.isGranularityEnabled = true
                xAxis.granularity = 600f
                xAxis.setLabelCount((max/600f).toInt(), true)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.valueFormatter = object : ValueFormatter() {

//                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
//                        val minutes = TimeUnit.SECONDS.toMinutes(value.toLong())
//                        if (minutes % 5 == 0L) {
//                            return "${minutes}m"
//                        }
//                        return ""
//                    }
                }
            }
        },
        update = {
            it.data = lineData
            it.notifyDataSetChanged()
            it.invalidate()
        }
    )
}*/


// 분 단위로 집계된 데이터
data class MinuteBucket(
    val minute: Long,
    val minHR: Int,
    val maxHR: Int,
    val avgHR: Int
)

private fun aggregateToMinuteBuckets(
    startTime: Instant,
    samples: List<HeartRateSample>
): List<MinuteBucket> {
    return samples
        .groupBy { ChronoUnit.MINUTES.between(startTime, it.time) }
        .map { (minute, list) ->
            val min = list.minOf { it.beatsPerMinute }
            val max = list.maxOf { it.beatsPerMinute }
            val avg = list.map { it.beatsPerMinute }.average().toInt()
            MinuteBucket(minute, min, max, avg)
        }
        .sortedBy { it.minute }
}

// X축 레이블 간격 결정
private fun labelInterval(totalMinutes: Long): Int {
    var interval = 5
    while (totalMinutes / interval > 6) {
        interval += 5
    }
    return interval
}

// 차트 터치 시 마커뷰
class HRMarkerView(context: Context, private val buckets: List<MinuteBucket>) :
    MarkerView(context, android.R.layout.simple_list_item_1) {
    private val tv: TextView = findViewById(android.R.id.text1)

    override fun refreshContent(
        e: Entry?,
        highlight: com.github.mikephil.charting.highlight.Highlight?
    ) {
        e?.let {
            val minute = it.x.toLong()
            val bucket = buckets.find { b -> b.minute == minute }
            if (bucket != null) {
                tv.text =
                    "Time: ${minute}m\nMin: ${bucket.minHR}\nMax: ${bucket.maxHR}\nAvg: ${bucket.avgHR}"
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}

@Composable
fun HeartRateGraphView(
    startTime: Instant,
    endTime: Instant,
    hrSample: List<HeartRateSample>
) {
    val buckets = remember(hrSample) {
        aggregateToMinuteBuckets(startTime, hrSample)
    }

    // entries
    val maxEntries = buckets.map { Entry(it.minute.toFloat(), it.maxHR.toFloat()) }
    val minEntries = buckets.map { Entry(it.minute.toFloat(), it.minHR.toFloat()) }
    val avgEntries = buckets.map { Entry(it.minute.toFloat(), it.avgHR.toFloat()) }

    // 평균 HR 선
    val avgDataSet = LineDataSet(avgEntries, "avg").apply {
        color = Color.Red.toArgb()
        lineWidth = 2f
        mode = LineDataSet.Mode.CUBIC_BEZIER
        cubicIntensity = 0.15f
        setDrawCircles(false)
        setDrawValues(false)
    }

    // 최대 HR
    val maxDataSet = LineDataSet(maxEntries, "max").apply {
        color = Color.Blue.toArgb()
        mode = LineDataSet.Mode.CUBIC_BEZIER
        lineWidth = 0.1f
        setDrawCircles(false)
        setDrawValues(false)
        setDrawFilled(true)
        cubicIntensity = 0.15f
        fillColor = Color.Blue.copy(alpha = 0.3f).toArgb()
        fillAlpha = 100
    }

    // 최소 HR → 흰색으로 채워서 max 아래 영역 지우기
    val minDataSet = LineDataSet(minEntries, "min").apply {
        color = Color.Blue.toArgb()
        mode = LineDataSet.Mode.CUBIC_BEZIER
        lineWidth = 0.1f
        setDrawCircles(false)
        setDrawValues(false)
        setDrawFilled(true)
        cubicIntensity = 0.10f
        fillColor = Color.White.toArgb()
        fillAlpha = 255
    }

    val lineData = LineData(maxDataSet, minDataSet, avgDataSet)

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                marker = HRMarkerView(context, buckets)

                // Y축
                val allValues = buckets.flatMap { listOf(it.minHR, it.maxHR) }
                val minY = allValues.min()
                val maxY = allValues.max()
                axisLeft.isEnabled = false
                axisRight.apply {
                    axisMinimum = (minY - 5).toFloat()
                    axisMaximum = (maxY + 5).toFloat()
                    setDrawGridLines(false)
                    setLabelCount(3, true)
                }

                // X축
                val totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime)
                val interval = labelInterval(totalMinutes)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    axisMinimum = 0f
                    axisMaximum = totalMinutes.toFloat()
                    granularity = 1f
                    setDrawGridLines(false)
                    valueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            val minutes = value.toLong()
                            return if (minutes % interval == 0L) "${minutes}m" else ""
                        }
                    }
                }

                data = lineData
            }
        },
        update = {
            it.data = lineData
            it.notifyDataSetChanged()
            it.invalidate()
        }
    )
}