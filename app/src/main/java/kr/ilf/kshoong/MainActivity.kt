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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kr.ilf.kshoong.ui.NavigationView
import kr.ilf.kshoong.ui.theme.KshoongTheme
import kr.ilf.kshoong.viewmodel.SwimDataViewModel
import kr.ilf.kshoong.viewmodel.SwimmingViewModel
import kr.ilf.kshoong.viewmodel.SwimmingViewModelFactory

class MainActivity : ComponentActivity() {

    private val healthConnectManager by lazy { HealthConnectManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        enableEdgeToEdge()
        setContent {
            KshoongTheme {

//                val viewModel: SwimDataViewModel =
//                    viewModel(factory = SwimDataViewModelFactory(healthConnectManager))
//                val uiState by viewModel.uiState
//                val isLoading = remember(uiState) { uiState == SwimDataViewModel.UiState.Loading }
//
//                if (isLoading) {
//                    LoadingView(healthConnectManager, viewModel)
//                } else {
//                    SwimCalendarView5(healthConnectManager, viewModel)
//                }
                /////////////////////////////////////////////////////////////////////////
                val viewModel: SwimmingViewModel =
                    viewModel(factory = SwimmingViewModelFactory(application, healthConnectManager))
                val navController = rememberNavController()
                NavigationView(navController, healthConnectManager, viewModel)
            }
        }
    }
}

@Composable
fun LoadingView(healthConnectManager: HealthConnectManager, viewModel: SwimDataViewModel) {
    val availability by healthConnectManager.availability
    if (availability && viewModel.hasAllPermissions.value.not()) {
        val permissions = viewModel.healthPermissions
        val permissionsLauncher =
            rememberLauncherForActivityResult(contract = viewModel.permissionsContract) {
                // Handle permission result/
                viewModel.initSwimData()
            }

        LaunchedEffect(Unit) {
            permissionsLauncher.launch(permissions)
        }
    } else {
        viewModel.initSwimData()
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