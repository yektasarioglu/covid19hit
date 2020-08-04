package com.yektasarioglu.covid19hit.manager

import android.Manifest
import android.content.res.Resources
import android.graphics.Color
import android.util.Log

import androidx.annotation.RequiresPermission

import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*

import com.yektasarioglu.base.extension.TAG
import com.yektasarioglu.network.response.RouteResponse

import org.json.JSONException

class MapKitManager : OnMapReadyCallback, IManager {

    private var huaweiMap: HuaweiMap? = null
    private var circle: Circle? = null

    private val polylineList = mutableListOf<Polyline>()
    private val pathList = mutableListOf<List<LatLng>>()
    private var latLngBounds: LatLngBounds? = null

    var selectedMarker: Marker? = null

    var mapStyleOptions: MapStyleOptions? = null

    override fun onDestroy() {
        huaweiMap = null
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    override fun onMapReady(map: HuaweiMap?) {

        Log.i(TAG, "onMapReady()")

        huaweiMap = map

        huaweiMap?.apply {
            // FIXME: Use activity context instead of app context

            if (mapStyleOptions != null) {
                try {
                    val result = setMapStyle(mapStyleOptions)
                    if (!result) Log.e("MapKitManager", "Style parsing failed.")
                    else Log.i("MapKitManager", "Yeap done")
                } catch (e: Resources.NotFoundException) {
                    Log.e("MapKitManager", "Can't find style. Error: ", e);
                }
            }

            isMyLocationEnabled = true

            uiSettings.apply {
                isCompassEnabled = true
                isIndoorLevelPickerEnabled = true
                isScrollGesturesEnabled = true
                isScrollGesturesEnabledDuringRotateOrZoom = true
                isMapToolbarEnabled = true
                // isMyLocationButtonEnabled = true // This is pretty much do the same thing with HuaweiMap.setMyLocationEnabled()
                isTiltGesturesEnabled = false
                isRotateGesturesEnabled = true
                isZoomControlsEnabled = true
                isZoomGesturesEnabled = false

                setAllGesturesEnabled(true)

                setPadding(0, 0, 0, 400) // This sets padding to the MapView's action UIs such as zoom in, zoom out or Huawei logo which placed on the left corner
            }
        }
    }

    fun setOnMarkerClickListener(listener: (marker: Marker) -> Unit) {
        huaweiMap?.setOnMarkerClickListener { marker ->
            selectedMarker = marker
            listener(marker)
            true
        }
    }

    fun moveCamera(latLng: Pair<Double, Double>, zoom: Float = 15f, tilt: Float? = null, bearing: Float? = null) {
        if (bearing == null && tilt == null)
            huaweiMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latLng.first, latLng.second), 15f))
        else {
            val cameraPosition = CameraPosition.Builder()
                .bearing(bearing!!)
                .tilt(tilt ?: 0f)
                .zoom(zoom)
                .target(LatLng(latLng.first, latLng.second))
                .build()

            huaweiMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    // TODO: Use this to restrict camera scroll
    fun restrictCameraScrollBound(southwestBoundary: LatLng, northeastBoundary: LatLng) {
        huaweiMap?.setLatLngBoundsForCameraTarget(LatLngBounds(southwestBoundary, northeastBoundary))
    }

    fun drawCircle(circleOptions: CircleOptions) {
        circle = huaweiMap?.addCircle(circleOptions)
    }

    fun markThePosition(markerOptions: MarkerOptions) {
        huaweiMap?.addMarker(markerOptions)
    }

    fun generateRoute(routeResponse: RouteResponse) {
        try {
            if (routeResponse.routes.isEmpty())
                return

            val route = routeResponse.routes.first()

            val sw = LatLng(route.bounds.southwest.lat, route.bounds.southwest.lng)
            val ne = LatLng(route.bounds.northeast.lat, route.bounds.northeast.lng)
            latLngBounds = LatLngBounds(sw, ne)

            // get paths
            val paths = route.paths
            for (i in paths.indices) {
                val path = paths[i]
                val mPath: MutableList<LatLng> = java.util.ArrayList()
                val steps = path.steps
                for (j in steps.indices) {
                    val step = steps[j]
                    val polyline = step.polyline
                    for (k in polyline.indices) {
                        if (j > 0 && k == 0) {
                            continue
                        }
                        val line = polyline[k]
                        val lat = line.lat
                        val lng = line.lng
                        val latLng = LatLng(lat, lng)
                        mPath.add(latLng)
                    }
                }

                pathList.add(i, mPath)
            }

            renderRoutes(pathList, latLngBounds)
        } catch (e: JSONException) {
            Log.e("MapKitManager", "JSONException$e")
        }
    }

    fun removePolylines() {
        for (polyline in polylineList) {
            polyline.remove()
        }
        polylineList.clear()
        pathList.clear()
        latLngBounds = null
    }

    private fun renderRoutes(paths: List<List<LatLng>>?, latLngBounds: LatLngBounds?) {
        if (null == paths || paths.isEmpty() || paths[0].isEmpty()) {
            return
        }

        for (i in paths.indices) {
            val path = paths[i]
            val options = PolylineOptions().color(Color.BLUE).width(5f)

            for (latLng in path) {
                options.add(latLng)
            }

            val polyline: Polyline = huaweiMap!!.addPolyline(options)
            polylineList.add(i, polyline)
        }
    }

}