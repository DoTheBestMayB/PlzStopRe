package com.stop.ui.route

import com.stop.databinding.ItemTimeLineBinding

object TimeLineViewPool {
    private val views = ArrayList<ItemTimeLineBinding>()

    fun getRecycledView(): ItemTimeLineBinding? {
        return views.removeLastOrNull()
    }

    fun putRecycledView(view: ItemTimeLineBinding) {
        views.add(view)
    }
}