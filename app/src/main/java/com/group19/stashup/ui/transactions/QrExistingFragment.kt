package com.group19.stashup.ui.transactions

import android.Manifest
import android.os.Bundle
import android.util.Log
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

    // Request permission for camera.
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                scanQrCodeLauncher.launch(null)
            }
        }

    // Launches QR scanner and try to add item to existing view.
    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) {
        try {
            val qrValue = it as QRResult.QRSuccess

            transactionsViewModel.listUpdated.observe(viewLifecycleOwner) { isDone ->
                if (!isDone) return@observe

                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            transactionsViewModel.addTransactionByUid(qrValue.content.rawValue, requireActivity())
        } catch (e: ClassCastException) {
            Log.e("QrScanner", e.message.toString())
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Ask for camera permission and launch QR scanner if possible.
     */
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

    /**
     * Create binding and initialize transactionViewModel.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrExistingBinding.inflate(inflater, container, false)
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]
        return binding.root
    }
}