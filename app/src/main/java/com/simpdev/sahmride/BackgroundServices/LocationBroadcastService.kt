package com.simpdev.sahmride.BackgroundServices

import Domain.Data.auth
import Domain.Data.database
import Domain.Data.driverCurrentLocation
import Domain.Data.userData
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.mapbox.android.core.location.*
import com.mapbox.geojson.Point
import com.simpdev.sahmride.LocationListeningCallback

class LocationBroadcastService: Service() {
    var LastLocation: Location? = null
    var driverRef = database.reference.child("driversLocation").child(auth.currentUser?.uid.toString())
    val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    var request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
        .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
        .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
        .build()
    val mainHandler = Handler(Looper.getMainLooper())
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val locationCallback = LocationListeningCallback(this)
        val locationEngine: LocationEngine = LocationEngineProvider.getBestLocationEngine(this)
        mainHandler.post(object : Runnable{
            override fun run() {

                Log.d("Services Start",userData.active.toString())
                if(userData.active)
                {
                    locationEngine.requestLocationUpdates(request,locationCallback, Looper.myLooper())
                    locationEngine.getLastLocation(object :
                        LocationEngineCallback<LocationEngineResult> {
                        override fun onSuccess(result: LocationEngineResult?) {
                            if (result?.lastLocation?.latitude != null) {
                                driverCurrentLocation = Point.fromLngLat(result.lastLocation?.longitude!!,result.lastLocation?.latitude!!)
                                LastLocation = result.lastLocation
                                driverRef.child("lat").setValue(result.lastLocation?.latitude)
                                driverRef.child("lng").setValue(result.lastLocation?.longitude)
                                Log.d("Location Updated","Success"+result.lastLocation?.latitude.toString())
                            }
                            else{
                                driverRef.child("lat").setValue(LastLocation?.latitude)
                                driverRef.child("lng").setValue(LastLocation?.longitude)
                                Log.d("Location Updated","Success"+LastLocation?.latitude.toString())
                            }
                        }
                        override fun onFailure(exception: Exception) {
                            Log.d("Location Update","Error")
                        }

                    })
                    mainHandler.postDelayed(this,8000)
                }
            }
        })

        var locationUpdater = Runnable { mainHandler }
        var locationThread = Thread(locationUpdater)
        locationThread.start()
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}