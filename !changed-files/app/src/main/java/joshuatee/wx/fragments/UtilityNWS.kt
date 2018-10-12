/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

*/

package joshuatee.wx.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.ui.UtilityTheme
import joshuatee.wx.util.UtilityImg

object UtilityNWS {

    private val NWS_ICON_V_2 = mapOf(
            "bkn.png" to R.drawable.bkn,
            "blizzard.png" to R.drawable.blizzard,
            "cold.png" to R.drawable.cold,
            "du.png" to R.drawable.du,
            "fc.png" to R.drawable.fc,
            "few.png" to R.drawable.few,
            "fg.png" to R.drawable.fg,
            "fog.png" to R.drawable.fg,
            "fu.png" to R.drawable.fu,
            "fzra.png" to R.drawable.fzra,
            "fzra_sn.png" to R.drawable.fzra_sn,
            "hi_nshwrs.png" to R.drawable.hi_nshwrs,
            "hi_ntsra.png" to R.drawable.hi_ntsra,
            "hi_shwrs.png" to R.drawable.hi_shwrs,
            "hi_tsra.png" to R.drawable.hi_tsra,
            "hot.png" to R.drawable.hot,
            "hz.png" to R.drawable.hz,
            "ip.png" to R.drawable.ip,
            "nbkn.png" to R.drawable.nbkn,
            "nblizzard.png" to R.drawable.nblizzard,
            "ncold.png" to R.drawable.ncold,
            "ndu.png" to R.drawable.ndu,
            "nfc.png" to R.drawable.nfc,
            "nfew.png" to R.drawable.nfew,
            "nfg.png" to R.drawable.nfg,
            "nfog.png" to R.drawable.nfg,
            "nfu.png" to R.drawable.nfu,
            "nfzra.png" to R.drawable.nfzra,
            "nfzra_sn.png" to R.drawable.nfzra_sn,
            "nip.png" to R.drawable.nip,
            "novc.png" to R.drawable.novc,
            "nra_fzra.png" to R.drawable.nra_fzra,
            "nraip.png" to R.drawable.nraip,
            "nra.png" to R.drawable.nra,
            "nra_sn.png" to R.drawable.nra_sn,
            "nsct.png" to R.drawable.nsct,
            "nscttsra.png" to R.drawable.nscttsra,
            "nshra.png" to R.drawable.nshra,
            "nskc.png" to R.drawable.nskc,
            "nsn.png" to R.drawable.nsn,
            "ntor.png" to R.drawable.ntor,
            "ntsra.png" to R.drawable.ntsra,
            "nwind_bkn.png" to R.drawable.nwind_bkn,
            "nwind_few.png" to R.drawable.nwind_few,
            "nwind_ovc.png" to R.drawable.nwind_ovc,
            "nwind_sct.png" to R.drawable.nwind_sct,
            "nwind_skc.png" to R.drawable.nwind_skc,
            "ovc.png" to R.drawable.ovc,
            "ra_fzra.png" to R.drawable.ra_fzra,
            "raip.png" to R.drawable.raip,
            "ra.png" to R.drawable.ra,
            "ra_sn.png" to R.drawable.ra_sn,
            "sct.png" to R.drawable.sct,
            "scttsra.png" to R.drawable.scttsra,
            "shra.png" to R.drawable.shra,
            "skc.png" to R.drawable.skc,
            "sn.png" to R.drawable.sn,
            "tor.png" to R.drawable.tor,
            "tsra.png" to R.drawable.tsra,
            "wind_bkn.png" to R.drawable.wind_bkn,
            "wind_few.png" to R.drawable.wind_few,
            "wind_ovc.png" to R.drawable.wind_ovc,
            "wind_sct.png" to R.drawable.wind_sct,
            "wind_skc.png" to R.drawable.wind_skc,
            "nsleet.png" to R.drawable.nip,
            "sleet.png" to R.drawable.ip,
            "haze.png" to R.drawable.hz,
            "nhaze.png" to R.drawable.novc,
            "nhz.png" to R.drawable.novc,
            "rain_fzra.png" to R.drawable.ra_fzra,
            "nrain_fzra.png" to R.drawable.nra_fzra,
            "nrain_snow.png" to R.drawable.nra_sn,
            "rain_snow.png" to R.drawable.ra_sn,
            "rain.png" to R.drawable.ra,
            "nrain.png" to R.drawable.nra,
            "snow_fzra.png" to R.drawable.fzra_sn,
            "nsnow_fzra.png" to R.drawable.nfzra_sn,
            "ntsra_hi.png" to R.drawable.hi_ntsra,
            "nhi_tsra.png" to R.drawable.nhi_tsra,
            "tsra_hi.png" to R.drawable.hi_tsra,
            "rain_showers.png" to R.drawable.shra,
            "nrain_showers.png" to R.drawable.nshra,
            "snow.png" to R.drawable.sn,
            "nsnow.png" to R.drawable.nsn,
            "tsra_sct.png" to R.drawable.scttsra,
            "ntsra_sct.png" to R.drawable.nscttsra,
            "ra_sn.png" to R.drawable.ra_sn,
            "sn_ip.png" to R.drawable.sn_ip,
            "snow_sleet.png" to R.drawable.sn_ip,
            "nsn_ip.png" to R.drawable.nsn_ip,
            "nsnow_sleet.png" to R.drawable.nsn_ip,
            "rasn.png" to R.drawable.rasn,
            "nrasn.png" to R.drawable.nrasn,
            "hurr.png" to R.drawable.hurr,
            "hurr-noh.png" to R.drawable.hurr_noh,
            "hurricane.png" to R.drawable.hurr,
            "nhurricane.png" to R.drawable.hurr_noh,
            "tropstorm.png" to R.drawable.tropstorm,
            "tropical_storm.png" to R.drawable.tropstorm,
            "ntropical_storm.png" to R.drawable.tropstorm_noh,
            "tropstorm-noh.png" to R.drawable.tropstorm_noh,
            "nmix.png" to R.drawable.nmix,
            "mix.png" to R.drawable.mix,
            "ts.png" to R.drawable.ts,
            "ts_hur_flags.png" to R.drawable.ts_hur_flags,
            "ts_warn.png" to R.drawable.tropstorm_noh,
            "nts_warn.png" to R.drawable.tropstorm_noh,
            "minus_ra.png" to R.drawable.minus_ra,
            "nminus_ra.png" to R.drawable.nminus_ra,
            "nsmoke.png" to R.drawable.nfu,
            "smoke.png" to R.drawable.fu
    )

    fun getIconV2(context: Context, url: String): Bitmap {
        val bm: Bitmap
        if (url == "NULL") {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }
        var fn = url.replace("?size=medium", "")
        fn = fn.replace("?size=small", "")
        fn = fn.replace("https://api.weather.gov/icons/land/", "")
        fn = fn.replace("http://api.weather.gov/icons/land/", "")
        fn = fn.replace("http://nids-wapiapp.bldr.ncep.noaa.gov:9000/icons/land/", "")
        fn = fn.replace("day/", "")
        if (fn.contains("night")) {
            fn = fn.replace("night/", "n")
            fn = fn.replace("/", "/n")
        }
        val fnResId = NWS_ICON_V_2["$fn.png"]
        bm = if (fnResId == null || fn.contains(",")) {
            parseBitmap(context, fn)
        } else {
            UtilityImg.loadBM(context, fnResId, false)
        }
        return bm
    }

    private fun parseBitmap(context: Context, url: String): Bitmap {
        val bm: Bitmap
        val tmpArr: List<String>
        if (url.contains("/")) {
            tmpArr = url.split("/").dropLastWhile { it.isEmpty() } //  snow,20/ovc,20
            bm = if (tmpArr.size > 1) {
                dualBitmapWithNumbers(context, tmpArr[0], tmpArr[1])
            } else {
                UtilityImg.getBlankBitmap()
            }
        } else {
            bm = dualBitmapWithNumbers(context, url)
        }
        return bm
    }

    private fun dualBitmapWithNumbers(context: Context, aF: String, bF: String): Bitmap {
        var a = aF
        var b = bF
        var num1 = ""
        var num2 = ""
        val aSplit = a.split(",").dropLastWhile { it.isEmpty() }
        val bSplit = b.split(",").dropLastWhile { it.isEmpty() }
        if (aSplit.size > 1) {
            num1 = aSplit[1]
        }
        if (bSplit.size > 1) {
            num2 = bSplit[1]
        }
        if (aSplit.isNotEmpty() && bSplit.isNotEmpty()) {
            a = aSplit[0]
            b = bSplit[0]
        }
        val dimens = 86
        val numHeight = 15
        var leftCropA = 4
        var leftCropB = 4
        if (a.contains("fg")) {
            leftCropA = 45
        }
        if (b.contains("fg")) {
            leftCropB = 45
        }
        val bm = Bitmap.createBitmap(dimens, dimens, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        canvas.drawColor(UtilityTheme.primaryColorFromSelectedTheme)
        val fnResId1 = NWS_ICON_V_2["$a.png"]
        val fnResId2 = NWS_ICON_V_2["$b.png"]
        if (fnResId1 == null || fnResId2 == null) {
            return bm
        }
        val bm1Tmp = UtilityImg.loadBM(context, fnResId1, false)
        val bm1 = Bitmap.createBitmap(bm1Tmp, leftCropA, 0, 41, dimens)
        canvas.drawBitmap(bm1, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        val paint = Paint()
        paint.color = MyApplication.nwsIconTextColor
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.isAntiAlias = true
        var xText = 58
        val yText = 84
        val xTextLeft = 2
        if (num2 == "100") {
            xText = 50
        }
        val paintStripe = Paint()
        val red = Color.red(MyApplication.nwsIconBottomColor)
        val green = Color.green(MyApplication.nwsIconBottomColor)
        val blue = Color.blue(MyApplication.nwsIconBottomColor)
        paintStripe.color = Color.argb(200, red, green, blue)
        if (num1 != "") {
            canvas.drawRect(0f, (dimens - numHeight).toFloat(), 41f, dimens.toFloat(), paintStripe)
            canvas.drawText("$num1%", xTextLeft.toFloat(), yText.toFloat(), paint)
        }
        val bm2Tmp = UtilityImg.loadBM(context, fnResId2, false)
        val bm2 = Bitmap.createBitmap(bm2Tmp, leftCropB, 0, 41, dimens) // was 42 change to 40
        canvas.drawBitmap(bm2, 45f, 0f, Paint(Paint.FILTER_BITMAP_FLAG)) // was 42 change to 44
        if (num2 != "") {
            canvas.drawRect(45f, (dimens - numHeight).toFloat(), dimens.toFloat(), dimens.toFloat(), paintStripe)
            canvas.drawText("$num2%", xText.toFloat(), yText.toFloat(), paint)
        }
        return bm
    }

    private fun dualBitmapWithNumbers(context: Context, aF: String): Bitmap {
        var a = aF
        var num1 = ""
        val aSplit = a.split(",").dropLastWhile { it.isEmpty() }
        if (aSplit.size > 1) {
            num1 = aSplit[1]
        }
        if (aSplit.isNotEmpty()) {
            a = aSplit[0]
        }
        val dimens = 86
        val numHeight = 15
        val bm = Bitmap.createBitmap(dimens, dimens, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        canvas.drawColor(UtilityTheme.primaryColorFromSelectedTheme)
        val fnResId1 = NWS_ICON_V_2["$a.png"] ?: return bm
        val bm1Tmp = UtilityImg.loadBM(context, fnResId1, false)
        val bm1 = Bitmap.createBitmap(bm1Tmp, 0, 0, dimens, dimens) // was 41,dimens
        canvas.drawBitmap(bm1, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        val paint = Paint()
        paint.color = MyApplication.nwsIconTextColor
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.isAntiAlias = true
        var xText = 58
        val yText = 84
        if (num1 == "100") {
            xText = 50
        }
        val paintStripe = Paint()
        val red = Color.red(MyApplication.nwsIconBottomColor)
        val green = Color.green(MyApplication.nwsIconBottomColor)
        val blue = Color.blue(MyApplication.nwsIconBottomColor)
        paintStripe.color = Color.argb(200, red, green, blue)
        if (num1 != "") {
            canvas.drawRect(0f, (dimens - numHeight).toFloat(), dimens.toFloat(), dimens.toFloat(), paintStripe)
            canvas.drawText("$num1%", xText.toFloat(), yText.toFloat(), paint)
        }
        return bm
    }
}

