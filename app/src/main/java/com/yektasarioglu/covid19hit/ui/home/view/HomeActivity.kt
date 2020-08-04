package com.yektasarioglu.covid19hit.ui.home.view

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager

import androidx.lifecycle.Observer

import com.huawei.hms.maps.MapView

import com.yektasarioglu.base.extension.onClickListener
import com.yektasarioglu.base.extension.tint
import com.yektasarioglu.covid19hit.R
import com.yektasarioglu.covid19hit.base.CustomBaseActivity
import com.yektasarioglu.covid19hit.databinding.HomeLayoutBinding
import com.yektasarioglu.covid19hit.enum.RouteType
import com.yektasarioglu.covid19hit.ui.home.viewmodel.HomeViewModel

import org.jetbrains.anko.toast

import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : CustomBaseActivity() {

    override val binding: HomeLayoutBinding by lazy { HomeLayoutBinding.inflate(layoutInflater) }

    private lateinit var mapView: MapView

    private val viewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //toast("Current Theme is ${themeName()}")

        initializeUIElements()

        with (viewModel) {
            initialize(this@HomeActivity)
            initializeStyle(theme = currentTheme)

            isCoordinateAvailable.observe(this@HomeActivity, Observer {
                viewModel.moveCameraToCurrentLocation()
                viewModel.drawCirclePivotalToCurrentLocation()
            })

            nearbyHealthInstitutionSites.observe(this@HomeActivity, Observer {
                Log.i("HomeActivity", "nearbyHealthInstitutionSites -> $it")

                it.forEach { site->
                    Log.i(TAG, "site's lat and long: ${site.location.lat}, ${site.location.lng}")
                    viewModel.markTheSite(site = site, distanceText = resources.getString(R.string.distance))
                }

                viewModel.setOnMarkerClickListener { marker ->
                    toast("Clicked ${marker.title}")
                    with (binding.actionsMenu) {
                        root.visibility = View.VISIBLE
                    }
                }

                toast(getString(R.string.scroll_to_see_more))
            })
        }

        mapView = binding.mapView

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(viewModel.getOnMapReadyCallback())
    }

    override fun onStart() {
        mapView.onStart()
        super.onStart()

        viewModel.requestLocationUpdates()
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
        //viewModel.removeLocationUpdates()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu?) : Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)

        if (currentTheme == R.style.Theme_Light)
            menu?.findItem(R.id.theme)?.setIcon(R.drawable.dark_36)
        else
            menu?.findItem(R.id.theme)?.setIcon(R.drawable.light_36)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.theme -> {
                if (currentTheme == R.style.Theme_Light) {
                    currentTheme = R.style.Theme_Dark
                    restartActivity()
                } else {
                    currentTheme = R.style.Theme_Light
                    restartActivity()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNetworkAvailable() {
        //toast("Houston, we're back !!")
    }

    override fun onNetworkUnavailable() {
        //toast("Houston, we've a problem !!")
    }

    private fun initializeUIElements() {
        binding.toolbar.logo = getDrawable(R.drawable.local_hospital_24)
        binding.toolbar.title = getString(R.string.abbreviated_app_name)

        setSupportActionBar(binding.toolbar)

        with (binding.actionsMenu) {

            cancelImageView.setOnClickListener {
                root.visibility = View.INVISIBLE

                binding.directions.root.visibility = View.INVISIBLE

                disableActionsMenuItems()

                viewModel.currentNavigationInfo.removeObservers(this@HomeActivity)
                viewModel.disableSelectedMarker()
                viewModel.removeRoute()
            }

            goViaBicycleImageView.onClickListener {
                goViaDrivingImageView.tint = null
                goViaWalkingImageView.tint = null

                startNavigationButton.visibility = View.VISIBLE

                it.tint = R.color.colorPrimaryDark
                viewModel.getRoute(`for` = RouteType.BICYCLE, onFailed = this@HomeActivity::showError)
            }

            goViaDrivingImageView.onClickListener {
                goViaBicycleImageView.tint = null
                goViaWalkingImageView.tint = null

                startNavigationButton.visibility = View.VISIBLE

                it.tint = R.color.colorPrimaryDark
                viewModel.getRoute(`for` = RouteType.DRIVE, onFailed = this@HomeActivity::showError)
            }

            goViaWalkingImageView.onClickListener {
                goViaBicycleImageView.tint = null
                goViaDrivingImageView.tint = null

                startNavigationButton.visibility = View.VISIBLE

                it.tint = R.color.colorPrimaryDark
                viewModel.getRoute(`for` = RouteType.WALK, onFailed = this@HomeActivity::showError)
            }

            startNavigationButton.onClickListener {
                toast("Starting Navigation ")

                it.visibility = View.INVISIBLE

                binding.directions.root.visibility = View.VISIBLE

                showLoading()

                viewModel.startNavigation()

                viewModel.currentNavigationInfo.observe(this@HomeActivity, Observer { info: Pair<String, String> ->
                    binding.directions.directionTextView.text = info.first
                    binding.directions.directionDetailTextView.text = info.second

                    hideLoading()
                })

            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.INVISIBLE

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun disableActionsMenuItems() {
        with (binding.actionsMenu) {
            goViaBicycleImageView.tint = null
            goViaDrivingImageView.tint = null
            goViaWalkingImageView.tint = null

            startNavigationButton.visibility = View.INVISIBLE
        }
    }

    private fun showError(message: String) {
        toast(message)
    }

}
