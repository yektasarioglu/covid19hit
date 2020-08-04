package com.yektasarioglu.covid19hit.manager

import android.app.Activity
import android.util.Log

import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*

import com.yektasarioglu.base.extension.TAG
import com.yektasarioglu.covid19hit.ui.home.viewmodel.Meter

import java.net.URLEncoder

private typealias PageIndex = Int
private typealias PageSize = Int

private const val DEFAULT_PAGE_SIZE = 20

class SiteKitManager(activity: Activity, apiKey: String) : IManager {

    private val MTAG = TAG

    // SiteKit Variables
    private var searchService: SearchService? = null

    init {
        searchService = SearchServiceFactory.create(activity, URLEncoder.encode(apiKey, "utf-8"))
    }

    override fun onDestroy() {
        searchService = null
    }

    fun search(locationName: String) {
        val textSearchRequest = TextSearchRequest()
        textSearchRequest.query = locationName

        searchService?.textSearch(textSearchRequest, object : SearchResultListener<TextSearchResponse> {
            override fun onSearchResult(textSearchResponse: TextSearchResponse?) {
                val response = StringBuilder("\n")
                response.append("success\n")
                var count = 1
                var addressDetail: AddressDetail
                for (site in textSearchResponse?.sites!!) {
                    addressDetail = site.address
                    response.append(
                        String.format(
                            "[%s]  name: %s, formatAddress: %s, country: %s, countryCode: %s \r\n",
                            "" + count++, site.name, site.formatAddress,
                            if (addressDetail == null) "" else addressDetail.country,
                            if (addressDetail == null) "" else addressDetail.countryCode
                        )
                    )
                }
                Log.d(MTAG, "search result is : $response")
            }

            override fun onSearchError(searchStatus: SearchStatus?) {
                Log.e(MTAG, "onSearchError(${searchStatus?.errorCode})")
            }

        })
    }

    /**
     *  PageSize: The value ranges from 1 to 20. The default value is 20.
     *  PageIndex: number of the current page. The value ranges from 1 to 60. The default value is 1.
     */
    fun searchNearby(location: Coordinate, locationName: String? = null, radius: Meter, searchLanguage: String = "en", locationType: LocationType = LocationType.ADDRESS, pageFilters: Pair<PageIndex, PageSize> = Pair(2, DEFAULT_PAGE_SIZE), searchResultListener: SearchResultListener<NearbySearchResponse?>) {
        // Create a request body.
        val request = NearbySearchRequest()

        request.apply {
            this.location = location
            this.radius = radius.toInt()
            query = locationName
            poiType = locationType // Point of interest
            language = searchLanguage
            pageIndex = pageFilters.first
            pageSize = pageFilters.second
        }

        // Call the nearby place search API.
        searchService!!.nearbySearch(request, searchResultListener)
    }

    companion object {
        const val MAX_PAGE_SIZE = 20
        const val MAX_PAGE_INDEX = 60
    }

}