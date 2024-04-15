package com.stop.ui.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stop.databinding.ItemRouteBinding
import com.stop.model.route.ItineraryInfo

class RouteAdapter(
    private val onItineraryClickListener: OnItineraryClickListener
) : ListAdapter<ItineraryInfo, RouteViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val viewHolder = RouteViewHolder(binding)
        binding.root.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            onItineraryClickListener.onItineraryClick(getItem(position))
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: RouteViewHolder) {
        holder.recycle()
    }


    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<ItineraryInfo>() {
            override fun areItemsTheSame(
                oldItinerary: ItineraryInfo,
                newItinerary: ItineraryInfo
            ): Boolean {
                return oldItinerary.totalDistance == newItinerary.totalDistance
            }

            override fun areContentsTheSame(
                oldItinerary: ItineraryInfo,
                newItinerary: ItineraryInfo
            ): Boolean {
                return oldItinerary == newItinerary
            }
        }
    }

}