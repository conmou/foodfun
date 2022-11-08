package edu.foodfun.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import edu.foodfun.R
import edu.foodfun.hilt.MyApplication

class ShowQRcodeFragment: Fragment() {
    lateinit var imgQRcode: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_qrcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgQRcode = view.findViewById(R.id.imgQRcode)
        imgQRcode.setImageBitmap(getQRCodeBitmap())
    }

    private fun getQRCodeBitmap(): Bitmap? {
        val userId = MyApplication.getInstance().currentUserUIStateStateFlow.value?.user?.id
        if(userId?.isEmpty() == true) return null
        val size = 256
        val bits = QRCodeWriter().encode(userId, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}