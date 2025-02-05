package com.stop.data.remote.source.route

import com.stop.domain.model.route.seoul.subway.StationType
import com.stop.domain.model.geoLocation.AddressType
import com.stop.domain.model.route.gyeonggi.*
import com.stop.domain.model.route.seoul.bus.BusRouteInfo
import com.stop.domain.model.route.seoul.bus.BusStationInfo
import com.stop.domain.model.route.seoul.bus.SeoulBusRouteInfo
import com.stop.domain.model.route.seoul.bus.SeoulBusStationInfo
import com.stop.domain.model.route.seoul.bus.LastTimeInfo
import com.stop.domain.model.route.seoul.subway.Station
import com.stop.domain.model.route.seoul.subway.StationLastTime
import com.stop.domain.model.route.seoul.subway.TransportDirectionType
import com.stop.domain.model.route.seoul.subway.WeekType
import com.stop.domain.model.route.tmap.RouteRequest
import com.stop.domain.model.route.tmap.custom.Coordinate
import com.stop.domain.model.route.tmap.origin.Itinerary
import com.stop.domain.model.route.tmap.origin.ReverseGeocodingResponse

internal interface RouteRemoteDataSource {

    suspend fun getRoute(routeRequest: RouteRequest): List<Itinerary>
    suspend fun reverseGeocoding(coordinate: Coordinate, addressType: AddressType): ReverseGeocodingResponse

    suspend fun getSubwayStationCd(stationType: StationType, stationName: String): String
    suspend fun getSubwayStations(lineName: String): List<Station>
    suspend fun getSubwayStationLastTime(
        stationId: String,
        transportDirectionType: TransportDirectionType,
        weekType: WeekType,
    ): List<StationLastTime>

    suspend fun getSeoulBusStationArsId(stationName: String): List<SeoulBusStationInfo>
    suspend fun getSeoulBusRoute(stationId: String): List<SeoulBusRouteInfo>
    suspend fun getSeoulBusLastTime(stationId: String, lineId: String): List<LastTimeInfo>

    suspend fun getGyeonggiBusStationId(stationName: String): List<GyeonggiBusStation>
    suspend fun getGyeonggiBusRoute(stationId: String): List<GyeonggiBusRoute>
    suspend fun getGyeonggiBusLastTime(lineId: String): List<GyeonggiBusLastTime>
    suspend fun getGyeonggiBusRouteStations(lineId: String): List<GyeonggiBusStation>
    suspend fun getBusRouteList(routeName: String): List<BusRouteInfo>
    suspend fun getBusStations(routeId: String): List<BusStationInfo>
}