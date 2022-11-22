package com.group19.stashup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group19.stashup.ui.transactions.database.Transaction

class MainViewModel : ViewModel() {
    var loggedIn: Boolean = false
    var menuId: MutableLiveData<Int> = MutableLiveData(R.menu.activity_main_drawer)
    var transaction: Transaction = Transaction()
}