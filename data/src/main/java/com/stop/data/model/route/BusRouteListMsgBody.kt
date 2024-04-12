package com.stop.data.model.route

import com.squareup.moshi.Json

data class BusRouteListMsgBody(
    @Json(name="itemList")
    val busRouteListItemList: List<BusRouteListItem>
)