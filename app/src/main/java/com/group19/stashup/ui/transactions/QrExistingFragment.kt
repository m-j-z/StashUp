package com.group19.stashup.ui.transactions

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.group19.stashup.databinding.FragmentQrExistingBinding
import com.group19.stashup.ui.transactions.database.TransactionsViewModel
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

class QrExistingFragment : Fragment() {

    // Bindings
    private var _binding: FragmentQrExistingBinding? = null
    private val binding get() = _binding!!

    // ViewModels
    private lateinit var transactionsViewModel: TransactionsViewModel

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                scanQrCodeLauncher.launch(null)
            }
        }

    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) {
        val qrValue = it as QRResult.QRSuccess
        transactionsViewModel.addTransactionByUid(qrValue.content.rawValue, requireActivity())
        transactionsViewModel.dataStatus().observe(viewLifecycleOwner) { isDone ->
            if (!isDone) return@observe

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.CAMERA
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            scanQrCodeLauncher.launch(null)
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrExistingBinding.inflate(inflater, container, false)
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        return binding.root
    }
}