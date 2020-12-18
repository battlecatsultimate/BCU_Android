package com.mandarin.bcu.androidutil.lineup.adapters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.battle.BasisSet
import common.system.files.VFile

class LUCastleSetting : Fragment() {

    private var destroyed = false

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.lineup_castle_set, group, false)

        val castle = view.findViewById<ImageView>(R.id.lineupcastle)

        drawCastle(castle)

        val buttons = arrayOf(view.findViewById(R.id.lineupchcannon), view.findViewById(R.id.lineupchlabel), view.findViewById<Button>(R.id.lineupchbase))

        for (i in buttons.indices) {

            buttons[i].setOnClickListener { setNyb(i, castle) }
        }

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (StaticStore.updateCastle) {
                    drawCastle(castle)

                    StaticStore.updateCastle = false
                }

                if (!destroyed)
                    handler.postDelayed(this, 50)
            }
        }

        handler.postDelayed(runnable, 50)

        return view
    }

    private fun setNyb(index: Int, img: ImageView) {
        if (index >= 3) return

        if (BasisSet.current().sele.nyc[index] == 7)
            BasisSet.current().sele.nyc[index] = 0
        else
            BasisSet.current().sele.nyc[index]++

        drawCastle(img)
    }

    private fun drawCastle(img: ImageView) {
        var data: IntArray? = BasisSet.current().sele.nyc

        if (data == null)
            data = intArrayOf(0, 0, 0)

        val result = Bitmap.createBitmap(128, 256, Bitmap.Config.ARGB_8888)

        val path = "./org/castle/"

        val cannon = path + "000/nyankoCastle_000_0" + data[0] + ".png"
        val label = path + "002/nyankoCastle_002_0" + data[1] + ".png"
        val base = path + "003/nyankoCastle_003_0" + data[2] + ".png"

        val cb = VFile.get(cannon).data.img.bimg() as Bitmap
        val lb = VFile.get(label).data.img.bimg() as Bitmap
        val bb = VFile.get(base).data.img.bimg() as Bitmap

        val c = Canvas(result)
        val p = Paint()

        c.drawBitmap(bb, 0f, 125f, p)
        c.drawBitmap(cb, 0f, 0f, p)
        c.drawBitmap(lb, 0f, 128f, p)

        img.setImageBitmap(result)
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyed = true
    }

    companion object {

        fun newInstance(): LUCastleSetting {
            return LUCastleSetting()
        }
    }
}
