package com.stop.model.route

data class ItineraryInfo(
    val totalDistance: Double, // 총 이동 거리
    val minuteOfTotalTime: Int, // 소요 시간 - 분
    val hourOfTotalTime: Int, // 소요 시간 - 시
    val routeInfo: List<RouteInfo>, // 경유지 정보
)