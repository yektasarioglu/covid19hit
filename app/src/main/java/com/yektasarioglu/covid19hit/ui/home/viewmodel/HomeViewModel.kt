package com.yektasarioglu.covid19hit.ui.home.viewmodel

import android.app.Activity
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.huawei.hms.location.LocationCallback
import com.huawei.hms.location.LocationRequest
import com.huawei.hms.location.LocationResult
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.model.*

import com.yektasarioglu.base.android.Theme
import com.yektasarioglu.base.extension.TAG
import com.yektasarioglu.covid19hit.*
import com.yektasarioglu.network.model.Institution
import com.yektasarioglu.network.model.UserLocation
import com.yektasarioglu.network.repository.HealthInstitutionRepository
import com.yektasarioglu.covid19hit.enum.RouteType
import com.yektasarioglu.covid19hit.utils.formatDistance
import com.yektasarioglu.covid19hit.manager.AnalyticsManager
import com.yektasarioglu.covid19hit.manager.LocationKitManager
import com.yektasarioglu.covid19hit.manager.MapKitManager
import com.yektasarioglu.covid19hit.manager.SiteKitManager
import com.yektasarioglu.covid19hit.utils.StringSimilarity
import com.yektasarioglu.network.repository.DirectionsRepository
import com.yektasarioglu.network.request.Coordinates
import com.yektasarioglu.network.request.RouteDirection
import com.yektasarioglu.network.response.RouteResponse
import com.yektasarioglu.network.response.Step
import com.yektasarioglu.network.utils.appContext

import java.util.concurrent.atomic.AtomicBoolean

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

import kotlinx.coroutines.*

typealias Meter = Float

private const val SIMILARITY_THRESHOLD = 0.45

const val DEFAULT_KM_RADIUS = 10000f

class HomeViewModel(
    private val healthInstitutionRepository: HealthInstitutionRepository,
    private val directionsRepository: DirectionsRepository
) : ViewModel() {

    private val MTAG = TAG

    private var circleColor: Int = 0

    // Managers
    private var analyticsManager: AnalyticsManager? = null
    private var mapKitManager: MapKitManager? = null
    private var siteKitManager: SiteKitManager? = null
    private var locationKitManager: LocationKitManager? = null

    private var stepList: List<Step>? = null

    private var isStartNavigationTriggered: Boolean = false
    private var isNearbyHealthInstitutionsFetched = AtomicBoolean(false)

    // This is our pattern -> Direction and Meter(Minute)
    var currentNavigationInfo: MutableLiveData<Pair<String, String>> = MutableLiveData()

    // User location properties
    val userLocation: UserLocation? by lazy { UserLocation() }

    val isCoordinateAvailable by lazy { MutableLiveData<Unit>() }
    val nearbyHealthInstitutionSites by lazy { MutableLiveData<ArrayList<Site>>() }

    override fun onCleared() {
        super.onCleared()
        analyticsManager?.onDestroy()
        siteKitManager?.onDestroy()
        locationKitManager?.onDestroy()
    }

    fun initialize(activity: Activity) {
        initializeManagers(activity)
        requestLocationUpdates()
    }

    fun getOnMapReadyCallback(): OnMapReadyCallback = mapKitManager as OnMapReadyCallback

    fun enableSelectedMarker() {
        mapKitManager?.selectedMarker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.hospital_pointer_mini_active))
        mapKitManager?.selectedMarker?.showInfoWindow()
    }

    fun disableSelectedMarker() {
        mapKitManager?.selectedMarker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.hospital_pointer_mini))
        mapKitManager?.selectedMarker?.hideInfoWindow()
    }

    fun moveCameraToCurrentLocation() {
        mapKitManager?.moveCamera(userLocation?.coordinate?.first!! to userLocation?.coordinate?.second!!)
    }

    fun drawCirclePivotalToCurrentLocation() {
        mapKitManager?.drawCircle(
            CircleOptions()
                .center(LatLng(userLocation?.coordinate?.first!!, userLocation?.coordinate?.second!!))
                .radius(DEFAULT_KM_RADIUS.toDouble())
                .fillColor(circleColor)
                .strokeColor(Color.TRANSPARENT)
        )
    }

    fun markTheSite(site: Site, distanceText: String) {
        val distance = formatDistance(site.distance)

        mapKitManager?.markThePosition(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital_pointer_mini))
                .position(LatLng(site.location.lat, site.location.lng))
                .title(site.name)
                .snippet("$distanceText = $distance")
        )
    }

    fun setOnMarkerClickListener(listener: (marker: Marker) -> Unit) = mapKitManager?.setOnMarkerClickListener {
        analyticsManager?.sendEvent("MarkerSelected", Bundle().apply {
            putString("Site", it.title)
        })

        Log.i(MTAG, "Selected ${it.title}")
        listener(it)
        enableSelectedMarker()
    }

    fun getRoute(`for`: RouteType, onFailed: ((errorMessage: String) -> Unit)? = null) {
        val originCoordinates = Coordinates(userLocation?.coordinate?.first!!, userLocation?.coordinate?.second!!)
        val destinationCoordinates = Coordinates(mapKitManager?.selectedMarker?.position!!.latitude, mapKitManager?.selectedMarker?.position!!.longitude)
        val routeDirection = RouteDirection(origin = originCoordinates, destination = destinationCoordinates)

        viewModelScope.launch {
            var routeResponse: RouteResponse? = null

            when (`for`) {
                RouteType.WALK -> {
                    routeResponse = directionsRepository.getWalkingRoute(routeDirection) {
                        onFailed?.invoke(it)
                    }
                }
                RouteType.DRIVE -> {
                    routeResponse = directionsRepository.getDrivingRoute(routeDirection) {
                        onFailed?.invoke(it)
                    }
                }
                RouteType.BICYCLE -> {
                    routeResponse = directionsRepository.getBicyclingRoute(routeDirection) {
                        onFailed?.invoke(it)
                    }
                }
            }

            stepList = routeResponse?.routes?.first()?.paths?.first()?.steps

            routeResponse?.let {
                mapKitManager?.removePolylines()
                mapKitManager?.generateRoute(it)
            }
        }
    }

    fun removeRoute() {
        mapKitManager?.removePolylines()
        stepList = null
        isStartNavigationTriggered = false
    }

    fun startNavigation() {
        isStartNavigationTriggered = true
    }

    private fun initializeManagers(activity: Activity) {
        analyticsManager = AnalyticsManager(activity)
        locationKitManager = LocationKitManager(activity)
        mapKitManager = MapKitManager()
        siteKitManager = SiteKitManager(activity, BuildConfig.HMS_API_KEY)

        // For testing purposes
        analyticsManager?.sendEvent("XX", Bundle().apply {
            putString("TestProperty1", "TestValue1")
        })
    }

    private fun requestLocationUpdates() {
        locationKitManager?.requestLocationUpdatesWithCallback(
            locationRequest = LocationRequest().apply {
                interval = 10000L
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                needAddress = true // This let you to reach the current address information.
            },
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult != null) {
                        with (locationResult.lastHWLocation) {

                            if (!isNearbyHealthInstitutionsFetched.get()) {
                                if (userLocation?.city == null &&
                                    userLocation?.country == null &&
                                    userLocation?.state == null &&
                                    userLocation?.coordinate == null
                                ) {
                                    userLocation?.city = city
                                    userLocation?.country = countryName
                                    userLocation?.state = state
                                    userLocation?.coordinate = latitude to longitude

                                    isCoordinateAvailable.value = Unit

                                    viewModelScope.launch {
                                        nearbyHealthInstitutionSites.value =
                                            withContext(context = Dispatchers.Default) {
                                                var list = listOf<Site>()
                                                while (list.isEmpty()) {
                                                    Log.i(MTAG, "list is empty")
                                                    list = getNearbyHealthInstitutions()
                                                }
                                                isNearbyHealthInstitutionsFetched.set(true)
                                                list
                                            } as ArrayList<Site>
                                    }
                                }
                            }

                            if (stepList != null) {
                                Log.i(MTAG, "stepList is ${stepList!!.first().latitudeRange?.first}")

                                run loop@ {
                                    stepList?.forEach {

                                        val inPolyline = latitude.toLong() in it.latitudeRange!! && longitude.toLong() in it.longitudeRange!!

                                        if (inPolyline) {
                                            Log.i(MTAG, "inPolyline is $inPolyline and ${it.instruction}")

                                            //val v = GeomagneticField(latitude.toFloat(), longitude.toFloat(), altitude.toFloat(), System.currentTimeMillis())

                                            val b = locationResult.lastLocation.bearingTo(Location("").apply {
                                                latitude = it.endLocation.lat
                                                longitude = it.endLocation.lng
                                            })

                                            currentNavigationInfo.value = it.instruction to "${it.distanceText} (${it.durationText})"

                                            if (isStartNavigationTriggered) {
                                                isStartNavigationTriggered = false
                                                mapKitManager?.moveCamera(
                                                    latLng = userLocation?.coordinate?.first!! to userLocation?.coordinate?.second!!,
                                                    zoom = 19f,
                                                    tilt = 20f,
                                                    bearing = b * -1
                                                )
                                            }

                                            return@loop
                                        }
                                    }
                                }

                            }

                            Log.i(MTAG, "onLocationResult() - Last location city is $city")
                            Log.i(MTAG, "onLocationResult() - Last location countryName is $countryName")
                            Log.i(MTAG, "onLocationResult() - Last location county is $county")
                            Log.i(MTAG, "onLocationResult() - Last location accuracy is $accuracy")
                            Log.i(MTAG, "onLocationResult() - Last location phone is $phone")
                            Log.i(MTAG, "onLocationResult() - Last location postalCode is $postalCode")
                            Log.i(MTAG, "onLocationResult() - Last location state is $state")
                            Log.i(MTAG, "onLocationResult() - Last location street is $street")
                            Log.i(MTAG, "onLocationResult() - Latitude is $latitude")
                            Log.i(MTAG, "onLocationResult() - Longitude is $longitude")
                        }
                    } else Log.i(MTAG, "locationResult is NULL !!")
                }
            })
    }

    fun initializeStyle(theme: Theme?) {
        if (theme == R.style.Theme_Light || theme == null) {
            circleColor = THEME_LIGHT_CIRCLE_COLOR
        } else {
            circleColor = THEME_DARK_CIRCLE_COLOR
            mapKitManager?.mapStyleOptions = MapStyleOptions.loadRawResourceStyle(appContext, R.raw.map_style_dark)
        }
    }

    private suspend fun getNearbyHealthInstitutions(radius: Meter = DEFAULT_KM_RADIUS): List<Site> {
        return CoroutineScope(Dispatchers.IO).async {
            val list = getHealthInstitutions()
            val filteredList = list?.filter { it.city == userLocation?.state?.toUpperCase() }
            Log.i(MTAG, "nearby list is $filteredList")
            Log.i(MTAG, "nearby list's size is ${filteredList?.size}")

            val result = suspendCoroutine<List<Site>> { continuation ->
                addAllNearbyLocations(
                    radius = radius,
                    onEnd = { nearbyHealthInstitutions ->
                        findNearbyOfficialHealthInstitutions(
                            officialHealthInstitutionList = filteredList!!,
                            nearbyHealthInstitutions = nearbyHealthInstitutions
                        ).let { found -> continuation.resume(found) }
                    })
            }

            return@async result
        }.await()
    }

    private suspend fun getHealthInstitutions(): List<Institution>? {
        val result = healthInstitutionRepository.getHealthInstitutionList()
        result?.forEach {
            Log.i(MTAG, "City is ${it.city} and Name is ${it.name}")
        }
        return result
    }

    private fun findNearbyOfficialHealthInstitutions(officialHealthInstitutionList: List<Institution>, nearbyHealthInstitutions: List<Site>): List<Site> {
        val result = mutableListOf<Site>()

        officialHealthInstitutionList.forEach { institution ->
            nearbyHealthInstitutions.forEach { site ->
                StringSimilarity.printSimilarity(institution.name, site.name.toUpperCase())

                val similarity = StringSimilarity.similarity(institution.name, site.name.toUpperCase())

                if (similarity > SIMILARITY_THRESHOLD) {
                    Log.i(MTAG, "findNearbyOfficialHealthInstitutions() - ${institution.name} and ${site.name.toUpperCase()} have %$similarity")
                    result.add(site)
                }

            }
        }

        Log.i(TAG, "------------------------------------------")
        result.forEach {
            Log.i(MTAG, "findNearbyOfficialHealthInstitutions() - site.name is ${it.name}")
        }

        return result
    }

    private inline fun addAllNearbyLocations(radius: Float, crossinline onEnd: (nearbyHealthInstitutions: List<Site>) -> Unit) {
        val nearbyHealthInstitutions = mutableListOf<Site>()

        var j = 0 // Delete this crappy solution when you find the good and robust one
        for (i in 1..SiteKitManager.MAX_PAGE_INDEX) {
            Log.i(TAG, "i is $i")

            siteKitManager?.searchNearby(
                location = Coordinate(userLocation?.coordinate?.first!!, userLocation?.coordinate?.second!!),
                radius = radius,
                searchLanguage = "tr",
                locationType = LocationType.HOSPITAL,
                pageFilters = i to 20,
                searchResultListener = object : SearchResultListener<NearbySearchResponse?> {

                    // Return search results upon a successful search.
                    override fun onSearchResult(results: NearbySearchResponse?) {
                        Log.i("TAG", "Total result count is ${results?.totalCount}")

                        val sites = results!!.sites
                        if (results.totalCount <= 0 || sites == null || sites.size <= 0)
                            return

                        for (site in sites) {
                            Log.i("TAG", "siteId: ${site.siteId}, name: ${site.name}, distance: ${site.distance} address: ${site.address} \r\n")
                        }

                        nearbyHealthInstitutions.addAll(sites)

                        Log.i(MTAG + "CALLBACK", "i is $i")
                        j++
                        if (j == 3)
                            onEnd(nearbyHealthInstitutions)
                    }

                    override fun onSearchError(status: SearchStatus) {
                        Log.i("TAG", "Error : " + status.errorCode + " " + status.errorMessage)
                    }
                }
            )
        }
    }

}