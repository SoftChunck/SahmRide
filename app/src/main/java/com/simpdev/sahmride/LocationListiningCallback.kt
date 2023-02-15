package com.simpdev.sahmride

import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import java.lang.ref.WeakReference


class LocationListeningCallback internal constructor(activity: MainScreen) :
    LocationEngineCallback<LocationEngineResult> {

    private val activityWeakReference: WeakReference<MainScreen>

    init {this.activityWeakReference = WeakReference(activity)}

    override fun onSuccess(result: LocationEngineResult) {

        // The LocationEngineCallback interface's method which fires when the device's location has changed.

        result.getLastLocation()
    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location can not be captured
     *
     * @param exception the exception message
     */
    override fun onFailure(exception: Exception) {

        // The LocationEngineCallback interface's method which fires when the device's location can not be captured

    }
}
