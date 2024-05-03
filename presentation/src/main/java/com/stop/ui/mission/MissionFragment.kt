package com.stop.ui.mission

import android.animation.Animator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.skt.tmap.TMapPoint
import com.stop.MainActivity
import com.stop.R
import com.stop.databinding.FragmentMissionBinding
import com.stop.domain.model.route.tmap.custom.Place
import com.stop.domain.model.route.tmap.custom.WalkRoute
import com.stop.model.alarm.AlarmStatus
import com.stop.model.map.Location
import com.stop.model.mission.MissionStatus
import com.stop.permission.PermissionManager
import com.stop.ui.alarmsetting.AlarmSettingViewModel
import com.stop.ui.mission.MissionService.Companion.MISSION_LAST_TIME
import com.stop.ui.mission.MissionService.Companion.MISSION_LOCATIONS
import com.stop.ui.mission.MissionService.Companion.MISSION_OVER
import com.stop.ui.mission.MissionService.Companion.MISSION_TIME_OVER
import com.stop.ui.tmap.MissionTMap
import com.stop.ui.tmap.TMapHandler
import com.stop.ui.util.Marker
import com.stop.util.isMoreThanOreo
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

class MissionFragment : Fragment() {

    private var _binding: FragmentMissionBinding? = null
    private val binding: FragmentMissionBinding
        get() = _binding!!

    private val missionViewModel: MissionViewModel by viewModels()
    private val alarmSettingViewModel: AlarmSettingViewModel by activityViewModels()

    private lateinit var tMap: MissionTMap
    private lateinit var missionServiceIntent: Intent
    private lateinit var backPressedCallback: OnBackPressedCallback
    private lateinit var userInfoReceiver: BroadcastReceiver
    private lateinit var timeReceiver: BroadcastReceiver

    private val permissionManager = PermissionManager(this)

    private val mapHandler = object : TMapHandler {
        override fun alertTMapReady() {
            requestLocationPermission(false) {
                tMap.isTracking = true
            }
            getAlarmInfo()
            alarmSettingViewModel.alarmStatus.value = AlarmStatus.MISSION
            drawPersonLine()
            setOnEnableScrollWithZoomLevelListener()
        }

        override fun setOnLocationChangeListener(location: android.location.Location) {
        }

        override fun setOnDisableScrollWIthZoomLevelListener() {
        }

        override fun setPanel(tMapPoint: TMapPoint, isClickedFromPlaceSearch: Boolean) {
        }

    }

    var personCurrentLocation = Location(37.553836, 126.969652)
    var firstTime = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setMissionService()
        setUserInfoBroadcastReceiver()
        setTimeOverBroadcastReceiver()
        missionViewModel.missionStatus.value = MissionStatus.ONGOING
    }

    private fun setMissionService() {
        missionServiceIntent = Intent(requireActivity(), MissionService::class.java)
        if (isMoreThanOreo()) {
            requireActivity().startForegroundService(missionServiceIntent)
        } else {
            requireActivity().startService(missionServiceIntent)
        }
    }

    private fun setUserInfoBroadcastReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(MissionService.MISSION_USER_INFO)
        }

        userInfoReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                missionViewModel.lastTime.value = intent?.getStringExtra(MISSION_LAST_TIME)
                missionViewModel.userLocations.value =
                    intent?.getParcelableArrayListExtra<Location>(MISSION_LOCATIONS) as ArrayList<Location>
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(
                userInfoReceiver,
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            requireActivity().registerReceiver(userInfoReceiver, intentFilter)
        }
    }

    private fun setTimeOverBroadcastReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(MISSION_TIME_OVER)
        }

        timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.getBooleanExtra(MISSION_TIME_OVER, false) == true) {
                    Snackbar.make(
                        requireActivity().findViewById(R.id.constraint_layout_container),
                        "시간이 만료되어 미션에 실패하셨습니다.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    setFailAnimation()
                }

            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(
                timeReceiver,
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            requireActivity().registerReceiver(timeReceiver, intentFilter)
        }
    }

    private fun setFailAnimation() {
        with(binding.lottieFail) {
            visibility = View.VISIBLE
            playAnimation()
            addAnimatorListener(object : Animator.AnimatorListener {

                override fun onAnimationEnd(animation: Animator) {
                    missionViewModel.missionStatus.value = MissionStatus.OVER
                }

                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationRepeat(animation: Animator) = Unit

            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMissionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAlarm()
        setTimer()
        initTMap()
        setMissionOver()
        requestLocationPermission(false)
        setObserve()
        setListener()
    }

    private fun setAlarm() {
        alarmSettingViewModel.getAlarm()
    }

    private fun setObserve() {
        missionViewModel.lastTime.observe(viewLifecycleOwner) {
            binding.textViewTimeLeft.text = it
        }
    }

    private fun setListener() {
        binding.layoutMissionClose.setOnClickListener {
            clickMissionOver()
        }
        binding.layoutCompass.setOnClickListener {
            setCompassMode()
        }
        binding.layoutZoomOut.setOnClickListener {
            setZoomOut()
        }
        binding.imageViewPersonCurrentLocation.setOnClickListener {
            setPersonCurrent()
        }
    }

    private fun setTimer() {
        missionServiceIntent.putExtra(
            MISSION_LAST_TIME,
            alarmSettingViewModel.alarmItem.value?.lastTime
        )
        if (isMoreThanOreo()) {
            requireActivity().startForegroundService(missionServiceIntent)
        } else {
            requireActivity().startService(missionServiceIntent)
        }

        missionServiceIntent.removeExtra(MISSION_LAST_TIME)
    }

    private fun initTMap() {
        tMap = MissionTMap(requireActivity(), mapHandler)

        binding.constraintLayoutContainer.addView(tMap.tMapView)
    }

    private fun setMissionOver() {
        viewLifecycleOwner.lifecycleScope.launch {
            missionViewModel.missionStatus.collect { missionStatus ->
                if (missionStatus == MissionStatus.OVER) {
                    alarmSettingViewModel.deleteAlarm()
                    missionServiceIntent.putExtra(MISSION_OVER, true)
                    if (isMoreThanOreo()) {
                        requireActivity().startForegroundService(missionServiceIntent)
                    } else {
                        requireActivity().startService(missionServiceIntent)
                    }
                    requireActivity().stopService(missionServiceIntent)
                    missionViewModel.missionStatus.value = MissionStatus.BEFORE
                    alarmSettingViewModel.alarmStatus.value = AlarmStatus.NON_EXIST

                    Intent(requireActivity(), MainActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(this)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            missionViewModel.destination.collect {
                binding.textViewDestination.text = it.name
            }
        }
    }

    private fun requestLocationPermission(
        isShowDialog: Boolean = false,
        onGranted: (() -> Unit) = { }
    ) {
        permissionManager.getLocationPermission(
            onGranted = onGranted,
            isOkayPartialGranted = true,
            isShowDialog = isShowDialog,
        )
    }

    fun setCompassMode() {
        tMap.tMapView.isCompassMode = tMap.tMapView.isCompassMode.not()
    }

    fun setPersonCurrent() {
        requestLocationPermission(true) {
            tMap.tMapView.setCenterPoint(
                personCurrentLocation.latitude,
                personCurrentLocation.longitude,
                true
            )

            tMap.isTracking = true
            tMap.tMapView.zoomLevel = 16
        }
    }

    fun setZoomOut() {
        with(tMap) {
            latitudes.clear()
            longitudes.clear()
            latitudes.add(missionViewModel.destination.value.coordinate.latitude.toDouble())
            longitudes.add(missionViewModel.destination.value.coordinate.longitude.toDouble())
            latitudes.add(personCurrentLocation.latitude)
            longitudes.add(personCurrentLocation.longitude)
            setRouteDetailFocus()
            isTracking = false
        }
    }

    fun clickMissionOver() {
        Snackbar.make(
            requireActivity().findViewById(R.id.constraint_layout_container),
            "미션을 취소했습니다",
            Snackbar.LENGTH_SHORT
        ).show()
        missionViewModel.missionStatus.value = MissionStatus.OVER
    }

    private fun setOnEnableScrollWithZoomLevelListener() {
        tMap.apply {
            tMapView.setOnEnableScrollWithZoomLevelListener { _, _ ->
                isTracking = false
            }
        }
    }

    private fun drawPersonLine() {
        var beforeLocation: Location? = null
        viewLifecycleOwner.lifecycleScope.launch {
            missionViewModel.userLocations.collectIndexed { index, userLocation ->
                if (userLocation.isEmpty()) {
                    return@collectIndexed
                }
                val location = userLocation.last()
                beforeLocation?.let {
                    drawNowLocationLine(
                        TMapPoint(location.latitude, location.longitude),
                        TMapPoint(it.latitude, it.longitude)
                    )
                    personCurrentLocation = location
                    if (tMap.isTracking) {
                        tMap.tMapView.setCenterPoint(
                            location.latitude,
                            location.longitude
                        )
                    }
                    beforeLocation = location
                    arriveDestination(location.latitude, location.longitude)
                } ?: run {
                    initMarker(userLocation)
                    beforeLocation = location
                }
            }
        }
    }

    private fun initMarker(nowLocation: ArrayList<Location>) {
        with(tMap) {
            addMarker(
                Marker.PERSON_MARKER,
                Marker.PERSON_MARKER_IMG,
                TMapPoint(nowLocation.last().latitude, nowLocation.last().longitude)
            )
            personCurrentLocation = nowLocation.last()
            latitudes.add(nowLocation.last().latitude)
            longitudes.add(nowLocation.last().longitude)
            setRouteDetailFocus()
            arriveDestination(nowLocation.last().latitude, nowLocation.last().longitude)

            drawWalkLines(
                nowLocation.map { TMapPoint(it.latitude, it.longitude) } as ArrayList<TMapPoint>,
                Marker.PERSON_LINE + PERSON_LINE_NUM.toString(),
                Marker.PERSON_LINE_COLOR
            )
            PERSON_LINE_NUM += 1

        }
    }

    private fun drawNowLocationLine(nowLocation: TMapPoint, beforeLocation: TMapPoint) {
        tMap.drawMoveLine(
            nowLocation,
            beforeLocation,
            Marker.PERSON_LINE + PERSON_LINE_NUM.toString(),
            Marker.PERSON_LINE_COLOR
        )
        PERSON_LINE_NUM += 1

        tMap.addMarker(Marker.PERSON_MARKER, Marker.PERSON_MARKER_IMG, nowLocation)
    }

    private fun getAlarmInfo() {
        alarmSettingViewModel.getAlarm(missionViewModel.missionStatus.value)
        val linePoints = arrayListOf<TMapPoint>()
        val walkInfo = alarmSettingViewModel.alarmItem.value?.routes as WalkRoute
        tMap.makeWalkRoute(walkInfo, linePoints)
        tMap.drawWalkLines(linePoints, Marker.WALK_LINE, Marker.WALK_LINE_COLOR)

        missionViewModel.destination.value = walkInfo.end
        makeDestinationMarker(walkInfo.end)
    }

    private fun makeDestinationMarker(destination: Place) {
        val latitude = destination.coordinate.latitude.toDouble()
        val longitude = destination.coordinate.longitude.toDouble()
        tMap.addMarker(
            Marker.DESTINATION_MARKER,
            Marker.DESTINATION_MARKER_IMG,
            TMapPoint(latitude, longitude)
        )
        tMap.latitudes.add(latitude)
        tMap.longitudes.add(longitude)
    }

    private fun arriveDestination(nowLatitude: Double, nowLongitude: Double) {
        if (tMap.getDistance(
                nowLatitude,
                nowLongitude,
                missionViewModel.destination.value.coordinate.latitude.toDouble(),
                missionViewModel.destination.value.coordinate.longitude.toDouble()
            ) <= 10
            && firstTime == 0
        ) {
            firstTime += 1
            Snackbar.make(
                requireActivity().findViewById(R.id.constraint_layout_container),
                "정류장에 도착했습니다!",
                Snackbar.LENGTH_SHORT
            ).show()
            setSuccessAnimation()
        }
    }

    private fun setSuccessAnimation() {
        with(binding.lottieSuccess) {
            playAnimation()
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    missionViewModel.missionStatus.value = MissionStatus.OVER
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

    override fun onDestroy() {
        tMap.onDestroy()

        requireActivity().unregisterReceiver(userInfoReceiver)
        requireActivity().unregisterReceiver(timeReceiver)

        super.onDestroy()
    }

    companion object {
        private var PERSON_LINE_NUM = 0
    }

}