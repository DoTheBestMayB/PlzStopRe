package com.stop.data.remote.model.route.gyeonggi

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response")
internal data class GyeonggiBusRouteStationsResponse(
    @Element(name = "msgBody")
    val msgBody: GyeonggiBusRouteStationsMsgBody
)