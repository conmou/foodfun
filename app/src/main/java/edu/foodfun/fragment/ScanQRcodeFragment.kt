package edu.foodfun.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import edu.foodfun.R
import edu.foodfun.dialog.UserDetailDialog
import edu.foodfun.enums.UserTemplateType
import javax.inject.Inject

class ScanQRcodeFragment : Fragment() {
    @Inject lateinit var barcodeView: DecoratedBarcodeView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scan_qrcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeView = view.findViewById(R.id.viewBarcode)

        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39))
        barcodeView.decodeContinuous {
            //新增回呼 當關閉時可繼續掃描
            UserDetailDialog(it.text, UserTemplateType.STRANGER).show(childFragmentManager, "FriendInviteDialog")
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
}