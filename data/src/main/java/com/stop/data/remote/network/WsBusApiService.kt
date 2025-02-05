package com.stop.data.remote.network

import com.stop.data.model.route.BusRouteListResponse
import com.stop.data.model.route.BusStationsResponse
import com.stop.data.remote.model.NetworkResult
import com.stop.data.remote.model.nowlocation.bus.BusNowLocationResponse
import com.stop.domain.model.route.seoul.bus.BusLastTimeResponse
import com.stop.domain.model.route.seoul.bus.BusRouteResponse
import com.stop.domain.model.route.seoul.bus.BusStationArsIdResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface WsBusApiService {

    @GET(GET_BUS_ARS_URL)
    suspend fun getBusArsId(
        @Query("stSrch") stationName: String,
        @Query("resultType") resultType: String = "json",
    ): NetworkResult<BusStationArsIdResponse>

    @GET(GET_BUS_NOW_LOCATION_URL)
    suspend fun getBusNowLocation(
        @Query("busRouteId") busRouteId: String,
        @Query("resultType") resultType: String = JSON
    ): NetworkResult<BusNowLocationResponse>

    @GET(GET_BUS_LINE_URL)
    suspend fun getBusRoute(
        @Query("arsId") stationId: String,
        @Query("resultType") resultType: String = "json",
    ): NetworkResult<BusRouteResponse>

    @GET(GET_BUS_LAST_TIME_URL)
    suspend fun getBusLastTime(
        @Query("arsId") stationId: String,
        @Query("busRouteId") lineId: String,
        @Query("resultType") resultType: String = "json",
    ): NetworkResult<BusLastTimeResponse>

    @GET(GET_BUS_ROUTE_LIST_URL)
    suspend fun getBusRouteList(
        @Query("strSrch") routeName: String,
        @Query("resultType") resultType: String = JSON,
    ): NetworkResult<BusRouteListResponse>

    @GET(GET_BUS_STATIONS_URL)
    suspend fun getBusStations(
        @Query("busRouteId") routeId: String,
        @Query("resultType") resultType: String = JSON,
    ): NetworkResult<BusStationsResponse>

    companion object {
        private const val GET_BUS_ARS_URL = "stationinfo/getStationByName"
        private const val GET_BUS_NOW_LOCATION_URL = "buspos/getBusPosByRtid"
        private const val JSON = "json"
        private const val GET_BUS_LINE_URL = "stationinfo/getRouteByStation"
        private const val GET_BUS_LAST_TIME_URL = "stationinfo/getBustimeByStation"
        private const val GET_BUS_ROUTE_LIST_URL = "busRouteInfo/getBusRouteList"
        private const val GET_BUS_STATIONS_URL = "busRouteInfo/getStaionByRoute"
    }
}