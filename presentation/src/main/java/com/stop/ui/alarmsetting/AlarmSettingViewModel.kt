package com.stop.ui.alarmsetting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.stop.util.AlarmFunctions
import com.stop.util.convertTimeMillisToString
import com.stop.domain.model.alarm.AlarmUseCaseItem
import com.stop.domain.usecase.alarm.DeleteAlarmUseCase
import com.stop.domain.usecase.alarm.GetAlarmUseCase
import com.stop.domain.usecase.alarm.SaveAlarmUseCase
import com.stop.util.makeFullTime
import com.stop.model.alarm.AlarmStatus
import com.stop.model.mission.MissionStatus
import com.stop.ui.alarmsetting.AlarmSettingFragment.Companion.ALARM_TIME
import com.stop.ui.alarmsetting.AlarmSettingFragment.Companion.LAST_TIME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AlarmSettingViewModel @Inject constructor(
    private val saveAlarmUseCase: SaveAlarmUseCase,
    private val getAlarmUseCase: GetAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val alarmFunctions: AlarmFunctions,
    private val workManager: WorkManager
) : ViewModel() {

    private val _alarmItem = MutableStateFlow<AlarmUseCaseItem?>(null)
    val alarmItem: StateFlow<AlarmUseCaseItem?>
        get() = _alarmItem

    private val _lastTimeCountDown = MutableLiveData("")
    val lastTimeCountDown: LiveData<String>
        get() = _lastTimeCountDown

    var alarmStatus = MutableStateFlow(AlarmStatus.NON_EXIST)

    private lateinit var workerId: UUID

    private val alarmTime = MutableLiveData(0)
    var alarmMethod = true

    fun saveAlarm(alarmUseCaseItem: AlarmUseCaseItem) {
        viewModelScope.launch(Dispatchers.IO) {
            saveAlarmUseCase(alarmUseCaseItem.copy(alarmTime = alarmTime.value ?: 0, alarmMethod = alarmMethod))
        }
    }

    fun getAlarm(missionStatus: MissionStatus = MissionStatus.BEFORE) {
        viewModelScope.launch(Dispatchers.IO) {
            getAlarmUseCase().collectLatest {
                _alarmItem.value = it

                if (it != null) {
                    when (missionStatus) {
                        MissionStatus.BEFORE -> {
                            alarmStatus.value = AlarmStatus.EXIST
                        }
                        MissionStatus.ONGOING -> {
                            alarmStatus.value = AlarmStatus.MISSION
                        }
                        MissionStatus.OVER -> {
                            alarmStatus.value = AlarmStatus.EXIST
                        }
                    }

                }
            }
        }
    }

    fun deleteAlarm() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteAlarmUseCase()
        }
        cancelAlarm()
    }

    fun callAlarm(time: String) {
        deleteAlarm()
        alarmFunctions.callAlarm(time, alarmTime.value ?: 0)
    }

    private fun cancelAlarm() {
        alarmFunctions.cancelAlarm()
    }

    fun makeAlarmWorker(time: String) {
        val workData = workDataOf(
            LAST_TIME to time,
            ALARM_TIME to alarmTime.value
        )

        val workRequest = OneTimeWorkRequestBuilder<LastTimeCheckWorker>()
            .setInputData(workData)
            .build()
        workerId = workRequest.id

        workManager.enqueue(workRequest)
    }

    fun removeAlarmWorker() {
        workManager.cancelWorkById(workerId)
    }

    fun startCountDownTimer(time: String) {
        val lastTimeMillis = makeFullTime(time).timeInMillis
        val nowTimeMillis = System.currentTimeMillis()
        var diffTimeMillis = if (lastTimeMillis > nowTimeMillis) lastTimeMillis - nowTimeMillis else 0L

        viewModelScope.launch(Dispatchers.IO) {
            var oldTimeMillis = System.currentTimeMillis()
            while (diffTimeMillis > 0L) {
                val delayMillis = System.currentTimeMillis() - oldTimeMillis
                if (delayMillis == 1000L) {
                    diffTimeMillis -= delayMillis
                    _lastTimeCountDown.postValue(convertTimeMillisToString(diffTimeMillis))
                    oldTimeMillis = System.currentTimeMillis()
                }
            }
        }
    }

    fun updateTime(newVal: Int) {
        alarmTime.value = newVal
    }

}