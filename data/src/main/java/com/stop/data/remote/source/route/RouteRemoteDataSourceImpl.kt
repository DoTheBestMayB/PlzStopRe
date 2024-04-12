package com.stop.data.remote.source.route

import com.squareup.moshi.JsonDataException
import com.stop.domain.model.route.seoul.subway.StationType
import com.stop.data.remote.model.NetworkResult
import com.stop.data.remote.network.ApisDataService
import com.stop.data.remote.network.FakeTmapApiService
import com.stop.data.remote.network.OpenApiSeoulService
import com.stop.data.remote.network.TmapApiService
import com.stop.data.remote.network.WsBusApiService
import com.stop.domain.model.geoLocation.AddressType
import com.stop.domain.model.route.gyeonggi.GyeonggiBusStation
import com.stop.domain.model.route.gyeonggi.GyeonggiBusRoute
import com.stop.domain.model.route.gyeonggi.GyeonggiBusLastTime
import com.stop.domain.model.route.seoul.bus.*
import com.stop.domain.model.route.seoul.subway.*
import com.stop.domain.model.route.seoul.subway.Station
import com.stop.domain.model.route.tmap.RouteRequest
import com.stop.domain.model.route.tmap.custom.Coordinate
import com.stop.domain.model.route.tmap.origin.*
import javax.inject.Inject

internal class RouteRemoteDataSourceImpl @Inject constructor(
    private val fakeTmapApiService: FakeTmapApiService,
    private val tMapApiService: TmapApiService,
    private val openApiSeoulService: OpenApiSeoulService,
    private val wsBusApiService: WsBusApiService,
    private val apisDataService: ApisDataService,
) : RouteRemoteDataSource {

    override suspend fun getRoute(routeRequest: RouteRequest): List<Itinerary> {
        with(
//            tMapApiService.getRoutes(routeRequest.toMap())
            fakeTmapApiService.getRoutes(routeRequest.toMap())
        ) {
            return when (this) {
                is NetworkResult.Success -> {
                    val itineraries = this.data.metaData?.plan?.itineraries
                        ?: throw JsonDataException(NO_RESULT)
                    eraseDuplicateLeg(itineraries)
                }

                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun reverseGeocoding(
        coordinate: Coordinate,
        addressType: AddressType
    ): ReverseGeocodingResponse {
        with(
            tMapApiService.getReverseGeocoding(
                coordinate.latitude,
                coordinate.longitude,
                addressType = addressType.type
            )
        ) {
            return when (this) {
                is NetworkResult.Success -> this.data
                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getSubwayStationCd(
        stationType: StationType,
        stationName: String,
    ): String {
        with(
            openApiSeoulService.getStationInfo(
                serviceName = "SearchInfoBySubwayNameService",
                stationName = stationName,
            )
        ) {
            return when (this) {
                is NetworkResult.Success -> findStationCd(stationType, this.data)
                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getSubwayStations(lineName: String): List<Station> {
        with(
            openApiSeoulService.getSubwayStations(
                serviceName = "SearchSTNBySubwayLineInfo",
                lineName = lineName,
            )
        ) {
            return when (this) {
                is NetworkResult.Success -> this.data.searchStationNameBySubwayLineInfo?.stations
                    ?: throw JsonDataException(NO_RESULT)

                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getSubwayStationLastTime(
        stationId: String,
        transportDirectionType: TransportDirectionType,
        weekType: WeekType,
    ): List<StationLastTime> {
        with(
            openApiSeoulService.getSubwayLastTime(
                serviceName = "SearchLastTrainTimeByIDService",
                stationId = stationId,
                weekTag = weekType.divisionValue,
                inOutTag = when (transportDirectionType) {
                    TransportDirectionType.INNER, TransportDirectionType.TO_END -> "1"
                    TransportDirectionType.OUTER, TransportDirectionType.TO_FIRST -> "2"
                    TransportDirectionType.UNKNOWN -> throw IllegalArgumentException()
                },
            )
        ) {
            return when (this) {
                is NetworkResult.Success -> this.data.searchLastTrainTimeByIDService?.stationLastTimes
                    ?: throw JsonDataException(NO_RESULT)

                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getSeoulBusStationArsId(stationName: String): List<SeoulBusStationInfo> {
        with(wsBusApiService.getBusArsId(stationName)) {
            return when (this) {
                is NetworkResult.Success -> this.data.arsIdMsgBody.busStations
                    ?: throw JsonDataException(NO_RESULT)

                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getSeoulBusRoute(stationId: String): List<SeoulBusRouteInfo> {
        with(wsBusApiService.getBusRoute(stationId)) {
            return when (this) {
                is NetworkResult.Success -> this.data.routeIdMsgBody.busRoutes
                    ?: throw JsonDataException(NO_RESULT)

                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getSeoulBusLastTime(
        stationId: String,
        lineId: String
    ): List<LastTimeInfo> {
        with(wsBusApiService.getBusLastTime(stationId, lineId)) {
            return when (this) {
                is NetworkResult.Success -> this.data.lastTimeMsgBody.lastTimes
                    ?: throw JsonDataException(NO_RESULT)

                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getGyeonggiBusStationId(stationName: String): List<GyeonggiBusStation> {
        with(apisDataService.getBusStationId(stationName)) {
            return when (this) {
                is NetworkResult.Success -> this.data.msgBody.busStations.map { it.toDomain() }
                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getGyeonggiBusRoute(stationId: String): List<GyeonggiBusRoute> {
        with(apisDataService.getBusRouteId(stationId)) {
            return when (this) {
                is NetworkResult.Success -> this.data.msgBody.routes.map { it.toDomain() }
                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getGyeonggiBusLastTime(lineId: String): List<GyeonggiBusLastTime> {
        with(apisDataService.getBusLastTime(lineId)) {
            return when (this) {
                is NetworkResult.Success -> this.data.msgBody.lastTimes.map { it.toDomain() }
                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getGyeonggiBusRouteStations(lineId: String): List<GyeonggiBusStation> {
        with(apisDataService.getBusRouteStations(lineId)) {
            return when (this) {
                is NetworkResult.Success -> this.data.msgBody.stations.map { it.toDomain() }
                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getBusRouteList(routeName: String): List<BusRouteInfo> {
        with(wsBusApiService.getBusRouteList(routeName)) {
            return when (this) {
                is NetworkResult.Success -> this.data.msgBody.busRouteListItemList.map { it.toDomain() }
                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    override suspend fun getBusStations(routeId: String): List<BusStationInfo> {
        with(wsBusApiService.getBusStations(routeId)) {
            return when (this) {
                is NetworkResult.Success -> this.data.msgBody.busStationsItemList.map { it.toDomain() }
                is NetworkResult.Failure -> throw IllegalArgumentException(this.message)
                is NetworkResult.NetworkError -> throw this.exception
                is NetworkResult.Unexpected -> throw this.exception
            }
        }
    }

    private fun eraseDuplicateLeg(itineraries: List<Itinerary>): List<Itinerary> {
        return itineraries.map { itinerary ->
            var beforeInfo: Pair<String, Coordinate>? = null
            var calculatedTotalTime = 0.0
            var calculatedTotalDistance = 0.0

            val newLegs = itinerary.legs.fold(listOf<Leg>()) { legs, leg ->
                val current =
                    Pair(leg.mode, Coordinate(leg.start.lat.toString(), leg.start.lon.toString()))

                if (legs.isEmpty()) {
                    beforeInfo = current
                    calculatedTotalTime += leg.sectionTime
                    calculatedTotalDistance += leg.distance
                    return@fold legs + leg
                }
                if (beforeInfo == current) {
                    return@fold legs
                }
                beforeInfo = current
                calculatedTotalTime += leg.sectionTime
                calculatedTotalDistance += leg.distance
                legs + leg
            }

            with(itinerary) {
                Itinerary(
                    fare = fare,
                    legs = newLegs,
                    pathType = pathType,
                    totalDistance = calculatedTotalDistance,
                    totalTime = calculatedTotalTime.toInt(),
                    transferCount = transferCount,
                    totalWalkDistance = totalWalkDistance,
                    totalWalkTime = totalWalkTime,
                )
            }
        }
    }

    private fun findStationCd(stationType: StationType, data: SubwayStationResponse): String {
        val searchInfoBySubwayNameService = data.searchInfoBySubwayNameService
            ?: throw JsonDataException(NO_RESULT)
        return searchInfoBySubwayNameService.row.firstOrNull { // 중복되는 경우도 있는지 확인필요
            it.lineNum == stationType.lineName
        }?.stationCd ?: throw IllegalArgumentException(NO_SUBWAY_STATION)
    }

    companion object {
        private const val NO_SUBWAY_STATION = "해당하는 지하철역이 없습니다."
        private const val NO_RESULT = "검색 결과가 없습니다."
    }
}