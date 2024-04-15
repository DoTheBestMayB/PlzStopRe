package com.stop.ui.route

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.stop.databinding.ItemRouteBinding
import com.stop.model.route.ItineraryInfo

class RouteViewHolder(
    private val binding: ItemRouteBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(itineraryInfo: ItineraryInfo) {

        if (itineraryInfo.hourOfTotalTime != 0) {
            binding.textViewRequiredHour.visibility = View.VISIBLE
            binding.textViewRequiredHourText.visibility = View.VISIBLE
            binding.textViewRequiredHour.text = itineraryInfo.hourOfTotalTime.toString()
        } else {
            binding.textViewRequiredHour.visibility = View.GONE
            binding.textViewRequiredHourText.visibility = View.GONE
        }

        binding.textViewRequiredMinute.text = itineraryInfo.minuteOfTotalTime.toString()

        binding.stationContainer.submitList(itineraryInfo.routeInfo)
        binding.timeLineContainer.submitList(itineraryInfo.routeInfo)
    }

    fun recycle() {
        binding.timeLineContainer.doRecycle()
        binding.stationContainer.doRecycle()
    }
}