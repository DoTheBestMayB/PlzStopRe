package com.stop.ui.routedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.stop.R
import com.stop.databinding.FragmentRouteDetailBinding
import com.stop.domain.model.route.tmap.custom.Coordinate
import com.stop.ui.route.RouteResultViewModel
import com.stop.ui.util.DrawerStringUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RouteDetailFragment : Fragment(), RouteDetailHandler {

    private var _binding: FragmentRouteDetailBinding? = null
    private val binding: FragmentRouteDetailBinding
        get() = _binding!!

    private val routeResultViewModel: RouteResultViewModel by navGraphViewModels(R.id.route_nav_graph)

    private lateinit var tMap: RouteDetailTMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouteDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTMap()
        initView()
        setRecyclerView()
        setObserve()
    }

    private fun initTMap() {
        tMap = RouteDetailTMap(requireActivity(), this)
        tMap.init()

        binding.layoutContainer.addView(tMap.tMapView)
    }

    private fun initView() {
        binding.layoutDrawer.openDrawer(GravityCompat.START)

        binding.imageViewArrowDrawer.setOnClickListener {
            binding.layoutDrawer.openDrawer(GravityCompat.START)
        }

        binding.routeDetailDrawer.viewAlarm.setOnClickListener {
            if (routeResultViewModel.isLastTimeAvailable.value == true) {
                findNavController().navigate(R.id.action_routeDetailFragment_to_alarmSetting)
            }
        }

        binding.imageViewClose.setOnClickListener {
            findNavController().apply {
                popBackStack(R.id.mapFragment, false)
                requireActivity().viewModelStore.clear()
            }
        }
    }

    private fun setRecyclerView() {
        val adapter = RouteDetailAdapter(object : OnRouteItemClickListener {
            override fun clickRouteItem(coordinate: Coordinate) {
                binding.layoutDrawer.closeDrawer(GravityCompat.START)
                tMap.setRouteItemFocus(coordinate)
            }
        })

        binding.routeDetailDrawer.recyclerViewRouteDetail.adapter = adapter
        adapter.submitList(routeResultViewModel.getRouteItems())
    }

    private fun setObserve() {
        routeResultViewModel.origin.observe(viewLifecycleOwner) {
            binding.textViewOrigin.text = it.name
        }
        routeResultViewModel.destination.observe(viewLifecycleOwner) {
            binding.textViewDestination.text = it.name
        }
        routeResultViewModel.itinerary.observe(viewLifecycleOwner) {
            binding.routeDetailDrawer.textViewTime.text = DrawerStringUtils.getTimeString(it.totalTime)
            binding.routeDetailDrawer.textViewInformation.text = DrawerStringUtils.getTopInformationString(it)
        }
        routeResultViewModel.isLastTimeAvailable.observe(viewLifecycleOwner) {
            binding.routeDetailDrawer.viewAlarm.visibility = if (it) View.VISIBLE else View.INVISIBLE
            binding.routeDetailDrawer.viewAlarm2.visibility = if (it) View.INVISIBLE else View.VISIBLE
            binding.routeDetailDrawer.constraintLayoutAlertCantDo.visibility = if (it) View.VISIBLE else View.INVISIBLE
            binding.routeDetailDrawer.textViewAlarmTextCantSet.visibility = if (it) View.INVISIBLE else View.VISIBLE
        }
    }

    override fun alertTMapReady() {
        routeResultViewModel.itinerary.value?.let {
            tMap.drawRoutes(it.routes)
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

}