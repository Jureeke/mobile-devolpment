package edu.ap.project.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun MapScreen() {
    val context = LocalContext.current

    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

        AndroidView(
            factory = { ctx ->
                val mapView = MapView(ctx)
                mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)

                val mapController = mapView.controller
                mapController.setZoom(15.0)
                val startPoint = GeoPoint(51.2302, 4.4165)
                mapController.setCenter(startPoint)
                mapView
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        )
}
