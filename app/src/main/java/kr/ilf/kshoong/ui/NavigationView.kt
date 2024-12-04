package kr.ilf.kshoong.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kr.ilf.kshoong.Destinations
import kr.ilf.kshoong.HealthConnectManager
import kr.ilf.kshoong.R
import kr.ilf.kshoong.viewmodel.SwimmingViewModel

@Composable
fun NavigationView(
    navController: NavHostController,
    healthConnectManager: HealthConnectManager,
    viewModel: SwimmingViewModel
) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Destinations.DESTINATION_LOADING) {
        composable(Destinations.DESTINATION_LOADING) {
            LoadingView(
                context = context,
                healthConnectManager = healthConnectManager,
                viewModel = viewModel,
                onLoadingComplete = { navController.navigate(Destinations.DESTINATION_SYNC) })
        }
        composable(Destinations.DESTINATION_SYNC) {
            SyncView(
                context = context,
                viewModel = viewModel
            )
        }
        composable(Destinations.DESTINATION_CALENDAR) {

        }
    }
}

@Composable
fun LoadingView(
    context: Context,
    healthConnectManager: HealthConnectManager,
    viewModel: SwimmingViewModel,
    onLoadingComplete: () -> Unit
) {
    fun setChangeToken() {
        val sharedPreferences = context.getSharedPreferences("changeToken", MODE_PRIVATE)
        viewModel.setChangeToken(sharedPreferences.getString("changeToken", null))
        onLoadingComplete()
    }

    val availability by healthConnectManager.availability
    if (availability && viewModel.hasAllPermissions.value.not()) {
        val permissions = viewModel.healthPermissions
        val permissionsLauncher =
            rememberLauncherForActivityResult(contract = viewModel.permissionsContract) {
                // Handle permission result/
                if (viewModel.checkPermissions()) {
                    setChangeToken()
                } else {
                    // 종료
                }
            }

        LaunchedEffect(Unit) {
            delay(1000)
            permissionsLauncher.launch(permissions)
        }
    } else if (availability && viewModel.hasAllPermissions.value) {
        LaunchedEffect(Unit) {
            delay(1000)
            setChangeToken()
        }
    } else {
        // 종료
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
                painter = painterResource(id = R.drawable.logo_loading_img),
                contentDescription = "logo",
                modifier = Modifier.size(108.dp)
            )
        }
    }
}

@Composable
fun SyncView(
    context: Context,
    viewModel: SwimmingViewModel,
//    onLoadingComplete: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.initSwimmingData() }

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
            Text(text = "Synchronizing", style = MaterialTheme.typography.titleLarge)
            Image(
                painter = painterResource(id = R.drawable.logo_loading_img),
                contentDescription = "logo",
                modifier = Modifier.size(108.dp)
            )
        }
    }
}
