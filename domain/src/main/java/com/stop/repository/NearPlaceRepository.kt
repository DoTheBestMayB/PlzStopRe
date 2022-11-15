package com.stop.repository

import com.stop.model.nearplace.Place

interface NearPlaceRepository {

    suspend fun getNearPlaceList(
        version: Int,
        searchKeyword: String,
        centerLon: Float,
        centerLat: Float,
        appKey: String
    ): List<Place>

}