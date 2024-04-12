package com.stop.data.remote.model.route.gyeonggi

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "msgBody")
internal data class GyeonggiBusRouteStationsMsgBody(
    @Element(name = "busRouteStationList")
    val stations: List<GyeonggiBusStation>
)
