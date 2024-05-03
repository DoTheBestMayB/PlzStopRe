package com.stop.ui.tmap

import android.content.Context
import androidx.core.content.ContextCompat
import com.skt.tmap.TMapPoint
import com.skt.tmap.overlay.TMapPolyLine
import com.stop.domain.model.route.tmap.custom.WalkRoute

class MissionTMap(
    private val context: Context,
    handler: TMapHandler,
) : TMap(context, handler) {

    fun drawMoveLine(nowLocation: TMapPoint, beforeLocation: TMapPoint, id: String, color: Int) {
        val points = arrayListOf(nowLocation, beforeLocation)
        val polyLine = TMapPolyLine(id, points).apply {
            lineColor = ContextCompat.getColor(context, color)
            outLineColor = ContextCompat.getColor(context, color)
        }

        tMapView.addTMapPolyLine(polyLine)
    }

    fun drawWalkLines(points: ArrayList<TMapPoint>, id: String, color: Int) {
        val polyLine = TMapPolyLine(id, points).apply {
            lineColor = ContextCompat.getColor(context, color)
            outLineColor = ContextCompat.getColor(context, color)
        }
        tMapView.addTMapPolyLine(polyLine)
    }

    fun makeWalkRoute(route: WalkRoute, linePoints: ArrayList<TMapPoint>) {
        route.steps.forEach { step ->
            step.lineString.split(" ").forEach { coordinate ->
                val points = coordinate.split(",")

                linePoints.add(TMapPoint(points.last().toDouble(), points.first().toDouble()))
            }
        }
    }

}