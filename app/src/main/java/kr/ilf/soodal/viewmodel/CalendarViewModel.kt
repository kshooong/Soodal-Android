package kr.ilf.soodal.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.StateFlow
import kr.ilf.soodal.database.entity.DailyRecord
import kr.ilf.soodal.database.entity.DetailRecord
import kr.ilf.soodal.database.entity.DetailRecordWithHR
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

interface CalendarViewModel {
    // msms 아이콘 테스트
    val testState: MutableState<Int>

    val uiState: MutableState<UiState>
    val calendarUiState: MutableState<CalendarUiState>
    val popupUiState: MutableState<PopupUiState>

    val healthPermissions: Set<String>
    val hasAllPermissions: MutableState<Boolean>

    val currentMonth: MutableState<LocalDate>
    val currentWeek: MutableState<LocalDate>

    val currentMonthTotal:StateFlow<DailyRecord>

    val dailyRecords: StateFlow<MutableMap<ZonedDateTime, DailyRecord>>

    // 선택된 날짜의 데이터
    val currentDetailRecords :StateFlow<List<DetailRecordWithHR>>

    // 영법 수정창 데이터
    val currentModifyRecord: StateFlow<DetailRecord?>

    // 새로 추가된 데이터
    val newRecords: StateFlow<MutableMap<String, DetailRecord>>

    // 선택한 날짜의 데이터 종합
    val totalDetailRecordWithHR: StateFlow<DetailRecordWithHR?>

    fun initSwimmingData(onSyncComplete: () -> Unit)
    fun updateDailyRecords(month: LocalDate = currentMonth.value)
    fun findDetailRecord(date: Instant)
    fun calculateTotalDetailRecord(detailRecords: List<DetailRecordWithHR> = currentDetailRecords.value)
    fun checkAndShowNewRecordPopup()
    fun resetDetailRecord()
    fun setModifyRecord(record: DetailRecord?)
    fun modifyDetailRecord(record: DetailRecord)
    suspend fun removeNewRecord(id: String)
    fun checkPermissions(): Boolean
    fun setChangeToken(token: String?)
    fun testNewSessionPopup()
}
