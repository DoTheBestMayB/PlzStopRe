package com.stop.ui.route

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.stop.databinding.ItemRouteStationBinding
import com.stop.model.route.RouteInfo

class StationContainer(
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {

    private var beforeViewId: Int? = null

    fun submitList(routeInfoList: List<RouteInfo>) {
        clearBeforeData()
        routeInfoList.forEachIndexed { index, routeInfo ->
            if (routeInfo.stationName == "출발지") {
                return@forEachIndexed
            }
            val binding = ItemRouteStationBinding.inflate(
                LayoutInflater.from(context),
                this,
                true,
            ).apply {
                root.id = View.generateViewId()
            }

            setBindingAttribute(binding, routeInfo, index)
            setConstraint(binding)
        }
    }

    private fun clearBeforeData() {
        removeAllViewsInLayout()
        beforeViewId = null
    }

    private fun setBindingAttribute(
        binding: ItemRouteStationBinding,
        routeInfo: RouteInfo,
        index: Int
    ) {
        binding.tvTypeName.text = routeInfo.typeName
        binding.tvLocation.text = routeInfo.stationName
        binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(routeInfo.symbolColor)
        binding.tvTypeName.setTextColor(routeInfo.symbolColor)
        binding.ivTypeIcon.setImageResource(routeInfo.symbolDrawableId)

        if (routeInfo.typeName == "하차") {
            binding.vBottomVerticalLine.visibility = View.GONE
        } else {
            binding.vBottomVerticalLine.visibility = View.VISIBLE
        }

        if (index == 1) {
            binding.vTopVerticalLine.visibility = View.INVISIBLE
        }

    }

    private fun setConstraint(binding: ItemRouteStationBinding) {
        val endId = beforeViewId ?: this.id
        val endSide = if (beforeViewId == null) {
            ConstraintSet.TOP
        } else {
            ConstraintSet.BOTTOM
        }

        with(ConstraintSet()) {
            clone(this@StationContainer)
            connect(binding.root.id, ConstraintSet.TOP, endId, endSide)
            connect(
                binding.root.id,
                ConstraintSet.START,
                this@StationContainer.id,
                ConstraintSet.START
            )
            connect(
                binding.root.id,
                ConstraintSet.END,
                this@StationContainer.id,
                ConstraintSet.END
            )
            applyTo(this@StationContainer)
        }
        beforeViewId = binding.root.id
    }

}