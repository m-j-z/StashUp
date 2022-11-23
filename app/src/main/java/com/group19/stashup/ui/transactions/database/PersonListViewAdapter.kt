package com.group19.stashup.ui.transactions.database

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.group19.stashup.R
import java.util.Currency

class PersonListViewAdapter(
    private val dataList: List<String>,
    private val transaction: Transaction,
    private val context: Context
) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null

    /**
     * Return number of items in [dataList].
     */
    override fun getCount(): Int {
        return dataList.size
    }

    /**
     * Returns item at [position] in [dataList].
     */
    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    /**
     * Get Id of item at [position].
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Creates view if null and sets name and cost.
     */
    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var newView = convertView
        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (newView == null) {
            newView = layoutInflater!!.inflate(R.layout.transaction_listview, null)
        }
        newView!!

        // Get currency code and symbol.
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        val currencyCode = preference.getString("currency", "CAD")
        val currencySymbol = Currency.getInstance(currencyCode).symbol

        // Get data from data list at position.
        val data = dataList[position]

        // Get transaction name and cost views.
        val transactionName: TextView = newView.findViewById(R.id.name_tv)
        val transactionCost: TextView = newView.findViewById(R.id.cost_tv)

        // Set transaction name.
        transactionName.text = data

        // Set transaction cost.
        val cost = "$currencySymbol ${String.format("%.2f", transaction.cost / dataList.size)}"
        transactionCost.text = cost

        return newView
    }
}