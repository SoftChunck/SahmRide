package com.simpdev.sahmride

import Domain.Data.auth
import Domain.Data.database
import Domain.Data.userData
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.mapbox.android.core.location.*


fun BroadcastLocation(context: Context, LocationCallback: LocationListeningCallback){
    var driverRef = database.reference.child("driversLocation").child(auth.currentUser?.uid.toString())
    val locationEngine: LocationEngine = LocationEngineProvider.getBestLocationEngine(context)
    val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    var request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
        .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
        .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
        .build()
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post(object : Runnable{
        override fun run() {
            if(userData.active)
            {
                locationEngine.requestLocationUpdates(request,LocationCallback, Looper.myLooper())
                locationEngine.getLastLocation(object :
                    LocationEngineCallback<LocationEngineResult> {
                    override fun onSuccess(result: LocationEngineResult?) {
                        if (result != null) {
                            driverRef.child("lat").setValue(result.lastLocation?.latitude)
                            driverRef.child("lng").setValue(result.lastLocation?.longitude)
                        }
                    }
                    override fun onFailure(exception: Exception) {

                    }

                })
                mainHandler.postDelayed(this,8000)
            }
        }
    })

    var locationUpdater = Runnable { mainHandler }
    var locationThread = Thread(locationUpdater)
    locationThread.start()
}