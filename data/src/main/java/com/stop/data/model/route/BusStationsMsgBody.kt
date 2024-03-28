package com.stop.data.model.route

import com.squareup.moshi.Json

data class BusStationsMsgBody(
    @Json(name="itemList")
    val busStationsItemList: List<BusStationsItem>
)