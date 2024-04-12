package com.stop.data.remote.model.route.gyeonggi

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "msgBody")
internal data class GyeonggiBusLastTimeMsgBody(
    @Element(name = "busRouteInfoItem")
    val lastTimes: List<GyeonggiBusLastTime>
)
