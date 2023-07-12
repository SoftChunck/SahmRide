package Domain.Data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
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
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport
import com.simpdev.sahmride.BackgroundServices.LocationBroadcastService
import com.simpdev.sahmride.Domain.Data.UserData
import com.simpdev.sahmride.Domain.RideDetails
import com.simpdev.sahmride.R
import io.getstream.chat.android.client.ChatClient
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File


var userData: UserData = UserData()
var auth = Firebase.auth
var db = Firebase.firestore
val database = Firebase.database
val storageRef = Firebase.storage.reference;
var context:Context? = null
var pathToProfilePic:String? = null
var currentRideDetails: RideDetails = RideDetails()
var driverCurrentLocation:Point? = null
var driverUid:String? = null
var circleAnnotationManager:CircleAnnotationManager? = null

val priceOfFule:Double = 280.0
val fulePerKm:Double = 0.4

var chatClient:ChatClient? = null

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
    circleAnnotationManager?.create(
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

    val root = Environment.getExternalStorageDirectory()
    val file = File(root.absolutePath + "/SahmRide/Profile/profile.jpg")
//    try {
//        file.createNewFile()
//        val storageReference = storageRef.child("images/${auth.currentUser!!.uid}/profile")
//        storageReference.getFile(file).addOnSuccessListener {
//            val inputStream = context?.contentResolver?.openInputStream(
//                Uri.fromFile(
//                    File(file.path)
//                ))
//            userData.profilePicBitmap = BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
//        }.addOnFailureListener {
//            Log.d("jpeg","File Failed")
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
}
//fun updateActiveStatus(active:Boolean){
//    val sharedPreference = context?.getSharedPreferences("userData",Context.MODE_PRIVATE)
//    var editor = sharedPreference?.edit()
//    if(editor != null){
//        editor.putBoolean("active", active)
//        editor.commit()
//        Log.d("Active Updated","User Data saved to file")
//    }
//}

fun checkActiveStatus(): Boolean {
    val sharedPreference = context?.getSharedPreferences("userData",Context.MODE_PRIVATE)
    Log.d("Active Updated", (sharedPreference?.getBoolean("active",false) == false).toString())
    return sharedPreference?.getBoolean("active",false) == true
}
fun readUserDataToFile(){
    val sharedPreference = context?.getSharedPreferences("userData",Context.MODE_PRIVATE)
    userData.firstName = sharedPreference?.getString("firstName",null)
    userData.lastName = sharedPreference?.getString("lastName",null)
    userData.email = sharedPreference?.getString("email",null)
    userData.gender = sharedPreference?.getString("gender",null)
    userData.isDriver = sharedPreference?.getBoolean("isDriver",false) == true
    userData.active = sharedPreference?.getBoolean("active",false) == true
}

//Start LocationBroadcastService
fun startLocationBroadcastService(){
    val startLocationService = Intent(context,LocationBroadcastService::class.java)
    context?.startService(startLocationService)
    Log.d("Services Start","Services Start")
}

data class ApiResponse(
    val token: String
)
interface ApiService {
    @GET("token")
    suspend fun getApiResponse(
        @Query("uid") uid: String,
    ): ApiResponse
}






