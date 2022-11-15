package com.group19.stashup.ui.expenditure

import com.beust.klaxon.Json

class CountryCityData(
    @Json(name = "country_name") val countryName: String,
    @Json(name = "city_name") val cityName: ArrayList<String>
)