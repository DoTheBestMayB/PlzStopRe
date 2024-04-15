package com.stop.ui.route

import com.stop.databinding.ItemRouteStationBinding

object StationViewPool {
    private val views = ArrayList<ItemRouteStationBinding>()

    fun getRecycledView(): ItemRouteStationBinding? {
        return views.removeLastOrNull()
    }

    fun putRecycledView(view: ItemRouteStationBinding) {
        views.add(view)
    }
}