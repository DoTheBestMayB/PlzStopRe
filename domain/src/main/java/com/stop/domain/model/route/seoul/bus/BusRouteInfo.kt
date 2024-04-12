package com.stop.domain.model.route.seoul.bus

data class BusRouteInfo(
    val busRouteName: String, // 노선명
    val busRouteAbbreviation : String, // 노선약칭
    val busRouteId: String, // 노선 번호
    val term: Int, // 배차간격
    val corpName: String, // 운수사명
    val lastBusTime: String, // 막차시간
)
