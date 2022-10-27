package com.tonopuchol.parking.main

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import com.tonopuchol.parking.R
import com.tonopuchol.parking.ghent.ParkingData
import com.tonopuchol.parking.ghent.ParkingSort
import com.tonopuchol.parking.parking.ParkingContent
import com.tonopuchol.parking.theme.ParkingTheme
import com.tonopuchol.parking.utils.LocationUtils
import com.tonopuchol.parking.utils.round
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.ArrayList

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocationServices.getFusedLocationProviderClient(this)

        setContent {
            ParkingTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ParkingView()
                }
            }
        }
    }


}

@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(
        MainNavigationItem.List,
        MainNavigationItem.Map
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = stringResource(id = item.title)
                    )
                },
                label = { Text(text = stringResource(id = item.title), fontSize = 9.sp) },
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {

                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingView(viewModel: MainViewModel = viewModel()) {
    val sortMethod by viewModel.sortMethod.collectAsState(initial = ParkingSort.NAME)
    val sortAscending by viewModel.sortAscending.collectAsState(initial = true)
    val data by viewModel.data.collectAsState(initial = null)


    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigation(navController = navController) },
        content = { padding ->
            NavHost(navController, startDestination = MainNavigationItem.List.route) {
                composable(MainNavigationItem.List.route) {
                    ParkingList(
                        modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                        data = data,
                        sortOn = sortMethod,
                        sortAscended = sortAscending,
                        sortChanged = { sm, d ->
                            if (sm != sortMethod) {
                                viewModel.changeSortMethod(sm)
                            }

                            if (d != sortAscending) {
                                viewModel.changeSortDirection(d)
                            }
                        }
                    )
                }
                composable(MainNavigationItem.Map.route) {
                    ParkingMap(data)
                }
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ParkingMap(data: Map<String, ParkingData>?) {
    val scope = rememberCoroutineScope()
    val permissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    when (permissionState.status) {
        is PermissionStatus.Granted -> {
            var search by remember { mutableStateOf("") }

            Column {


                OutlinedTextField(
                    label = { Text(text = stringResource(R.string.search_address)) },
                    value = search,
                    onValueChange = { search = it },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                //SEARCH
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search_content_description)
                                )
                            }
                        )

                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {  })
                )

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        val layout = LayoutInflater.from(context).inflate(R.layout.map, null, false)

                        val map = layout.findViewById<MapView>(R.id.mvMap)
                        map.tileProvider = MapTileProviderBasic(context)
                        map.setTileSource(TileSourceFactory.MAPNIK)
                        val mapController = map.controller
                        mapController.setZoom(16.5)
                        map.setMultiTouchControls(true)

                        val defaultGPS = GeoPoint(51.05, 3.71667)
                        mapController.setCenter(defaultGPS)

                        LocationUtils.getInstance(context)
                        LocationUtils.getCurrentLocation(
                            onSuccess = { loc ->
                                val currentPosition = GeoPoint(loc.latitude, loc.longitude)
                                mapController.setCenter(currentPosition)

                                val startMarker = Marker(map)
                                startMarker.position = (currentPosition)
                                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                startMarker.title = "Current location"
                                startMarker.id = "cur_pos"
                                startMarker.icon =
                                    AppCompatResources.getDrawable(context, R.drawable.ic_position)
                                map.overlays.add(startMarker)

                                val rm = OSRMRoadManager(context, "AGENT_SMITH")

                                scope.launch(Dispatchers.IO) {
                                    data?.forEach { (_, pd) ->
                                        val coordinates =
                                            pd.getLocationInfo().coordinatesForDisplay!!
                                        val pl =
                                            GeoPoint(coordinates.latitude, coordinates.longitude)
                                        val r = rm.getRoad(ArrayList(listOf(currentPosition, pl)))
                                        pd.distance = r.mLength
                                    }

                                    withContext(Dispatchers.Main) {
                                        map.overlays.filterIsInstance<Marker>().forEach {
                                            it.title += " (${data?.get(it.id)?.distance?.round()} km)"
                                        }

                                        map.invalidate() //redraw overlay
                                    }
                                }
                            },
                            onFail = {
                                mapController.setCenter(defaultGPS)
                            }
                        )

                        //TODO: LOOK INTO WHY MAP GETS FUCKED AFTER A WHILE

                        Configuration.getInstance().load(
                            context,
                            androidx.preference.PreferenceManager.getDefaultSharedPreferences(
                                context
                            )
                        )
                        map.zoomController
                        data?.forEach { entry ->
                            val startMarker = Marker(map)
                            val pos = GeoPoint(
                                entry.value.getLocationInfo()?.coordinatesForDisplay?.latitude!!,
                                entry.value.getLocationInfo()?.coordinatesForDisplay?.longitude!!
                            )
                            startMarker.position = (pos)
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            val subDescription = StringBuilder()

                            subDescription.append(context.getString(R.string.capacity_amount))
                            subDescription.append(entry.value.availableCapacity)
                            subDescription.append(" / ")
                            subDescription.append(entry.value.totalCapacity)
                            subDescription.append(", ")
                            subDescription.append(context.getString(R.string.opening_hours))
                            subDescription.append(entry.value.openingTimes ?: "")

                            val title = StringBuilder(entry.value.name ?: "")
                            val street =
                                entry.value.getLocationInfo().roadName?.replace("?", "") ?: ""
                            title.append("\n")
                            title.append(street)

                            startMarker.title = title.toString()
                            startMarker.id = entry.key
                            startMarker.icon =
                                AppCompatResources.getDrawable(context, R.drawable.ic_parking)

                            startMarker.subDescription = subDescription.toString()

                            map.overlays.add(startMarker)
                        }

                        layout
                    }
                )
            }
        }
        is PermissionStatus.Denied -> {
            Column {
                val textToShow = if (permissionState.status.shouldShowRationale) {
                    "Location is important to show the map. Please grant the permission."
                } else {
                    "Location permission required for this feature to be available. " +
                            "Please grant the permission"
                }
                Text(textToShow)
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Request permission")
                }
            }
        }
    }


/*val camPosition = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(3.5, 50.1), 10f)
    }



    GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = camPosition)*/
//GoogleMap(modifier = Modifier.fillMaxSize()) {
/*data?.forEach { entry ->
    Marker(
        state = MarkerState(position = LatLng(entry.value.location.lat, entry.value.location.lon)),
        title = entry.value.name,
        snippet = entry.value.getLocationInfo().roadName
    )
}*/
// }
}

@Composable
fun ParkingList(
    modifier: Modifier = Modifier,
    data: Map<String, ParkingData>?,
    sortOn: ParkingSort,
    sortAscended: Boolean,
    sortChanged: (ParkingSort, Boolean) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        ParkingSort(sortOn, sortAscended, sortChanged)

        if (data == null) {
            CircularProgressIndicator()
            Text("Loading")
        }

        data?.let { d ->

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                content = {
                    items(
                        items = if (sortAscended) {
                            when (sortOn) {
                                ParkingSort.NAME -> d.map { it.value }.sortedBy { it.name }
                                ParkingSort.LAST_UPDATED -> d.map { it.value }
                                    .sortedBy { it.lastUpdate }
                                ParkingSort.FREE_CAPACITY -> d.map { it.value }
                                    .sortedBy { it.availableCapacity }
                                ParkingSort.CATEGORY -> d.map { it.value }.sortedBy { it.category }
                                ParkingSort.OPERATOR -> d.map { it.value }.sortedBy { it.operator }
                                ParkingSort.DISTANCE -> d.map { it.value }
                                    .sortedBy { it.lastUpdate/*.coordinatesForDisplay?.calculateDistance()*/ }
                            }
                        } else {
                            when (sortOn) {
                                ParkingSort.NAME -> d.map { it.value }
                                    .sortedByDescending { it.name }
                                ParkingSort.LAST_UPDATED -> d.map { it.value }
                                    .sortedByDescending { it.lastUpdate }
                                ParkingSort.FREE_CAPACITY -> d.map { it.value }
                                    .sortedByDescending { it.availableCapacity }
                                ParkingSort.CATEGORY -> d.map { it.value }
                                    .sortedByDescending { it.category }
                                ParkingSort.OPERATOR -> d.map { it.value }
                                    .sortedByDescending { it.operator }
                                ParkingSort.DISTANCE -> d.map { it.value }
                                    .sortedByDescending { it.location.lon }
                            }
                        },
                        key = { pd: ParkingData -> pd.id ?: "" }
                    ) {
                        ParkingContent(
                            data = it,
                            modifier = Modifier.padding(
                                start = 8.dp,
                                end = 8.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            )
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingSort(
    sortOn: ParkingSort,
    sortAscended: Boolean,
    sortChanged: (ParkingSort, Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        val sortOnOptions = mapOf(
            Pair(ParkingSort.NAME, stringResource(R.string.sort_name)),
            Pair(ParkingSort.LAST_UPDATED, stringResource(R.string.sort_last_updated)),
            Pair(ParkingSort.FREE_CAPACITY, stringResource(R.string.sort_free_capacity)),
            Pair(ParkingSort.CATEGORY, stringResource(R.string.sort_category)),
            Pair(ParkingSort.OPERATOR, stringResource(R.string.sort_operator)),
        )
        var sortOnExpanded by remember { mutableStateOf(false) }
        var sortOnText = sortOnOptions[sortOn]!!

        ExposedDropdownMenuBox(expanded = sortOnExpanded, onExpandedChange = {
            sortOnExpanded = !sortOnExpanded
        }) {
            OutlinedTextField(
                readOnly = true,
                value = sortOnText,
                onValueChange = { sortOnText = it },
                label = { Text(text = stringResource(R.string.sort_on)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = sortOnExpanded
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.fillMaxWidth(0.5f)
            )

            ExposedDropdownMenu(
                expanded = sortOnExpanded,
                onDismissRequest = {
                    sortOnExpanded = false
                }
            ) {
                sortOnOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        onClick = {
                            sortChanged(selectionOption.key, sortAscended)
                            sortOnText = selectionOption.value
                            sortOnExpanded = false
                        },
                        text = {
                            Text(text = selectionOption.value)

                        }
                    )
                }
            }
        }

        var sortAscendedExpanded by remember { mutableStateOf(false) }
        val sortAscendedOptions = mapOf(
            Pair(true, stringResource(R.string.sort_ascending)),
            Pair(false, stringResource(R.string.sort_descending))
        )

        var sortAscendedText = sortAscendedOptions[sortAscended]!!

        ExposedDropdownMenuBox(expanded = sortAscendedExpanded, onExpandedChange = {
            sortAscendedExpanded = !sortAscendedExpanded
        }) {
            OutlinedTextField(
                readOnly = true,
                value = sortAscendedText,
                onValueChange = { sortAscendedText = it },
                label = { Text(text = stringResource(R.string.sort_direction)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = sortAscendedExpanded
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = sortAscendedExpanded,
                onDismissRequest = {
                    sortAscendedExpanded = false
                }
            ) {
                sortAscendedOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        onClick = {
                            sortChanged(sortOn, selectionOption.key)
                            sortAscendedExpanded = false
                        },
                        text = {
                            Text(text = selectionOption.value)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ParkingTheme {
        ParkingList(
            data = mapOf(
                Pair("1", ParkingData.getFilledForTest()),
                Pair("2", ParkingData.getFilledForTest())
            ),
            sortOn = ParkingSort.NAME,
            sortAscended = true
        ) { _, _ -> }
    }
}