package edu.ap.project.screens

import ItemViewModel
import UserViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.SliderDefaults
import androidx.navigation.NavController
import edu.ap.project.model.Item

@Composable
fun MapScreen(
    itemViewModel: ItemViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current

    // State voor het geselecteerde item
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var mapView: MapView? = null
    var range by remember { mutableStateOf(5f) }

    // OSMDroid configuratie laden
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

    val items by itemViewModel.allItems.observeAsState(emptyList())
    val user by userViewModel.userData.collectAsState(initial = null)

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                mapView = MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(12.0)
                }
                mapView!!
            },
            update = { view ->
                if (user != null) {
                    user?.locationCoordinates?.let {
                        view.controller.setCenter(GeoPoint(it.latitude, it.longitude))
                    }

                    val zoomLevel = calculateZoomLevel(range)
                    view.controller.setZoom(zoomLevel)

                    val userLocation = GeoPoint(user!!.locationCoordinates!!.latitude, user!!.locationCoordinates!!.longitude)
                    view.overlays.clear()

                    val circlePolygon = createCircle(userLocation, range * 1000.0)
                    view.overlays.add(circlePolygon)

                    val markerOverlay = org.osmdroid.views.overlay.ItemizedOverlayWithFocus<org.osmdroid.views.overlay.OverlayItem>(
                        items.filter { item ->
                            calculateDistance(
                                user!!.locationCoordinates?.latitude ?: 0.0,
                                user!!.locationCoordinates?.longitude ?: 0.0,
                                item.location!!.latitude,
                                item.location!!.longitude
                            ) <= range
                        }.map { item ->
                            org.osmdroid.views.overlay.OverlayItem(
                                item.title,
                                item.description,
                                GeoPoint(item.location!!.latitude, item.location!!.longitude)
                            )
                        },
                        object : org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<org.osmdroid.views.overlay.OverlayItem> {
                            override fun onItemSingleTapUp(index: Int, item: org.osmdroid.views.overlay.OverlayItem?): Boolean {
                                if (item != null) {
                                    // Stel het geselecteerde item in
                                    selectedItem = items[index]
                                }
                                return true
                            }

                            override fun onItemLongPress(index: Int, item: org.osmdroid.views.overlay.OverlayItem?): Boolean {
                                return true
                            }
                        },
                        context
                    )

                    markerOverlay.setFocusItemsOnTap(true)
                    view.overlays.add(markerOverlay)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Info-box met praktische info en navigatieknop
        selectedItem?.let { item ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "${item.title}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "${item.description}", style = MaterialTheme.typography.bodyMedium)


                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = {
                        // Navigeer naar de detailpagina van het item
                        navController.navigate("detail/${item.uid}")
                    }) {
                        Text("Bekijk details")
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Slider(
                    value = range,
                    onValueChange = { newRange ->
                        range = if (newRange <= 5) {
                            Math.round(newRange).toFloat()
                        } else {
                            5 + (Math.round((newRange - 5) / 5) * 5).toFloat()
                        }
                    },
                    valueRange = 1f..50f,
                    steps = 48,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        inactiveTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        activeTickColor = MaterialTheme.colorScheme.secondary,
                        inactiveTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                    )
                )
                Text(
                    text = "Bereik: ${range.toInt()} km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        LaunchedEffect(Unit) {
            userViewModel.refreshUserData()
            itemViewModel.getAllItems()
        }
    }
}


fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}

fun createCircle(center: GeoPoint, radiusInMeters: Double, pointsCount: Int = 64): Polygon {
    val polygon = Polygon()
    val points = mutableListOf<GeoPoint>()

    val angleStep = 360.0 / pointsCount
    for (i in 0 until pointsCount) {
        val angle = Math.toRadians(i * angleStep)

        // Omrekeningen van meters naar graden
        val latAdjustment = radiusInMeters / 111320.0
        val lonAdjustment = radiusInMeters / (40008000.0 / 360.0)

        // Verander de locatie volgens de cirkel
        val lat = center.latitude + latAdjustment * Math.cos(angle)
        val lon = center.longitude + lonAdjustment * Math.sin(angle) / Math.cos(Math.toRadians(center.latitude))

        points.add(GeoPoint(lat, lon))
    }

    polygon.points = points
    polygon.fillColor = Color.argb(60, 0, 102, 138)
    polygon.strokeColor = Color.BLUE
    polygon.strokeWidth = 2f
    return polygon
}

fun calculateZoomLevel(range: Float): Double {
    return when {
        range <= 1f -> 15.5
        range <= 2f -> 14.6
        range <= 3f -> 14.0
        range <= 4f -> 13.6
        range <= 5f -> 13.3
        range <= 10f -> 12.3
        range <= 15f -> 11.7
        range <= 20f -> 11.3
        range <= 25f -> 11.0
        range <= 30f -> 10.7
        range <= 35f -> 10.4
        range <= 40f -> 10.2
        range <= 45f -> 10.0
        else -> 9.7
    }
}
