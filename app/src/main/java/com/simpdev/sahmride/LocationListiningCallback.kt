package com.simpdev.sahmride

import android.util.Log
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.simpdev.sahmride.BackgroundServices.LocationBroadcastService
import java.lang.ref.WeakReference


class LocationListeningCallback internal constructor(service : LocationBroadcastService) :
    LocationEngineCallback<LocationEngineResult> {

    private val activityWeakReference: WeakReference<LocationBroadcastService>

    init {this.activityWeakReference = WeakReference(service)}

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
        Log.d("Location Not Captured","Location Not Captured")
    }
}
