package com.stop.ui.tmap

import com.skt.tmap.TMapPoint

interface TMapHandler {

    fun alertTMapReady()

    fun setOnLocationChangeListener(location: android.location.Location)

    fun setOnDisableScrollWIthZoomLevelListener()

    fun setPanel(tMapPoint: TMapPoint, isClickedFromPlaceSearch: Boolean)

}