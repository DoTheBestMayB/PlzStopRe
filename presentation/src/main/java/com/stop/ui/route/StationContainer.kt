package com.stop.ui.route

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.stop.databinding.ItemStationContainerBinding
import com.stop.model.route.RouteItem

class StationContainer(
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {

    private var beforeViewId: Int? = null

    fun submitList(routeItems: List<RouteItem>) {
        clearBeforeData()
        routeItems.forEachIndexed { index, routeItem ->
            val stationContainerItemBinding = ItemStationContainerBinding.inflate(
                LayoutInflater.from(context),
                this,
                true,
            ).apply {
                root.id = View.generateViewId()
            }

            setBindingAttribute(stationContainerItemBinding, routeItem, index)
            setConstraint(stationContainerItemBinding)
        }
    }

    private fun clearBeforeData() {
        beforeViewId = null
    }

    private fun setBindingAttribute(
        binding: ItemStationContainerBinding,
        routeItem: RouteItem,
        index: Int
    ) {
        binding.textViewTypeName.text = routeItem.typeName
        binding.textViewName.text = routeItem.name
        binding.viewBeforeLine.setBackgroundColor(routeItem.beforeColor)
        binding.viewCurrentLine.setBackgroundColor(routeItem.currentColor)
        ImageViewCompat.setImageTintList(binding.imageViewCurrentLine, ColorStateList.valueOf(routeItem.currentColor))
        binding.imageViewMode.setImageResource(routeItem.mode)

        if (routeItem.typeName == "하차") {
            binding.viewLine.visibility = View.GONE
            binding.viewCurrentLine.visibility = View.GONE
        } else {
            binding.viewLine.visibility = View.VISIBLE
            binding.viewCurrentLine.visibility = View.VISIBLE
        }

        if (index == 0) {
            binding.viewBeforeLine.visibility = View.INVISIBLE
        }
    }

    private fun setConstraint(binding: ItemStationContainerBinding) {
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