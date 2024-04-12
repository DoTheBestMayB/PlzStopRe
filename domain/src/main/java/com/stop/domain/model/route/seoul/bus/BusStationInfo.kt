package com.stop.domain.model.route.seoul.bus

data class BusStationInfo(
    val startTime: String,
    val lastTime: String,
    val stationName: String,
    val stationId: String, // 정류소 고유 ID station
    val stationNumber: String, // 정류소 번호 stationNo
)