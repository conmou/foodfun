package edu.foodfun.service

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.hilt.MyApplication
import edu.foodfun.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GPSService : LifecycleService() {
    @Inject lateinit var userRepository: UserRepository
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallBack: LocationCallback
    private lateinit var mProviderClient: FusedLocationProviderClient

    companion object {
        const val CHANNEL_ID = "1111"
        const val CHANNEL_ID_NAME = "FoodFun_GPS_Service"
        private const val NOTIFICATION_ID = 9083150
    }

    override fun onCreate() {
        super.onCreate()

        mProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 10F
        }

        mLocationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val user = MyApplication.getInstance().currentUserUIStateStateFlow.value?.user ?: return
                val location = locationResult.lastLocation!!
                lifecycleScope.launch {
                    userRepository.updateLocation(user.id!!, GeoPoint(location.latitude, location.longitude))
                }
            }
        }

        try {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(null, null)
            channel.setShowBadge(false)
            val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)

            val notification = createNotification(applicationContext, CHANNEL_ID) ?: NotificationCompat.Builder(this, CHANNEL_ID).build()
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent!!.action == "START") {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
                return START_STICKY
            }
            mProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
        }
        else if (intent.action == "STOP") {
            mProviderClient.removeLocationUpdates(mLocationCallBack)
            stopForeground(true)
            stopSelfResult(startId)
        }
        return START_STICKY
    }

    private fun createNotification(context: Context, channelid: String): Notification? {
        try {
            return NotificationCompat.Builder(context, channelid)
                .setContentTitle("FoodFun")
                .setContentText("GPS裝置獲取中")
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setSmallIcon(R.drawable.pandaeat)
                .build()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }
}