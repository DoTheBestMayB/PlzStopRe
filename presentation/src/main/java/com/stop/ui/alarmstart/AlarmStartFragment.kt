package com.stop.ui.alarmstart

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.stop.R
import com.stop.databinding.FragmentAlarmStartBinding
import com.stop.ui.alarmsetting.AlarmSettingFragment.Companion.ALARM_NOTIFICATION_HIGH_ID
import com.stop.ui.alarmsetting.AlarmSettingViewModel
import com.stop.ui.mission.MissionService.Companion.MISSION_CODE
import kotlinx.coroutines.launch

class AlarmStartFragment : Fragment() {

    private var _binding: FragmentAlarmStartBinding? = null
    private val binding: FragmentAlarmStartBinding
        get() = _binding!!

    private val alarmSettingViewModel by activityViewModels<AlarmSettingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setObserve()
        setListener()
    }

    private fun initView() {
        alarmSettingViewModel.getAlarm()

        lifecycleScope.launch {
            alarmSettingViewModel.alarmItem
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    it?.let {
                        alarmSettingViewModel.startCountDownTimer(it.lastTime)
                        binding.textViewStartPosition.text = it.startPosition
                        binding.textViewStartPositionInfo.text = it.startPosition
                        binding.textViewEndPosition.text = it.endPosition
                        binding.textViewEndPositionInfo.text = it.endPosition
                        binding.textViewLastTimeInfo.text = it.lastTime
                        binding.textViewWalkTimeInfo.text =
                            requireContext().getString(R.string.walk_time_text).format(it.walkTime)
                    }
                }
        }
    }

    private fun setObserve() {
        alarmSettingViewModel.lastTimeCountDown.observe(viewLifecycleOwner) {
            binding.textViewTimeLeft.text = it
        }
    }

    private fun setListener() {
        binding.viewMission.setOnClickListener {
            clickMissionStart()
        }
        binding.viewAlarm.setOnClickListener {
            clickAlarmTurnOff()
        }
    }

    private fun clickAlarmTurnOff() {
        turnOffSoundService()
        alarmSettingViewModel.deleteAlarm()
        cancelNotification()
        requireActivity().finish()
    }

    private fun clickMissionStart() {
        turnOffSoundService()
        cancelNotification()
        binding.root.findNavController().navigate(R.id.action_alarmStartFragment_to_missionFragment)
    }

    private fun turnOffSoundService() {
        val intent = Intent(requireContext(), SoundService::class.java)
        requireContext().stopService(intent)
        SoundService.normalExit = true
    }

    private fun cancelNotification() {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ALARM_NOTIFICATION_HIGH_ID)
    }

    override fun onResume() {
        super.onResume()

        requireActivity().intent.extras?.getInt("MISSION_CODE")?.let {
            if (it == MISSION_CODE) {
                binding.root.findNavController()
                    .navigate(R.id.action_alarmStartFragment_to_missionFragment)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

}