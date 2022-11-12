package com.group19.stashup.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.group19.stashup.R
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}