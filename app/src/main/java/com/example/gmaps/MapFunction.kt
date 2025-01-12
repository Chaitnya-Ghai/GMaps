package com.example.gmaps

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class MapFunction {
    companion object{
   public fun getLocationName(context: Context, coordinates: LatLng): String {
        var address = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
                ?.firstOrNull()?.apply {
                    for (index in 0 until maxAddressLineIndex) {
                        address += getAddressLine(index).appendIfNotBlank(", ")
                    }
                    address += locality.appendIfNotBlank(", ")
                    address += adminArea.appendIfNotBlank(", ")
                    address += countryName.appendIfNotBlank(", ")
                    address += postalCode.appendIfNotBlank(", ")
                }
            address = address.trim().removeSuffix(",")
            if (address.isNotBlank()) {
                return address
            } else return "address not found"

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return address
    }
    }
}
