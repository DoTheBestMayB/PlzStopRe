package com.stop.domain.repository

import com.stop.domain.model.geoLocation.AddressType
import com.stop.domain.model.route.gyeonggi.GetGyeonggiBusStationIdResponse
import com.stop.domain.model.route.seoul.bus.GetBusLineResponse
import com.stop.domain.model.route.seoul.bus.GetBusStationArsIdResponse
import com.stop.domain.model.route.tmap.RouteRequest
import com.stop.domain.model.route.tmap.custom.Coordinate
import com.stop.domain.model.route.tmap.origin.ReverseGeocodingResponse
import com.stop.domain.model.route.tmap.origin.RouteResponse

interface RouteRepository {

    suspend fun getRoute(routeRequest: RouteRequest): RouteResponse
    suspend fun reverseGeocoding(coordinate: Coordinate, addressType: AddressType): ReverseGeocodingResponse

    suspend fun getSubwayStationCd(stationId: String, stationName: String): String

    suspend fun getSeoulBusStationArsId(stationName: String): GetBusStationArsIdResponse
    suspend fun getSeoulBusLine(stationId: String): GetBusLineResponse
    suspend fun getSeoulBusLastTime(stationId: String, lineId: String): String

    suspend fun getGyeonggiBusStationId(stationName: String): GetGyeonggiBusStationIdResponse
}