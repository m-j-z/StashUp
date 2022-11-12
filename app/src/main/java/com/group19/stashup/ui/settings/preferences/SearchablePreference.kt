package com.group19.stashup.ui.settings.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.group19.stashup.R
import java.util.*

class SearchablePreference @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet,
    defStyleAttr: Int = 0
) : Preference(context, attributes, defStyleAttr), SearchView.OnQueryTextListener {
    private lateinit var currencyList: ArrayList<String>
    private lateinit var listAdapter: ArrayAdapter<String>

    init {
        widgetLayoutResource = R.layout.searchable_list
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val view = holder.itemView

        val searchView: SearchView = view.findViewById(R.id.search_view)
        val listView: ListView = view.findViewById(R.id.list_view)

        currencyList = ArrayList()
        Currency.getAvailableCurrencies().forEach {
            currencyList.add(it.displayName)
        }

        listAdapter =
            ArrayAdapter(context, android.R.layout.simple_list_item_1, currencyList)
        listView.adapter = listAdapter

        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (currencyList.contains(query)) {
            listAdapter.filter.filter(query)
        } else {
            Toast.makeText(context, "No currency found...", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        listAdapter.filter.filter(newText)
        return false
    }
}