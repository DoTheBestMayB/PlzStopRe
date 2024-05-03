package com.stop.ui.routedetail

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.stop.databinding.ItemRouteFirstBinding
import com.stop.databinding.ItemRouteLastBinding
import com.stop.databinding.ItemRoutePathBinding
import com.stop.model.route.RouteItem
import com.stop.model.route.RouteItemType
import com.stop.ui.util.DrawerStringUtils


class RouteDetailAdapter(
    private val onRouteItemClickListener: OnRouteItemClickListener
) : ListAdapter<RouteItem, RecyclerView.ViewHolder>(diffUtil) {

    class FirstViewHolder(
        private val binding: ItemRouteFirstBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(routeItem: RouteItem) {
            binding.textViewName.text = routeItem.name
            binding.viewCurrentLine.setBackgroundColor(routeItem.currentColor)
            ImageViewCompat.setImageTintList(
                binding.imageViewCurrentLine,
                ColorStateList.valueOf(routeItem.currentColor)
            )
        }
    }

    class PathViewHolder(
        private val binding: ItemRoutePathBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(routeItem: RouteItem) {
            binding.textViewName.text = routeItem.name
            binding.textViewInformation.text =
                DrawerStringUtils.getRouteItemInformationString(routeItem)
            binding.textViewLastTime1.visibility =
                if (routeItem.lastTime != null) View.VISIBLE else View.INVISIBLE
            binding.textViewLastTime2.text = routeItem.lastTime
            binding.viewBeforeLine.setBackgroundColor(routeItem.beforeColor)
            binding.viewCurrentLine.setBackgroundColor(routeItem.currentColor)
            ImageViewCompat.setImageTintList(
                binding.imageViewCurrentLine,
                ColorStateList.valueOf(routeItem.currentColor)
            )
            binding.imageViewMode.setImageResource(routeItem.mode)
        }
    }

    class LastViewHolder(
        private val binding: ItemRouteLastBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(routeItem: RouteItem) {
            binding.textViewName.text = routeItem.name
            binding.viewBeforeLine.setBackgroundColor(routeItem.beforeColor)
            ImageViewCompat.setImageTintList(
                binding.imageViewCurrentLine,
                ColorStateList.valueOf(routeItem.currentColor)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ViewBinding
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            TYPE_FIRST -> {
                binding = ItemRouteFirstBinding.inflate(inflater, parent, false)
                FirstViewHolder(binding)
            }

            TYPE_PATH -> {
                binding = ItemRoutePathBinding.inflate(inflater, parent, false)
                PathViewHolder(binding)
            }

            TYPE_LAST -> {
                binding = ItemRouteLastBinding.inflate(inflater, parent, false)
                LastViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid ViewType")
        }

        binding.root.setOnClickListener {
            onRouteItemClickListener.clickRouteItem(getItem(viewHolder.adapterPosition).coordinate)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FirstViewHolder -> holder.bind(getItem(position))
            is PathViewHolder -> holder.bind(getItem(position))
            is LastViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when ((getItem(position) as RouteItem).type) {
            RouteItemType.FIRST -> TYPE_FIRST
            RouteItemType.PATH -> TYPE_PATH
            RouteItemType.LAST -> TYPE_LAST
        }
    }

    companion object {
        private const val TYPE_FIRST = 0
        private const val TYPE_PATH = 1
        private const val TYPE_LAST = 2

        private val diffUtil = object : DiffUtil.ItemCallback<RouteItem>() {
            override fun areItemsTheSame(oldItem: RouteItem, newItem: RouteItem): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: RouteItem, newItem: RouteItem): Boolean {
                return oldItem == newItem
            }
        }
    }

}