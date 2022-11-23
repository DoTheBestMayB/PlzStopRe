package com.stop.ui.mission

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.skt.tmap.TMapGpsManager
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem
import com.skt.tmap.overlay.TMapMarkerItem2
import com.skt.tmap.overlay.TMapPolyLine
import com.stop.BuildConfig
import com.stop.R

class TMap(
    private val context: Context,
    private val handler: TMapHandler
) {

    private lateinit var tMapView: TMapView
    private var isTracking = true
    private var lineNum = 0
    private val mockLocation = Location("")

    private val onLocationChangeListener = TMapGpsManager.OnLocationChangedListener { location ->
        if (location != null && checkKoreaLocation(location)) {
            //drawMoveLine(TMapPoint(location.latitude, location.longitude))
            tMapView.setLocationPoint(location.latitude, location.longitude)
            movePin(location)
        }
    }

    private fun checkKoreaLocation(location: Location): Boolean {
        return location.longitude > KOREA_LONGITUDE_MIN && location.longitude < KOREA_LONGITUDE_MAX
                && location.latitude > KOREA_LATITUDE_MIN && location.latitude < KOREA_LATITUDE_MAX
    }

    fun init() {
        tMapView = TMapView(context)
        tMapView.setSKTMapApiKey(BuildConfig.TMAP_APP_KEY)
        tMapView.setOnMapReadyListener {
            tMapView.setVisibleLogo(false)
            tMapView.mapType = TMapView.MapType.DEFAULT
            tMapView.zoomLevel = 16

            setTrackingMode()
            handler.alertTMapReady()
        }
        tMapView.setOnEnableScrollWithZoomLevelListener { _, _ ->
            isTracking = false
        }
    }

    fun setTrackingMode() {
        val manager = TMapGpsManager(context).apply {
            minDistance = 2.5F
            provider = TMapGpsManager.PROVIDER_GPS
            openGps()
            provider = TMapGpsManager.PROVIDER_NETWORK
            openGps()
        }

        //manager.setOnLocationChangeListener(onLocationChangeListener)
    }

    fun getTMapView(): TMapView {
        return tMapView
    }

    private fun drawMoveLine(location: TMapPoint, beforeLocationPoint: TMapPoint) {
        val points = arrayListOf(location, beforeLocationPoint)
        Log.d("TMap","points $points lineNum $lineNum")
        val polyLine = TMapPolyLine("person_line$lineNum", points).apply {
            lineColor = Color.MAGENTA
            outLineColor = Color.MAGENTA
        }
        lineNum += 1
        tMapView.addTMapPolyLine(polyLine)
    }

    private fun movePin(location: Location) {
        val marker = TMapMarkerItem().apply {
            id = "marker_person_pin"
            icon = ContextCompat.getDrawable(context, R.drawable.ic_person_pin)?.toBitmap()
            setTMapPoint(location.latitude, location.longitude)
        }

        tMapView.removeTMapMarkerItem("marker_person_pin")
        tMapView.addTMapMarkerItem(marker)

        if (isTracking) {
            tMapView.setCenterPoint(location.latitude, location.longitude, true)
        }
    }

    fun moveLocation(longitude: String, latitude: String) {
        mockLocation.latitude = latitude.toDouble()
        mockLocation.longitude = longitude.toDouble()

        val beforeLocationPoint = tMapView.locationPoint
        drawMoveLine(TMapPoint(mockLocation.latitude, mockLocation.longitude), beforeLocationPoint)
        movePin(mockLocation)
        tMapView.setLocationPoint(latitude.toDouble(), longitude.toDouble())
    }


    companion object {
        private const val KOREA_LATITUDE_MIN = 32.814978
        private const val KOREA_LATITUDE_MAX = 39.036253

        private const val KOREA_LONGITUDE_MIN = 124.661865
        private const val KOREA_LONGITUDE_MAX = 132.550049
    }
}