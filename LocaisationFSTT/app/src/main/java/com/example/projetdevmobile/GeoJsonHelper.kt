package com.example.projetdevmobile

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import java.io.InputStream
import com.mapbox.maps.extension.style.expressions.dsl.generated.within
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import android.view.LayoutInflater
import android.widget.TextView
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import com.mapbox.geojson.Point
import com.mapbox.maps.viewannotation.annotatedLayerFeature
import com.mapbox.maps.viewannotation.geometry

object GeoJsonHelper {

    private const val TAG = "GeoJsonHelper"

    @JvmStatic
    fun addGeoJsonLayer(mapView:MapView, style: Style, context: Context, geoJsonAssetName: String) {
        val geoJsonString = loadGeoJsonFromAsset(context, geoJsonAssetName)

        if (geoJsonString != null) {
            Log.d(TAG, "GeoJSON loaded from asset: $geoJsonAssetName")
            val featureCollection = FeatureCollection.fromJson(geoJsonString)

            val featureCount = featureCollection.features()?.size ?: 0
            Toast.makeText(context, "Loaded $featureCount features from GeoJSON", Toast.LENGTH_SHORT).show()

            val sourceId = "geojson-source"
            val layerId = "geojson-layer"

            style.addSource(
                geoJsonSource(sourceId) {
                    featureCollection(featureCollection)
                }
            )


            style.addLayer(
                fillLayer(layerId, sourceId) {
                    fillColor("#3399ff")
                    fillOpacity(0.6)
                    visibility(Visibility.VISIBLE)
                }
            )



            val viewAnnotationManager = mapView.viewAnnotationManager

            mapView.getMapboxMap().addOnMapClickListener { point ->
                val screenCoordinate = mapView.getMapboxMap().pixelForCoordinate(point)
                val queryGeometry = RenderedQueryGeometry(screenCoordinate)

                val gestionnaireAnnotations = mapView.viewAnnotationManager
                gestionnaireAnnotations.removeAllViewAnnotations()

                mapView.getMapboxMap().queryRenderedFeatures(queryGeometry, RenderedQueryOptions(listOf(layerId), null)) { expected ->
                    expected.value?.firstOrNull().let { rendered ->
                    val feature = rendered?.queriedFeature?.feature
                    val propsJson = feature?.properties()
                    val propsText = propsJson?.entrySet()
                            ?.joinToString("\n") { "${it.key}: ${it.value}" }
                            ?: "Aucune propriété"


                    val view = LayoutInflater.from(context).inflate(R.layout.popup_layout, null)
                    val textView = view.findViewById<TextView>(R.id.popup_text)
                    textView.text = propsText

                    Log.d(TAG, "Attributes: "+ propsText)

                    val inflaterAsynchrone = AsyncLayoutInflater(context)

                        viewAnnotationManager.addViewAnnotation(
                            resId = R.layout.popup_layout,
                            options = viewAnnotationOptions {
                                geometry(point)
//                                annotatedLayerFeature(layerId) {
//                                    featureId(feature?.id()!!)
//                                }
                            },
                            asyncInflater = inflaterAsynchrone,
                            asyncInflateCallback = { vueInflatee ->
                                val texte = vueInflatee.findViewById<TextView>(R.id.popup_text)
                                texte.text = propsText
                            }
                        )

                    }
                }

                true
            }


        } else {
            Log.e(TAG, "Failed to load GeoJSON from asset: $geoJsonAssetName")
            Toast.makeText(context, "Failed to load GeoJSON: $geoJsonAssetName", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadGeoJsonFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream: InputStream = context.assets.open(fileName)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading GeoJSON file: $fileName", e)
            null
        }
    }


}
