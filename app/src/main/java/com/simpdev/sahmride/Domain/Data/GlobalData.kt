package Domain.Data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport
import com.simpdev.sahmride.Domain.Data.UserData
import com.simpdev.sahmride.R
import java.io.File

var userData: UserData = UserData()
var auth = Firebase.auth
var db = Firebase.firestore
val database = Firebase.database
val storageRef = Firebase.storage.reference;
var context:Context? = null
var pathToProfilePic:String? = null

fun trackCurrentLocation(mapView: MapView, density:Float){
    mapView.location.updateSettings {
        enabled = true
        pulsingEnabled = true
        pulsingColor = R.color.purple_200
        pulsingMaxRadius = 90F
    }
    mapView.viewport.transitionTo(mapView.viewport.makeFollowPuckViewportState(
        FollowPuckViewportStateOptions.Builder()
            .bearing(FollowPuckViewportStateBearing.Constant(0.0))
            .padding(EdgeInsets(200.0 * density, 0.0, 0.0, 0.0))
            .build()
    ))
}
fun flytoLocation(mapView: MapView, coordinates: Point?){
    mapView.getMapboxMap().flyTo(
        CameraOptions.Builder()
            .zoom(14.0)
            .center(coordinates)
            .build(),
        MapAnimationOptions.mapAnimationOptions { duration(1000) }
    )
}
fun drawCircularAnnotation(mapView: MapView, coordinates: Point){
    mapView.annotations.createCircleAnnotationManager().create(
        CircleAnnotationOptions()
            .withPoint(Point.fromLngLat(coordinates.longitude(),coordinates.latitude()))
            .withCircleRadius(8.0)
            .withCircleColor("#ee4e8b")
            .withCircleStrokeWidth(2.0)
            .withCircleStrokeColor("#ffffff")
            .withDraggable(true)
    )
}

fun fetchImageAndSaveToInternalStorage(storageReference: StorageReference, filename: String) {
    val localFile = File.createTempFile(filename, "jpeg")
    storageReference.getFile(localFile).addOnSuccessListener {
        Log.d("jpeg",localFile.path)
        pathToProfilePic = localFile.path
    }.addOnFailureListener {
        Log.d("jpeg","File Failed")
    }
}
fun readImageFromExternalStorage(): Bitmap? {
    if(pathToProfilePic != null)
    {
        val inputStream = context?.contentResolver?.openInputStream(Uri.fromFile(File(pathToProfilePic)))
        return BitmapFactory.decodeStream(inputStream)
    }
    else
    {
        return null
    }
}

fun saveUserDataToFile(firstName:String,lastName:String,email:String,gender:String,isDriver:Boolean){
    val sharedPreference = context?.getSharedPreferences("userData",Context.MODE_PRIVATE)
    var editor = sharedPreference?.edit()
    if(editor != null){
        editor.putBoolean("loggedIn",true)
        editor.putString("firstName",firstName)
        editor.putString("lastName",lastName)
        editor.putString("email",email)
        editor.putString("gender",gender)
        editor.putBoolean("isDriver",isDriver)
        editor.commit()
        Log.d("Data saved","User Data saved to file")
    }
}

fun readUserDataToFile(){
    val sharedPreference = context?.getSharedPreferences("userData",Context.MODE_PRIVATE)
    userData.firstName = sharedPreference?.getString("firstName",null)
    userData.lastName = sharedPreference?.getString("lastName",null)
    userData.email = sharedPreference?.getString("email",null)
    userData.gender = sharedPreference?.getString("gender",null)
    userData.isDriver = sharedPreference?.getBoolean("isDriver",false) == false
}