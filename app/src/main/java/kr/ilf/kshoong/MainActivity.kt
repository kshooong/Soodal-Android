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
import kr.ilf.kshoong.ui.SwimCalendarView
import kr.ilf.kshoong.ui.theme.KshoongTheme
import kr.ilf.kshoong.ui.SwimCalendarView2
import kr.ilf.kshoong.ui.SwimCalendarView3

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }
        })
        enableEdgeToEdge()
        setContent {
            KshoongTheme {
                SwimCalendarView3()
            }
        }
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