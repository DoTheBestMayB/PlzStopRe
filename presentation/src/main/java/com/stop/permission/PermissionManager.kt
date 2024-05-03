package com.stop.permission

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.stop.R

class PermissionManager(fragment: Fragment) {

    private val locationPartialPermissionDialog: AlertDialog.Builder by lazy {
        val context = fragment.requireContext()
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.location_permission_dialog_title))
            .setMessage(context.getString(R.string.location_partial_permission_dialog_message))
            .setNegativeButton(context.getString(R.string.deny)) { _: DialogInterface, _: Int ->
            }
            .setPositiveButton(context.getString(R.string.allow)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:" + context.packageName)
                }
                context.startActivity(intent)
            }
    }
    private val locationPermissionDeniedDialog: AlertDialog.Builder by lazy {
        val context = fragment.requireContext()
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.location_permission_dialog_title))
            .setMessage(context.getString(R.string.location_permission_denied_dialog_message))
            .setNegativeButton(context.getString(R.string.deny)) { _: DialogInterface, _: Int ->
            }
            .setPositiveButton(context.getString(R.string.allow)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:" + context.packageName)
                }
                context.startActivity(intent)
            }
    }

    private var onGranted: () -> Unit = {}
    private var isShowDialog = false
    private var isOkayPartialGranted = false

    private val locationPermissionRequest = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                onGranted()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                if (isOkayPartialGranted) {
                    onGranted()
                }
                if (isShowDialog) {
                    locationPartialPermissionDialog.show()
                }
            }

            else -> {
                if (isShowDialog) {
                    locationPermissionDeniedDialog.show()
                }
            }
        }
    }

    fun getLocationPermission(
        onGranted: () -> Unit,
        isOkayPartialGranted: Boolean,
        isShowDialog: Boolean,
    ) {
        this.onGranted = onGranted
        this.isOkayPartialGranted = isOkayPartialGranted
        this.isShowDialog = isShowDialog
        locationPermissionRequest.launch(LOCATION_PERMISSIONS)
    }

    companion object {
        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}