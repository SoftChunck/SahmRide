package com.simpdev.sahmride

import android.content.Context


fun BroadcastLocation(context: Context, LocationCallback: LocationListeningCallback){
//    var driverRef = database.reference.child("driversLocation").child(auth.currentUser?.uid.toString())
//    val locationEngine: LocationEngine = LocationEngineProvider.getBestLocationEngine(context)
//    val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
//    val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
//    var request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
//        .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
//        .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
//        .build()
//    val mainHandler = Handler(Looper.getMainLooper())
//    mainHandler.post(object : Runnable{
//        override fun run() {
//            if(userData.active)
//            {
//                locationEngine.requestLocationUpdates(request,LocationCallback, Looper.myLooper())
//                locationEngine.getLastLocation(object :
//                    LocationEngineCallback<LocationEngineResult> {
//                    override fun onSuccess(result: LocationEngineResult?) {
//                        if (result != null) {
//                            driverRef.child("lat").setValue(result.lastLocation?.latitude)
//                            driverRef.child("lng").setValue(result.lastLocation?.longitude)
//                            Log.d("Location Updated","Success"+result.lastLocation?.latitude.toString())
//                        }
//                    }
//                    override fun onFailure(exception: Exception) {
//                                    Log.d("Location Update","Error")
//                    }
//
//                })
//                mainHandler.postDelayed(this,4000)
//            }
//        }
//    })
//
//    var locationUpdater = Runnable { mainHandler }
//    var locationThread = Thread(locationUpdater)
//    locationThread.start()
}