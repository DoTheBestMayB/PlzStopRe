package com.stop.data.remote.model.route.gyeonggi

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "msgBody")
internal data class GyeonggiBusStationMsgBody(
    @Element(name = "busStationList")
    val busStations: List<GyeonggiBusStation>
)
