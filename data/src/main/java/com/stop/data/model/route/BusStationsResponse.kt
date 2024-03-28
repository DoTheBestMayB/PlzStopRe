package com.stop.data.model.route

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BusStationsResponse(
    @Json(name = "msgBody")
    val msgBody: BusStationsMsgBody
)
