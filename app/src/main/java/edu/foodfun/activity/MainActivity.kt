package edu.foodfun.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vmadalin.easypermissions.models.PermissionRequest
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.service.GPSService

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var bottomNavbar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        bottomNavbar = findViewById(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavbar.setupWithNavController(navController)
        requiredLocationPermissions()
        requiredCameraPermissions()
        beginGPSService()
    }

    override fun onDestroy() {
        super.onDestroy()

        val intent = Intent(applicationContext, GPSService::class.java).apply {
            action = "STOP"
        }
        startForegroundService(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, listOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
            SettingsDialog.Builder(this).rationale("??????????????????????????????????????????????????????").build().show()
        }
    }

    @AfterPermissionGranted(1)
    private fun requiredLocationPermissions() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Already have permission, do the thing
            // ...
        }
        else {
            // Do not have permissions, request them now
            val request = PermissionRequest.Builder(this)
                .code(1)
                .perms(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                .rationale("??????????????????????????????????????????????????????")
                .positiveButtonText("??????")
                .negativeButtonText("??????")
                .build()
            EasyPermissions.requestPermissions(this, request)
        }
    }

    private fun beginGPSService() {
        val intent = Intent(applicationContext, GPSService::class.java).apply {
            action = "START"
        }
        startForegroundService(intent)
    }

    private fun requiredCameraPermissions() {
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show()
            }
        }
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                // ??????
                Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                // ????????????????????????????????????App?????????????????????
                AlertDialog.Builder(this)
                    .setTitle("??????????????????")
                    .setMessage("??????APP????????????????????????????????????")
                    .setPositiveButton("Ok") { _, _ -> requestPermissionLauncher.launch(Manifest.permission.CAMERA) }
                    .show()
            }
            else -> {
                // ????????????????????????????????????
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

}
