package com.group19.stashup.ui.transactions.database

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.group19.stashup.R
import java.util.*
import kotlin.collections.ArrayList

class TransactionRecyclerViewAdapter(
    private var transactions: ArrayList<Transaction>,
    private val context: Context
    ) : RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder>() {
    private var listener: ItemClickListener? = null
    var inflater: LayoutInflater? = null

    /**
     * On create view holder, inflate layout.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        if (inflater == null) {
            inflater = LayoutInflater.from(context)
        }
        return ViewHolder(inflater!!.inflate(R.layout.transaction_listview, parent, false))
    }

    /**
     * For each row of data, set transaction name and transaction cost.
     * Transaction data determined by variable [position].
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get currency code and symbol.
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        val currencyCode = preference.getString("currency", "CAD")
        val currencySymbol = Currency.getInstance(currencyCode).symbol

        // Get transaction data.
        val data = transactions[position]

        // Set transaction name.
        holder.name.text = data.transactionName

        // Set cost per person.
        var cost = "-$currencySymbol ${String.format("%.2f", data.cost)}"
        if (data.ownerUid == data.payerUid) {
            holder.cost.setTextColor(context.getColor(R.color.green))
            cost = "+$currencySymbol ${String.format("%.2f", data.cost)}"
        }
        holder.cost.text = cost
    }

    /**
     * Returns the number of items inside the current list of transactions.
     */
    override fun getItemCount(): Int {
        return transactions.size
    }

    /**
     * Sets the ItemClickListener to [listener] for adapter.
     */
    fun setOnClickListener(itemListener: ItemClickListener) {
        listener = itemListener
    }

    /**
     * Returns the item of [transactions] at [position].
     */
    fun getItem(position: Int): Transaction {
        return transactions[position]
    }

    /**
     * Defines each row of data for the RecyclerViewAdapter.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var name: TextView
        var cost: TextView

        init {
            itemView.setOnClickListener(this)
            name = itemView.findViewById(R.id.name_tv)
            cost = itemView.findViewById(R.id.cost_tv)
        }

        override fun onClick(v: View?) {
            if (listener == null || v == null) return

            listener!!.onItemClick(v, adapterPosition)
        }
    }

    /**
     * OnItemClickListener, function onItemClick to be implemented by user.
     */
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}