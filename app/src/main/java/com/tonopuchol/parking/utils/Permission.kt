package com.tonopuchol.parking.utils

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

@Composable
fun PermissionUI(
    context: Context,
    permission: String,
    permissionRationale: String,
    permissionAction: (PermissionAction) -> Unit
) {
    val TAG = "PermissionnUI"

    val permissionGranted = context.checkIfPermissionGranted(
            permission
        )

    if (permissionGranted) {
        Log.d(TAG, "Permission already granted, exiting..")
        permissionAction(PermissionAction.OnPermissionGranted)
        return
    }


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permission provided by user")
            // Permission Accepted
            permissionAction(PermissionAction.OnPermissionGranted)
        } else {
            Log.d(TAG, "Permission denied by user")
            // Permission Denied
            permissionAction(PermissionAction.OnPermissionDenied)
        }
    }


    val showPermissionRationale = context.shouldShowPermissionRationale(
        permission
    )


    if (showPermissionRationale) {
        Log.d(TAG, "Showing permission rationale for $permission")

        Snackbar(
            content = { Text(permissionRationale) },
            action = {
                launcher.launch(permission)
            },
            dismissAction = {
                permissionAction(PermissionAction.OnPermissionDenied)
            }
        )
    } else {
        //Request permissions again
        Log.d(TAG, "Requesting permission for $permission again")
        SideEffect {
            launcher.launch(permission)
        }

    }


}

sealed class PermissionAction {

    object OnPermissionGranted : PermissionAction()

    object OnPermissionDenied : PermissionAction()
}