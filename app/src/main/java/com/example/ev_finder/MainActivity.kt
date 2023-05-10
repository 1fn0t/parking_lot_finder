package com.example.ev_finder

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composeweatherapp.weather.api.fetchStations
import com.example.ev_finder.response_classes.Node
import com.example.ev_finder.ui.theme.EVfinderTheme
import com.example.locationservices.DefaultLocationClient
import com.example.locationservices.LocationViewModel
import com.example.locationservices.LocationViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MainActivity : ComponentActivity() {
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geocoder = Geocoder(this)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            0
        )

        setContent {
            EVfinderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var locationField by remember {
                        mutableStateOf(TextFieldValue(""))
                    }
                    var buttonWasPressed by rememberSaveable {
                        mutableStateOf(false)
                    }
                    val locationClient = DefaultLocationClient(
                        this,
                        LocationServices.getFusedLocationProviderClient(this)
                    )
                    var userLatLng: LatLng? by remember {
                        mutableStateOf(null)
                    }
                    val locationModel = viewModel<LocationViewModel>(factory = LocationViewModelFactory(locationClient))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = locationField.text,
                                onValueChange = { changedValue ->
                                    locationField = TextFieldValue(changedValue)
                                },
                                placeholder = {
                                    Text(text = "Enter location")
                                },
                                singleLine = true,
                                modifier = Modifier.width(160.dp)
                            )
                            Button(onClick = {
                                var enteredPosition: Address? = null
                                try {
                                    enteredPosition = geocoder.getFromLocationName(locationField.text, 1)?.get(0)
                                } catch (e: Exception) {
                                    Log.d("Main", "ERROR: $e")
                                }

                                if (enteredPosition == null) {
                                    Toast.makeText(this@MainActivity, "Unable to get coordinates of location", Toast.LENGTH_LONG).show()
                                }
                                enteredPosition?.let {
                                    userLatLng = LatLng(it.latitude, it.longitude)
                                    buttonWasPressed = true
                                }
                            }) {
                                Text(text = "submit")
                            }
                            Button(onClick = {
                                locationModel.getUserLocation()
                                locationModel.userLocation?.let {
                                    userLatLng = LatLng(it.latitude, it.longitude)
                                }
                                buttonWasPressed = true
                            }) {
                                Text(text = "use my location")
                            }
                        }
                        if (buttonWasPressed) {
                            userLatLng?.let {
                                MapWidget(userLatLng = userLatLng!!, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EVfinderTheme {
        Greeting("Android")
    }
}

@Composable
fun MapWidget(
    userLatLng: LatLng,
    modifier: Modifier = Modifier
) {
//    val singapore = LatLng(1.35, 103.87)
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(singapore, 17f)
//    }
    val geocoder = Geocoder(LocalContext.current)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }
    var lots by remember {
        mutableStateOf(emptyList<Node>())
    }
//    var userLocationInfo = geocoder.getFromLocation(userLat, userLon, 1)
    LaunchedEffect(key1 = Unit) {
        val fetchedLots = mutableListOf<Node>()
        fetchStations(userLatLng.latitude, userLatLng.longitude)?.Nodes?.forEach {
            fetchedLots.add(it)
        }
        lots = fetchedLots
    }
    var context = LocalContext.current
    GoogleMap(
        properties = MapProperties(
            isBuildingEnabled = true,
            mapType = MapType.NORMAL,
        ),
        cameraPositionState = cameraPositionState,
        modifier = modifier
    ) {
        val markerIcon = bitmapDescriptorFromVector(context = context, R.drawable.parking)
        Marker(
            state = MarkerState(position = userLatLng),
            title = "You",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )
        lots.forEach {
            val pointDetails = geocoder.getFromLocation(it.Latitude, it.Longitude, 1)
            Log.d("Main", pointDetails.toString())
            Marker(
                state = MarkerState(position = LatLng(
                    it.Latitude, it.Longitude
                )),
                title = pointDetails?.get(0)?.thoroughfare,
                icon = markerIcon
            )
        }
    }
}

private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    // below line is use to generate a drawable.
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)

    // below line is use to set bounds to our vector drawable.
    vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

    // below line is use to create a bitmap for our drawable which we have added.
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    // below line is use to add bitmap in our canvas.
    val canvas = android.graphics.Canvas(bitmap)
    canvas.scale(0.6f, 0.6f)

    // below line is use to draw our vector drawable in canvas.
    vectorDrawable.draw(canvas)

    // after generating our bitmap we are returning our bitmap.
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}