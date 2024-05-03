package com.stop.ui.map

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.skt.tmap.TMapPoint
import com.stop.AlarmActivity
import com.stop.R
import com.stop.databinding.FragmentMapBinding
import com.stop.model.alarm.AlarmStatus
import com.stop.model.map.Location
import com.stop.model.mission.MissionStatus
import com.stop.permission.PermissionManager
import com.stop.ui.alarmsetting.AlarmSettingFragment
import com.stop.ui.alarmsetting.AlarmSettingFragment.Companion.ALARM_MAP_CODE
import com.stop.ui.alarmsetting.AlarmSettingViewModel
import com.stop.ui.alarmstart.SoundService
import com.stop.ui.mission.MissionService
import com.stop.ui.mission.MissionViewModel
import com.stop.ui.placesearch.PlaceSearchViewModel
import com.stop.ui.tmap.MapTMap
import com.stop.ui.tmap.TMapHandler
import com.stop.ui.util.Marker
import kotlinx.coroutines.launch

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding: FragmentMapBinding
        get() = _binding!!

    private val alarmViewModel: AlarmSettingViewModel by activityViewModels()
    private val placeSearchViewModel: PlaceSearchViewModel by activityViewModels()
    private val missionViewModel: MissionViewModel by viewModels()

    private lateinit var permissionManager: PermissionManager
    private val mapHandler = object : TMapHandler {
        override fun alertTMapReady() {
            requestLocationPermission()

            tMap.initListener()
            initAfterTMapReady()
        }

        override fun setOnLocationChangeListener(location: android.location.Location) {
            placeSearchViewModel.currentLocation = Location(location.latitude, location.longitude)
        }

        override fun setOnDisableScrollWIthZoomLevelListener() {
            if (binding.homePanel.layoutPanel.visibility == View.VISIBLE) {
                binding.homePanel.layoutPanel.visibility = View.GONE
                tMap.tMapView.removeTMapMarkerItem(Marker.PLACE_MARKER)
            } else {
                setViewVisibility()
                mapUIVisibility = mapUIVisibility.xor(View.GONE)
            }
        }

        override fun setPanel(tMapPoint: TMapPoint, isClickedFromPlaceSearch: Boolean) {
            placeSearchViewModel.getGeoLocationInfo(
                tMapPoint.latitude,
                tMapPoint.longitude,
                isClickedFromPlaceSearch
            )
        }
    }

    private lateinit var missionServiceIntent: Intent
    private lateinit var tMap: MapTMap
    private var mapUIVisibility = View.GONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tMap = MapTMap(requireActivity(), mapHandler)
        permissionManager = PermissionManager(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTMap()
        initBottomSheetBehavior()
        initBottomSheetView()
        setBroadcastReceiver()
        setObserve()
        setListener()
    }

    private fun setObserve() {
        placeSearchViewModel.panelVisibility.observe(viewLifecycleOwner) {
            binding.homePanel.layoutPanel.visibility = it
        }

        placeSearchViewModel.geoLocation.observe(viewLifecycleOwner) {
            binding.homePanel.textViewPanelTitle.text = it.title
            binding.homePanel.textViewPanelAddress.text = it.roadAddress
        }
        placeSearchViewModel.distance.observe(viewLifecycleOwner) {
            binding.homePanel.textViewPanelDistance.text = it.toString()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmViewModel.alarmStatus.collect {
                    binding.homeBottomSheet.textViewAlarmState.text = it.text
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmViewModel.alarmItem.collect {
                    if (it == null) {
                        return@collect
                    }
                    binding.homeBottomSheet.layoutStateExpanded.textViewStartPositionInfo.text =
                        it.startPosition
                    binding.homeBottomSheet.layoutStateExpanded.textViewEndPositionInfo.text =
                        it.endPosition
                    binding.homeBottomSheet.layoutStateExpanded.textViewLastTimeInfo.text =
                        it.lastTime
                    binding.homeBottomSheet.layoutStateExpanded.textViewWalkTimeInfo.text =
                        requireContext().getString(R.string.walk_time_text).format(it.walkTime)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                missionViewModel.missionStatus.collect {
                    binding.homeBottomSheet.layoutStateExpanded.textViewMissionStart.text = it.text
                }
            }
        }
    }

    private fun setListener() {
        binding.homeBottomSheet.layoutStateExpanded.layoutMissionStart.setOnClickListener {
            setMissionStart()
        }
    }

    private fun initTMap() {
        binding.layoutContainer.addView(tMap.tMapView)
        initAfterTMapReady()
    }

    private fun initBottomSheetBehavior() {
        binding.layoutHomeBottomSheet.maxHeight = (630 * resources.displayMetrics.density).toInt()
        binding.homeBottomSheet.layoutStateExpanded.layoutBottomSheetHomeStateExpanded.alpha = 0F

        val behavior = BottomSheetBehavior.from(binding.layoutHomeBottomSheet)

        alarmViewModel.alarmStatus.asLiveData().observe(viewLifecycleOwner) { alarmStatus ->
            when (alarmStatus) {
                AlarmStatus.NON_EXIST -> {
                    behavior.isDraggable = false
                }

                AlarmStatus.EXIST -> {
                    behavior.isDraggable = true
                }

                AlarmStatus.MISSION -> {
                    behavior.isDraggable = true
                }
            }
        }

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) = Unit

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                with(binding.homeBottomSheet) {
                    layoutStateExpanded.layoutBottomSheetHomeStateExpanded.alpha = slideOffset
                    textViewAlarmState.alpha = 1 - slideOffset
                }
            }
        })
    }

    private fun initBottomSheetView() {
        binding.homeBottomSheet.layoutStateExpanded.viewAlarm.setOnClickListener {
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val behavior = BottomSheetBehavior.from(binding.layoutHomeBottomSheet)
            val intent = Intent(requireContext(), SoundService::class.java)

            alarmViewModel.deleteAlarm()
            notificationManager.cancel(AlarmSettingFragment.ALARM_NOTIFICATION_HIGH_ID)
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            requireContext().stopService(intent)
            SoundService.normalExit = true

            setMissionOver()
        }
    }

    private fun setMissionOver() {
        missionServiceIntent = Intent(requireActivity(), MissionService::class.java)
        requireActivity().stopService(missionServiceIntent)
        missionViewModel.missionStatus.value = MissionStatus.BEFORE
        alarmViewModel.alarmStatus.value = AlarmStatus.NON_EXIST
    }

    private fun setBroadcastReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(MissionService.MISSION_STATUS)
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.getBooleanExtra(MissionService.MISSION_STATUS, false) == true) {
                    missionViewModel.missionStatus.value = MissionStatus.ONGOING
                    alarmViewModel.alarmStatus.value = AlarmStatus.MISSION
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(
                receiver,
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            requireActivity().registerReceiver(receiver, intentFilter)
        }
    }

    private fun requestLocationPermission(isShowDialog: Boolean = false) {
        val onGranted = tMap::setTrackingMode
        permissionManager.getLocationPermission(
            onGranted = onGranted,
            isOkayPartialGranted = true,
            isShowDialog = isShowDialog,
        )
    }

    private fun initAfterTMapReady() {
        initView()
        initNavigateAction()
        observeClickPlace()
        observeClickCurrentLocation()
    }

    private fun initView() {
        binding.layoutCompass.setOnClickListener {
            tMap.tMapView.isCompassMode = tMap.tMapView.isCompassMode.not()
        }

        binding.layoutCurrent.setOnClickListener {
            requestLocationPermission(isShowDialog = true)

            tMap.isTracking = true
            tMap.tMapView.setCenterPoint(
                placeSearchViewModel.currentLocation.latitude,
                placeSearchViewModel.currentLocation.longitude,
                true
            )
            tMap.addMarker(
                Marker.PERSON_MARKER,
                Marker.PERSON_MARKER_IMG,
                TMapPoint(
                    placeSearchViewModel.currentLocation.latitude,
                    placeSearchViewModel.currentLocation.longitude
                )
            )
        }

    }

    private fun initNavigateAction() {
        binding.textViewSearch.setOnClickListener {
            findNavController().navigate(R.id.action_mapFragment_to_placeSearchFragment)
        }

        binding.homePanel.viewPanelStart.setOnClickListener {
            placeSearchViewModel.setPanelVisibility(View.INVISIBLE)
            val bundle = bundleOf("start" to placeSearchViewModel.panelInfo)
            findNavController().navigate(R.id.action_mapFragment_to_route_nav_graph, bundle)
        }

        binding.homePanel.viewPanelEnd.setOnClickListener {
            placeSearchViewModel.setPanelVisibility(View.INVISIBLE)
            val bundle = bundleOf("end" to placeSearchViewModel.panelInfo)
            findNavController().navigate(R.id.action_mapFragment_to_route_nav_graph, bundle)
        }
    }

    private fun observeClickPlace() {
        placeSearchViewModel.clickPlaceUseCaseItem.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { clickPlace ->
                val clickTMapPoint = TMapPoint(clickPlace.centerLat, clickPlace.centerLon)

                tMap.isTracking = false
                tMap.tMapView.setCenterPoint(
                    clickTMapPoint.latitude,
                    clickTMapPoint.longitude,
                    true
                )
                tMap.addMarker(Marker.PLACE_MARKER, Marker.PLACE_MARKER_IMG, clickTMapPoint)
                mapHandler.setPanel(clickTMapPoint, true)
            }
        }
    }

    private fun observeClickCurrentLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            placeSearchViewModel.clickCurrentLocation
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    val currentLocation = placeSearchViewModel.currentLocation
                    val currentTMapPoint =
                        TMapPoint(currentLocation.latitude, currentLocation.longitude)

                    tMap.isTracking = false
                    tMap.tMapView.setCenterPoint(
                        currentTMapPoint.latitude,
                        currentTMapPoint.longitude
                    )
                    tMap.addMarker(Marker.PLACE_MARKER, Marker.PLACE_MARKER_IMG, currentTMapPoint)
                    mapHandler.setPanel(currentTMapPoint, false)
                }
        }
    }

    private fun setViewVisibility() {
        with(binding) {
            textViewSearch.visibility = mapUIVisibility
            layoutCompass.visibility = mapUIVisibility
            layoutCurrent.visibility = mapUIVisibility
        }
    }

    override fun onResume() {
        super.onResume()

        alarmViewModel.getAlarm()

        requireActivity().intent.extras?.getInt("ALARM_MAP_CODE")?.let {
            if (it == ALARM_MAP_CODE) {
                openBottomSheet()
            }
        }
    }

    private fun openBottomSheet() {
        val behavior = BottomSheetBehavior.from(binding.layoutHomeBottomSheet)

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        placeSearchViewModel.setPanelVisibility(binding.homePanel.layoutPanel.visibility)
        binding.layoutContainer.removeView(tMap.tMapView)
        _binding = null

        super.onDestroyView()
    }

    fun setMissionStart() {
        Intent(requireContext(), AlarmActivity::class.java).apply {
            putExtra("MISSION_CODE", MissionService.MISSION_CODE)
            flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(this)
        }
    }
}