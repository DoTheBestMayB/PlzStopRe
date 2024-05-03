package com.stop.ui.route

import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stop.R
import com.stop.domain.model.route.TransportLastTime
import com.stop.domain.model.route.tmap.RouteRequest
import com.stop.domain.model.route.tmap.custom.Itinerary
import com.stop.domain.model.route.tmap.custom.MoveType
import com.stop.domain.model.route.tmap.custom.Route
import com.stop.domain.model.route.tmap.custom.TransportRoute
import com.stop.domain.model.route.tmap.custom.WalkRoute
import com.stop.domain.usecase.route.GetLastTransportTimeUseCase
import com.stop.domain.usecase.route.GetRouteUseCase
import com.stop.model.ErrorType
import com.stop.model.Event
import com.stop.model.route.ItineraryInfo
import com.stop.model.route.Place
import com.stop.model.route.RouteInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val getRouteUseCase: GetRouteUseCase,
    private val getLastTransportTimeUseCase: GetLastTransportTimeUseCase,
) : ViewModel() {

    private val _origin = MutableLiveData<Place>()
    val origin: LiveData<Place>
        get() = _origin

    private val _destination = MutableLiveData<Place>()
    val destination: LiveData<Place>
        get() = _destination

    private val _routeResponse = MutableLiveData<List<Itinerary>>()
    val routeResponse: LiveData<List<Itinerary>>
        get() = _routeResponse

    private val _itineraryInfo = MutableLiveData<List<ItineraryInfo>>()
    val itineraryInfo: LiveData<List<ItineraryInfo>>
        get() = _itineraryInfo

    private val _selectedItinerary = MutableLiveData<Itinerary>()
    val selectedItinerary: LiveData<Itinerary>
        get() = _selectedItinerary

    private val _lastTimeResponse = MutableLiveData<Event<List<TransportLastTime?>>>()
    val lastTimeResponse: LiveData<Event<List<TransportLastTime?>>>
        get() = _lastTimeResponse

    private val _errorMessage = MutableLiveData<Event<ErrorType>>()
    val errorMessage: LiveData<Event<ErrorType>>
        get() = _errorMessage

    private val _isLoading = MutableLiveData<Event<Boolean>>()
    val isLoading: LiveData<Event<Boolean>>
        get() = _isLoading

    private var clickedItineraryIndex: Int = -1
    var alertDialog: AlertDialog? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        val errorMessage = when (throwable) {
            is SocketTimeoutException -> Event(ErrorType.SOCKET_TIMEOUT_EXCEPTION)
            is UnknownHostException -> Event(ErrorType.UNKNOWN_HOST_EXCEPTION)
            else -> Event(ErrorType.UNKNOWN_EXCEPTION)
        }
        _errorMessage.postValue(errorMessage)
        _isLoading.postValue(Event(false))
    }

    fun patchRoute(isShowError: Boolean = true) {
        val originValue = _origin.value ?: let {
            if (!isShowError) {
                return
            }
            _errorMessage.value = Event(ErrorType.NO_START)
            return
        }

        val destinationValue = _destination.value ?: let {
            if (!isShowError) {
                return
            }
            _errorMessage.value = Event(ErrorType.NO_END)
            return
        }
        _isLoading.value = Event(true)

        val routeRequest = RouteRequest(
            startX = originValue.coordinate.longitude,
            startY = originValue.coordinate.latitude,
            endX = destinationValue.coordinate.longitude,
            endY = destinationValue.coordinate.latitude,
        )

        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            val itineraries = getRouteUseCase(routeRequest)
            if (itineraries.isEmpty()) {
                _errorMessage.postValue(Event(ErrorType.NO_ROUTE_RESULT))
                _routeResponse.postValue(listOf())
                _itineraryInfo.postValue(listOf())
                _isLoading.postValue(Event(false))
                return@launch
            }
            _routeResponse.postValue(itineraries)
            createItineraryInfo(itineraries)
            _isLoading.postValue(Event(false))
        }
    }

    private fun createItineraryInfo(itineraries: List<Itinerary>) {
        _itineraryInfo.postValue(itineraries.map { itinerary ->
            val hour = itinerary.totalTime / 60 / 60
            val minute = itinerary.totalTime / 60 % 60
            ItineraryInfo(
                itinerary.totalDistance,
                minute,
                hour,
                createRouteInfo(itinerary.routes)
            )
        })
    }

    private fun createRouteInfo(routes: List<Route>): List<RouteInfo> {
        val routeInfo = mutableListOf<RouteInfo>()

        for ((index, route) in routes.withIndex()) {
            if (route.mode == MoveType.TRANSFER) {
                continue
            }
            val (typeName, symbolResId) = if (index == routes.size - 1) {
                "하차" to R.drawable.ic_star_white
            } else {
                getTypeName(route) to getRouteItemMode(route)
            }

            routeInfo.add(
                RouteInfo(
                    route.mode,
                    route.sectionTime,
                    route.proportionOfSectionTime,
                    typeName,
                    route.start.name,
                    getRouteItemColor(route),
                    symbolResId
                )
            )
        }
        return routeInfo
    }

    private val walkColor = Color.parseColor("#E2E7EE")
    private val elseColor = Color.parseColor("#EEEEEE")

    private fun getRouteItemColor(route: Route): Int {
        return when (route) {
            is TransportRoute -> Color.parseColor("#${route.routeColor}")
            is WalkRoute -> walkColor
            else -> elseColor
        }
    }

    private fun getRouteItemMode(route: Route): Int {
        return when (route.mode) {
            MoveType.WALK, MoveType.TRANSFER -> R.drawable.ic_walk_white
            MoveType.BUS -> R.drawable.ic_bus_white
            MoveType.SUBWAY -> R.drawable.ic_subway_white
            else -> R.drawable.ic_star_white
        }
    }

    private fun getTypeName(route: Route): String {
        return when (route) {
            is WalkRoute -> "도보"
            is TransportRoute -> getSubwayTypeName(route)
            else -> ""
        }
    }

    private fun getSubwayTypeName(route: TransportRoute): String {
        return when (route.mode) {
            MoveType.SUBWAY -> route.routeInfo.replace("수도권", "")
            MoveType.BUS -> route.routeInfo.split(":")[1]
            else -> route.routeInfo
        }
    }

    fun changeOriginAndDestination() {
        _origin.value = _destination.value.also {
            _destination.value = _origin.value
        }
        patchRoute(false)
    }

    fun calculateLastTransportTime(itineraryInfo: ItineraryInfo) {
        val itinerary =
            _routeResponse.value?.first { it.totalDistance == itineraryInfo.totalDistance }
                ?: throw IllegalArgumentException()
        _selectedItinerary.value = itinerary
        checkClickedItinerary(itinerary)
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            this@RouteViewModel._lastTimeResponse.postValue(
                Event(
                    getLastTransportTimeUseCase(
                        itinerary
                    )
                )
            )
        }
    }

    private fun checkClickedItinerary(itinerary: Itinerary) {
        clickedItineraryIndex = _routeResponse.value?.indexOf(itinerary) ?: -1
    }

    fun setOrigin(place: Place) {
        _origin.value = place
    }

    fun setDestination(place: Place) {
        _destination.value = place
    }
}