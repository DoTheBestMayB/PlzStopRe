package com.stop.data.model.route

import com.stop.domain.model.route.seoul.bus.BusRouteInfo

data class BusRouteListItem(
    val busRouteAbrv: String,
    val busRouteId: String,
    val busRouteNm: String,
    val corpNm: String,
    val edStationNm: String,
    val firstBusTm: String,
    val firstLowTm: String,
    val lastBusTm: String,
    val lastBusYn: String,
    val lastLowTm: String,
    val length: String,
    val routeType: String,
    val stStationNm: String,
    val term: String
) {
    fun toDomain(): BusRouteInfo {
        return BusRouteInfo(
            busRouteName = busRouteNm,
            busRouteAbbreviation = busRouteAbrv,
            busRouteId = busRouteId,
            term = term.toInt(),
            corpName = corpNm,
            lastBusTime = lastBusTm,
        )
    }
}