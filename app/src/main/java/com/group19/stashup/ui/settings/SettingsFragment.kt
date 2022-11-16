package com.group19.stashup.ui.settings

import android.content.SharedPreferences
import android.icu.util.Currency
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.group19.stashup.R
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener,
    SearchView.OnQueryTextListener {
    private lateinit var currencyPreference: Preference
    private lateinit var currencyList: ArrayList<String>
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var currencyDialog: AlertDialog

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        getPreferences()
    }

    private fun getPreferences() {
        currencyPreference = preferenceScreen.findPreference("currency")!!
        currencyPreference.onPreferenceClickListener = this
        currencyPreference.summary = sharedPreferences.getString("currency", "CAD")
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            currencyPreference.key -> {
                createCurrencyDialog()
            }
        }
        return false
    }

    /**
     * Creates a dialog for Currency Preference.
     */
    private fun createCurrencyDialog() {
        val builder = AlertDialog.Builder(requireActivity())

        // Inflate view.
        val view = requireActivity().layoutInflater.inflate(R.layout.searchable_list, null)

        // Initialize ListView with items.
        val listView: ListView = view.findViewById(R.id.list_view)
        currencyList = ArrayList()
        val date = Date(System.currentTimeMillis())
        Locale.getAvailableLocales().forEach {
            Currency.getAvailableCurrencyCodes(it, date)?.forEach { code ->
                if (!currencyList.contains(code)) {
                    currencyList.add(code)
                }
            }
        }
        currencyList.sort()
        listAdapter =
            ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, currencyList)
        listView.adapter = listAdapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = listAdapter.getItem(position)
            currencyPreference.summary = item

            sharedPreferences.edit().apply {
                putString(currencyPreference.key, item)
                apply()
            }
            currencyDialog.cancel()
        }

        // Initialize SearchView.
        val searchView: SearchView = view.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(this)

        // Set dialog view.
        builder.setView(view)

        // Create and show dialog.
        currencyDialog = builder.create()
        currencyDialog.show()
    }

    /**
     * On Query text submitted.
     */
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (currencyList.contains(query)) {
            listAdapter.filter.filter(query)
        }
        return false
    }

    /**
     * On Query text changed.
     */
    override fun onQueryTextChange(newText: String?): Boolean {
        listAdapter.filter.filter(newText)
        return false
    }
}