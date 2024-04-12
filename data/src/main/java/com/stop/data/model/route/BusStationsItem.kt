package com.stop.data.model.route

import com.stop.domain.model.route.seoul.bus.BusStationInfo

data class BusStationsItem(
    val arsId: String,
    val beginTm: String,
    val busRouteAbrv: String,
    val busRouteId: String,
    val busRouteNm: String,
    val direction: String,
    val fullSectDist: String?,
    val gpsX: String,
    val gpsY: String,
    val lastTm: String,
    val posX: String,
    val posY: String,
    val routeType: String,
    val sectSpd: String,
    val section: String,
    val seq: String,
    val station: String,
    val stationNm: String,
    val stationNo: String,
    val transYn: String,
    val trnstnid: String
) {
    fun toDomain(): BusStationInfo {
        return BusStationInfo(
            startTime = beginTm,
            lastTime = lastTm,
            stationName = stationNm,
            stationId = station,
            stationNumber = stationNo
        )
    }
}