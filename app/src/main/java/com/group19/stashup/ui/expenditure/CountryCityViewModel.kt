package com.group19.stashup.ui.expenditure

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.group19.stashup.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CountryCityViewModel : ViewModel() {
    var isLoaded: MutableLiveData<Boolean> = MutableLiveData(false)
    private var countryCityList: ArrayList<CountryCityData> = ArrayList()
    var countryList: ArrayList<String> = ArrayList()
    private var cityList: ArrayList<String> = ArrayList()

    /**
     * Loads all countries and cities into [countryCityList].
     * Loads all countries into [countryList].
     */
    fun loadData(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            if (countryList.isNotEmpty()) return@launch

            val countryJson = context.resources.openRawResource(R.raw.country_city).reader()
            JsonReader(countryJson).use {
                it.beginArray {
                    while (it.hasNext()) {
                        val countryCity = Klaxon().parse<CountryCityData>(it)
                        countryCityList.add(countryCity!!)
                    }
                }
            }
            countryCityList.forEach {
                if (countryList.contains(it.countryName)) return@forEach

                countryList.add(it.countryName)
            }
            isLoaded.postValue(true)
        }
    }

    /**
     * Loads all cities from [country] into [cityList].
     */
    fun getCities(country: String): ArrayList<String> {
        run breaking@{
            if (cityList.isNotEmpty()) cityList.clear()

            countryCityList.forEach {
                if (it.countryName == country) {
                    it.cityName.forEach cityLoop@ { city ->
                        if (cityList.contains(city)) return@cityLoop

                        cityList.add(city)
                    }
                    return@breaking
                }
            }
        }
        return cityList
    }
}