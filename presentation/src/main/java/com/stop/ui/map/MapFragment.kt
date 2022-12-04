package com.stop.ui.map

import android.Manifest.permission
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.skt.tmap.TMapPoint
import com.stop.R
import com.stop.databinding.FragmentMapBinding
import com.stop.model.Location
import com.stop.ui.alarmsetting.AlarmSettingViewModel
import com.stop.ui.placesearch.PlaceSearchViewModel
import com.stop.ui.util.Marker
import kotlinx.coroutines.launch

class MapFragment : Fragment(), MapHandler {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val alarmViewModel: AlarmSettingViewModel by activityViewModels()
    private val placeSearchViewModel: PlaceSearchViewModel by activityViewModels()

    private lateinit var tMap: MapTMap
    private var mapUIVisibility = View.GONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        initBinding()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTMap()
        initView()
        initNavigateAction()
        initBottomSheetBehavior()
        listenButtonClick()
    }

    override fun alertTMapReady() {
        requestPermissionsLauncher.launch(PERMISSIONS)

        tMap.initListener()
        addBookmarkMarker()
        observeClickPlace()
        observeClickCurrentLocation()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.alarmViewModel = alarmViewModel
        binding.placeSearchViewModel = placeSearchViewModel
    }

    private fun initTMap() {
        tMap = MapTMap(requireContext(), this)
        tMap.init()

        binding.frameLayoutContainer.addView(tMap.tMapView)
    }

    private fun initView() {
        binding.layoutCompass.setOnClickListener {
            tMap.tMapView.isCompassMode = tMap.tMapView.isCompassMode.not()
        }

        binding.layoutCurrent.setOnClickListener {
            requestPermissionsLauncher.launch(PERMISSIONS)

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
            binding.root.findNavController()
                .navigate(R.id.action_mapFragment_to_placeSearchFragment)
        }

        /*
        binding.layoutBookmark.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_mapFragment_to_bookMarkFragment)
        }
        */

        binding.layoutPanel.findViewById<View>(R.id.view_panel_start).setOnClickListener {
            val action = MapFragmentDirections.actionMapFragmentToRouteFragment().setStart(placeSearchViewModel.panelInfo)
            binding.root.findNavController().navigate(action)
        }

        binding.layoutPanel.findViewById<View>(R.id.view_panel_end).setOnClickListener {
            val action = MapFragmentDirections.actionMapFragmentToRouteFragment().setEnd(placeSearchViewModel.panelInfo)
            binding.root.findNavController().navigate(action)
        }
    }

    private fun initBottomSheetBehavior() {
        val behavior = BottomSheetBehavior.from(binding.layoutHomeBottomSheet)

        alarmViewModel.getAlarm()

        alarmViewModel.isAlarmItemNotNull.asLiveData().observe(viewLifecycleOwner){
            behavior.isDraggable = it
        }

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.homeBottomSheet.layoutStateExpanded.root.visibility = View.VISIBLE
                        binding.homeBottomSheet.textViewAlarmState.visibility = View.GONE
                        binding.homeBottomSheet.homeBottomSheetDragHandle.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.homeBottomSheet.layoutStateExpanded.root.visibility = View.GONE
                        binding.homeBottomSheet.textViewAlarmState.visibility = View.VISIBLE
                        binding.homeBottomSheet.homeBottomSheetDragHandle.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_DRAGGING -> Unit
                    BottomSheetBehavior.STATE_SETTLING -> Unit
                    BottomSheetBehavior.STATE_HIDDEN -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit

        })
    }

    private fun addBookmarkMarker() {
        placeSearchViewModel.bookmarks.forEachIndexed { index, location ->
            tMap.addMarker(
                index.toString(),
                Marker.BOOKMARK_MARKER_IMG,
                TMapPoint(location.latitude, location.longitude)
            )
        }
    }

    private fun observeClickPlace() {
        placeSearchViewModel.clickPlaceUseCaseItem.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { clickPlace ->
                val clickTMapPoint = TMapPoint(clickPlace.centerLat, clickPlace.centerLon)

                tMap.tMapView.setCenterPoint(
                    clickTMapPoint.latitude,
                    clickTMapPoint.longitude,
                    true
                )
                tMap.addMarker(Marker.PLACE_MARKER, Marker.PLACE_MARKER_IMG, clickTMapPoint)
                setPanel(clickTMapPoint)
            }
        }
    }

    private fun observeClickCurrentLocation() {
        lifecycleScope.launch {
            placeSearchViewModel.clickCurrentLocation
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect {
                    val currentLocation = placeSearchViewModel.currentLocation
                    val currentTMapPoint =
                        TMapPoint(currentLocation.latitude, currentLocation.longitude)

                    tMap.tMapView.setCenterPoint(
                        currentTMapPoint.latitude,
                        currentTMapPoint.longitude
                    )
                    tMap.addMarker(Marker.PLACE_MARKER, Marker.PLACE_MARKER_IMG, currentTMapPoint)
                    setPanel(currentTMapPoint)
                }
        }
    }

    override fun setPanel(tMapPoint: TMapPoint) {
        placeSearchViewModel.getGeoLocationInfo(tMapPoint.latitude, tMapPoint.longitude)
    }

    override fun setOnLocationChangeListener(location: android.location.Location) {
        placeSearchViewModel.currentLocation = Location(location.latitude, location.longitude)
    }

    override fun setOnDisableScrollWIthZoomLevelListener() {
        if (binding.layoutPanel.visibility == View.VISIBLE) {
            binding.layoutPanel.visibility = View.GONE
            tMap.tMapView.removeTMapMarkerItem(Marker.PLACE_MARKER)
        } else {
            setViewVisibility()
            mapUIVisibility = mapUIVisibility.xor(View.GONE)
        }
    }

    private fun setViewVisibility() {
        with(binding) {
            layoutSearch.visibility = mapUIVisibility
            layoutCompass.visibility = mapUIVisibility
            layoutCurrent.visibility = mapUIVisibility
            layoutBookmark.visibility = mapUIVisibility
        }
    }

    private fun listenButtonClick(){
        binding.homeBottomSheet.layoutStateExpanded.buttonAlarmTurnOff.setOnClickListener {
            alarmViewModel.deleteAlarm()
            val behavior = BottomSheetBehavior.from(binding.layoutHomeBottomSheet)
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { it.value }) {
            tMap.setTrackingMode()
        }
    }

    companion object {
        private val PERMISSIONS =
            arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION)
    }
}