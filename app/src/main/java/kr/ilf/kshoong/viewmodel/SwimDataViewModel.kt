package kr.ilf.kshoong.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ilf.kshoong.HealthConnectManager
import kr.ilf.kshoong.data.SwimData

class SwimDataViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {
    private val _swimDataFlow = MutableStateFlow<Map<String, SwimData>>(emptyMap())
    val swimDataFlow
        get() = _swimDataFlow.asStateFlow()

    fun getSwimData() {
        viewModelScope.launch {

        }
    }
}