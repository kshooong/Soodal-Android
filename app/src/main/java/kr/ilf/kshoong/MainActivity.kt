package kr.ilf.kshoong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.ilf.kshoong.data.SwimData
import kr.ilf.kshoong.ui.SwimCalendarView4
import kr.ilf.kshoong.ui.theme.KshoongTheme
import kr.ilf.kshoong.viewmodel.SwimDataViewModel
import kr.ilf.kshoong.viewmodel.SwimDataViewModelFactory

class MainActivity : ComponentActivity() {

    private val healthConnectManager by lazy { HealthConnectManager(this) }

    companion object {
        val data = HashMap<String, SwimData>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDummyData()
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        enableEdgeToEdge()
        setContent {
            KshoongTheme {
                val isLoading = remember {
                    mutableStateOf(true)
                }

                if (isLoading.value) {
                    LoadingView(isLoading, healthConnectManager)
                } else {
                    SwimCalendarView4(data, healthConnectManager)
                }
            }
        }
    }


    private fun setDummyData() {
        data["2024-10-01-화"] = SwimData("2024-10-01-화", 600, 200, 400, 200, 100)
        data["2024-10-02-수"] = SwimData("2024-10-02-수", 625, 0, 450, 150, 150)
        data["2024-10-03-목"] = SwimData("2024-10-03-목", 500, 300, 0, 225, 100)
        data["2024-10-04-금"] = SwimData("2024-10-04-금", 200, 650, 425, 200, 400)
        data["2024-10-05-토"] = SwimData("2024-10-05-토", 700, 200, 0, 250, 150)
        data["2024-10-06-일"] = SwimData("2024-10-06-일", 800, 300, 0, 300, 200)
        data["2024-10-07-월"] = SwimData("2024-10-07-월", 575, 0, 375, 0, 150)
        data["2024-10-08-화"] = SwimData("2024-10-08-화", 625, 0, 425, 0, 100)
        data["2024-10-09-수"] = SwimData("2024-10-09-수", 500, 200, 350, 0, 100)
        data["2024-10-10-목"] = SwimData("2024-10-10-목", 950, 0, 0, 250, 100)
        data["2024-10-11-금"] = SwimData("2024-10-11-금", 700, 200, 0, 250, 150)
        data["2024-10-14-월"] = SwimData("2024-10-14-월", 600, 250, 0, 200, 100)
        data["2024-10-15-화"] = SwimData("2024-10-15-화", 625, 275, 425, 0, 200)
        data["2024-10-16-수"] = SwimData("2024-10-16-수", 500, 0, 375, 225, 200)
        data["2024-10-17-목"] = SwimData("2024-10-17-목", 650, 0, 400, 200, 100)
        data["2024-10-18-금"] = SwimData("2024-10-18-금", 700, 0, 450, 250, 150)
        data["2024-10-19-토"] = SwimData("2024-10-19-토", 800, 300, 425, 300, 200)
        data["2024-10-20-일"] = SwimData("2024-10-20-일", 575, 0, 375, 0, 150)
        data["2024-10-21-월"] = SwimData("2024-10-21-월", 625, 250, 400, 200, 100)
        data["2024-10-22-화"] = SwimData("2024-10-22-화", 500, 0, 350, 0, 500)
        data["2024-10-23-수"] = SwimData("2024-10-23-수", 950, 0, 0, 250, 100)
        data["2024-10-24-목"] = SwimData("2024-10-24-목", 400, 0, 400, 500, 150)
        data["2024-10-27-일"] = SwimData("2024-10-27-일", 600, 250, 450, 200, 100)
        data["2024-10-28-월"] = SwimData("2024-10-28-월", 1500, 0, 200, 0, 100)
        data["2024-10-29-화"] = SwimData("2024-10-29-화", 500, 300, 0, 225, 100)
        data["2024-10-30-수"] = SwimData("2024-10-30-수", 650, 250, 400, 500, 100)
        data["2024-10-31-목"] = SwimData("2024-10-31-목", 700, 0, 450, 250, 150)
        data["2024-11-01-금"] = SwimData("2024-11-01-금", 800, 300, 425, 300, 200)
        data["2024-11-02-토"] = SwimData("2024-11-02-토", 575, 0, 0, 175, 150)
        data["2024-11-03-일"] = SwimData("2024-11-03-일", 625, 250, 400, 200, 100)
        data["2024-11-04-월"] = SwimData("2024-11-04-월", 500, 200, 0, 150, 100)
        data["2024-11-05-화"] = SwimData("2024-11-05-화", 250, 300, 500, 250, 100)
        data["2024-11-06-수"] = SwimData("2024-11-06-수", 400, 400, 0, 400, 200)
        data["2024-11-07-목"] = SwimData("2024-11-07-목", 600, 0, 450, 200, 100)
        data["2024-11-08-금"] = SwimData("2024-11-08-금", 625, 275, 500, 0, 100)
        data["2024-11-09-토"] = SwimData("2024-11-09-토", 500, 0, 600, 0, 200)
        data["2024-11-10-일"] = SwimData("2024-11-10-일", 650, 0, 400, 400, 100)
    }

}

@Composable
fun LoadingView(isLoading: MutableState<Boolean>, healthConnectManager: HealthConnectManager) {
    val availability by healthConnectManager.availability
    val viewModel: SwimDataViewModel =
        viewModel(factory = SwimDataViewModelFactory(healthConnectManager))

    if (availability && viewModel.hasAllPermissions.value.not()) {
        val permissions = viewModel.healthPermissions
        val permissionsLauncher =
            rememberLauncherForActivityResult(contract = viewModel.permissionsContract) {
                // Handle permission result
                viewModel.initSwimData()
                isLoading.value = false
            }

        LaunchedEffect(Unit) {
            permissionsLauncher.launch(permissions)
        }
    } else {
        viewModel.initSwimData()
        LaunchedEffect(Unit) {
            isLoading.value = false
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White, shape = ShapeDefaults.ExtraLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "KSHOONG!", style = MaterialTheme.typography.titleLarge)
            Image(
                painter = painterResource(id = R.drawable.logo_loading),
                contentDescription = "logo",
                modifier = Modifier.size(300.dp)
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun Preview() {
//    val isLoading = true
//    KshoongTheme {
//        if (isLoading) {
//            LoadingVIew()
//        } else {
//    SwimCalendarView4(MainActivity.data)

//        }
//    }
}