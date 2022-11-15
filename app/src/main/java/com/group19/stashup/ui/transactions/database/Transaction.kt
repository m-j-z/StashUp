package com.group19.stashup.ui.transactions.database

import android.os.Parcel
import android.os.Parcelable

data class Transaction(
    var transactionUid: String = "",
    var transactionName: String = "",
    var cost: Double = 0.0,
    var isShared: Boolean = false,
    var ownerUid: String = "",
    var payerUid: String = "",
    var city: String = "",
    var country: String = "",
    var dateEpoch: Long = 0,
    var people: ArrayList<String>
) : Parcelable {
    constructor() : this(people = arrayListOf())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(transactionName)
        parcel.writeDouble(cost)
        parcel.writeByte(if (isShared) 1 else 0)
        parcel.writeString(ownerUid)
        parcel.writeString(payerUid)
        parcel.writeString(city)
        parcel.writeString(country)
        parcel.writeLong(dateEpoch)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction()
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}