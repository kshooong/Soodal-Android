package kr.ilf.kshoong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kr.ilf.kshoong.data.SwimData
import kr.ilf.kshoong.ui.SwimCalendarView
import kr.ilf.kshoong.ui.SwimCalendarView3
import kr.ilf.kshoong.ui.SwimCalendarView4
import kr.ilf.kshoong.ui.theme.KshoongTheme

class MainActivity : ComponentActivity() {
    companion object {
        val data = HashMap<String, SwimData>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDummyData()
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }
        })
        enableEdgeToEdge()
        setContent {
            KshoongTheme {
                SwimCalendarView4(data)
            }
        }
    }

    fun setDummyData() {
        data["2024-10-01-화"] = SwimData("2024-10-01-화", 600, 200, 400, 200, 100)
        data["2024-10-02-수"] = SwimData("2024-10-02-수", 625, 275, 450, 150, 75)
        data["2024-10-03-목"] = SwimData("2024-10-03-목", 500, 300, 375, 225, 100)
        data["2024-10-04-금"] = SwimData("2024-10-04-금", 650, 250, 425, 200, 100)
        data["2024-10-05-토"] = SwimData("2024-10-05-토", 700, 200, 400, 250, 150)
        data["2024-10-06-일"] = SwimData("2024-10-06-일", 800, 300, 450, 300, 200)
        data["2024-10-07-월"] = SwimData("2024-10-07-월", 575, 225, 375, 175, 125)
        data["2024-10-08-화"] = SwimData("2024-10-08-화", 625, 250, 425, 200, 100)
        data["2024-10-09-수"] = SwimData("2024-10-09-수", 500, 200, 350, 150, 75)
        data["2024-10-10-목"] = SwimData("2024-10-10-목", 950, 300, 500, 250, 100)
        data["2024-10-11-금"] = SwimData("2024-10-11-금", 700, 200, 400, 250, 150)
        data["2024-10-14-월"] = SwimData("2024-10-14-월", 600, 250, 450, 200, 100)
        data["2024-10-15-화"] = SwimData("2024-10-15-화", 625, 275, 425, 175, 75)
        data["2024-10-16-수"] = SwimData("2024-10-16-수", 500, 300, 375, 225, 100)
        data["2024-10-17-목"] = SwimData("2024-10-17-목", 650, 250, 400, 200, 100)
        data["2024-10-18-금"] = SwimData("2024-10-18-금", 700, 200, 450, 250, 150)
        data["2024-10-19-토"] = SwimData("2024-10-19-토", 800, 300, 425, 300, 200)
        data["2024-10-20-일"] = SwimData("2024-10-20-일", 575, 225, 375, 175, 125)
        data["2024-10-21-월"] = SwimData("2024-10-21-월", 625, 250, 400, 200, 100)
        data["2024-10-22-화"] = SwimData("2024-10-22-화", 500, 200, 350, 150, 75)
        data["2024-10-23-수"] = SwimData("2024-10-23-수", 950, 300, 500, 250, 100)
        data["2024-10-24-목"] = SwimData("2024-10-24-목", 700, 200, 400, 250, 150)
        data["2024-10-27-일"] = SwimData("2024-10-27-일", 600, 250, 450, 200, 100)
        data["2024-10-28-월"] = SwimData("2024-10-28-월", 625, 275, 425, 175, 75)
        data["2024-10-29-화"] = SwimData("2024-10-29-화", 500, 300, 375, 225, 100)
        data["2024-10-30-수"] = SwimData("2024-10-30-수", 650, 250, 400, 200, 100)
        data["2024-10-31-목"] = SwimData("2024-10-31-목", 700, 200, 450, 250, 150)
        data["2024-11-01-금"] = SwimData("2024-11-01-금", 800, 300, 425, 300, 200)
        data["2024-11-02-토"] = SwimData("2024-11-02-토", 575, 225, 375, 175, 125)
        data["2024-11-03-일"] = SwimData("2024-11-03-일", 625, 250, 400, 200, 100)
        data["2024-11-04-월"] = SwimData("2024-11-04-월", 500, 200, 350, 150, 75)
        data["2024-11-05-화"] = SwimData("2024-11-05-화", 950, 300, 500, 250, 100)
        data["2024-11-06-수"] = SwimData("2024-11-06-수", 700, 200, 400, 250, 150)
        data["2024-11-07-목"] = SwimData("2024-11-07-목", 600, 250, 450, 200, 100)
        data["2024-11-08-금"] = SwimData("2024-11-08-금", 625, 275, 425, 175, 75)
        data["2024-11-09-토"] = SwimData("2024-11-09-토", 500, 300, 375, 225, 100)
        data["2024-11-10-일"] = SwimData("2024-11-10-일", 650, 250, 400, 200, 100)
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    KshoongTheme {
        SwimCalendarView()
    }
}