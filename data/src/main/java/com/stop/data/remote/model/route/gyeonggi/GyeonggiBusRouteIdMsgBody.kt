package com.stop.data.remote.model.route.gyeonggi

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response")
internal data class GyeonggiBusRouteIdMsgBody(
    @Element(name = "busRouteList")
    val routes: List<GyeonggiBusRoute>
)
