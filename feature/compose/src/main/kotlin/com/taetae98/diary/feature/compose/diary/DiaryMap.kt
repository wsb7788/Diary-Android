package com.taetae98.diary.feature.compose.diary

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.taetae98.diary.feature.common.Permission
import com.taetae98.diary.feature.common.Setting
import com.taetae98.diary.feature.common.isFalse
import com.taetae98.diary.feature.compose.map.NaverMap
import com.taetae98.diary.feature.compose.variable.canAccessFineLocation
import com.taetae98.diary.feature.compose.variable.canAccessLocation
import com.taetae98.diary.feature.resource.StringResource

@Composable
fun DiaryMap(
    modifier: Modifier = Modifier,
    onNaverMapSearch: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val (canAccessLocationState, setCanAccessLocation) = remember { mutableStateOf(Permission.canAccessLocation(context)) }
    val (canAccessFineLocationState, setCanAccessFineLocation) = remember { mutableStateOf(Permission.canAccessFineLocation(context)) }
    val canAccessLocation = canAccessLocationState || canAccessLocation()
    val canAccessFineLocation = canAccessFineLocationState || canAccessFineLocation()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            setCanAccessLocation(Permission.canAccessLocation(context))
            setCanAccessFineLocation(Permission.canAccessFineLocation(context))
        }
    )

    Box(modifier = modifier) {
        NaverMap(
            modifier = modifier,
            onSearch = onNaverMapSearch,
            isLocationButtonEnabled = canAccessLocation
        )

        if (canAccessLocation.isFalse()) {
            DiaryFloatingButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .clickable { Setting.openApplicationDetails(context) },
                text = stringResource(id = StringResource.permission),
                icon = Icons.Rounded.LocationOn
            )
        } else if (canAccessFineLocation.isFalse()) {
            DiaryFloatingButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .clickable { Setting.openApplicationDetails(context) },
                text = stringResource(id = StringResource.fine_location),
                icon = Icons.Rounded.GpsFixed
            )
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
}