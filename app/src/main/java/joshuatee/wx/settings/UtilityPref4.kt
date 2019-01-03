/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.settings

import android.content.Context
import android.preference.PreferenceManager

object UtilityPref4 {

    fun prefInitSoundingSitesLoc(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        val value = preferences.getString("SND_AOT_X", null)
        if (value == null) {
            // thanks http://web.stcloudstate.edu/raweisman/courses/upperair.txt
            // http://www.esrl.noaa.gov/raobs/intl/intl2000.wmo
            editor.putString("SND_1Y7_X", "32.87")
            editor.putString("SND_1Y7_Y", "114.40")
            editor.putString("SND_76405_X", "24.07")
            editor.putString("SND_76405_Y", "110.33")
            editor.putString("SND_76458_X", "23.18")
            editor.putString("SND_76458_Y", "106.42")
            editor.putString("SND_VBG_X", "34.72")
            editor.putString("SND_VBG_Y", "120.57")
            editor.putString("SND_ABR_X", "45.45")
            editor.putString("SND_ABR_Y", "98.41")
            editor.putString("SND_ALB_X", "42.75")
            editor.putString("SND_ALB_Y", "73.80")
            editor.putString("SND_ABQ_X", "35.03")
            editor.putString("SND_ABQ_Y", "106.62")
            editor.putString("SND_AMA_X", "35.23")
            editor.putString("SND_AMA_Y", "101.70")
            editor.putString("SND_BIS_X", "46.77")
            editor.putString("SND_BIS_Y", "100.75")
            editor.putString("SND_RNK_X", "37.21")
            editor.putString("SND_RNK_Y", "80.41")
            editor.putString("SND_BOI_X", "43.57")
            editor.putString("SND_BOI_Y", "116.22")
            editor.putString("SND_BRO_X", "25.90")
            editor.putString("SND_BRO_Y", "97.43")
            editor.putString("SND_BUF_X", "42.93")
            editor.putString("SND_BUF_Y", "78.73")
            editor.putString("SND_CAR_X", "46.87")
            editor.putString("SND_CAR_Y", "68.02")
            editor.putString("SND_ILX_X", "40.10")
            editor.putString("SND_ILX_Y", "89.30")
            editor.putString("SND_CHS_X", "32.90")
            editor.putString("SND_CHS_Y", "80.03")
            editor.putString("SND_MPX_X", "44.85")
            editor.putString("SND_MPX_Y", "93.57")
            editor.putString("SND_CHH_X", "41.67")
            editor.putString("SND_CHH_Y", "69.97")
            editor.putString("SND_CRP_X", "27.77")
            editor.putString("SND_CRP_Y", "97.50")
            editor.putString("SND_DRT_X", "29.37")
            editor.putString("SND_DRT_Y", "100.92")
            editor.putString("SND_DRA_X", "36.62")
            editor.putString("SND_DRA_Y", "116.02")
            editor.putString("SND_DNR_X", "39.75")
            editor.putString("SND_DNR_Y", "104.87")
            editor.putString("SND_DDC_X", "37.77")
            editor.putString("SND_DDC_Y", "99.97")
            editor.putString("SND_LKN_X", "40.87")
            editor.putString("SND_LKN_Y", "115.73")
            editor.putString("SND_EPZ_X", "31.90")
            editor.putString("SND_EPZ_Y", "106.70")
            editor.putString("SND_FLG_X", "35.23")
            editor.putString("SND_FLG_Y", "111.82")
            editor.putString("SND_FWD_X", "32.80")
            editor.putString("SND_FWD_Y", "97.30")
            editor.putString("SND_APX_X", "44.90")
            editor.putString("SND_APX_Y", "84.72")
            editor.putString("SND_GGW_X", "48.21")
            editor.putString("SND_GGW_Y", "106.63")
            editor.putString("SND_GJT_X", "39.12")
            editor.putString("SND_GJT_Y", "108.53")
            editor.putString("SND_GYX_X", "43.89")
            editor.putString("SND_GYX_Y", "70.25")
            editor.putString("SND_TFX_X", "47.45")
            editor.putString("SND_TFX_Y", "111.38")
            editor.putString("SND_GRB_X", "44.48")
            editor.putString("SND_GRB_Y", "88.13")
            editor.putString("SND_GSO_X", "36.08")
            editor.putString("SND_GSO_Y", "79.95")
            editor.putString("SND_INL_X", "48.57")
            editor.putString("SND_INL_Y", "93.38")
            editor.putString("SND_JAN_X", "32.32")
            editor.putString("SND_JAN_Y", "90.08")
            editor.putString("SND_JAX_X", "30.43")
            editor.putString("SND_JAX_Y", "81.61")
            editor.putString("SND_EYW_X", "24.55")
            editor.putString("SND_EYW_Y", "81.75")
            editor.putString("SND_LCH_X", "30.12")
            editor.putString("SND_LCH_Y", "93.22")
            editor.putString("SND_LZK_X", "34.83")
            editor.putString("SND_LZK_Y", "92.25")
            editor.putString("SND_MFR_X", "42.37")
            editor.putString("SND_MFR_Y", "122.87")
            editor.putString("SND_MFX_X", "25.80")
            editor.putString("SND_MFX_Y", "80.38")
            editor.putString("SND_MAF_X", "31.95")
            editor.putString("SND_MAF_Y", "102.18")
            editor.putString("SND_BNA_X", "36.12")
            editor.putString("SND_BNA_Y", "86.68")
            editor.putString("SND_MHX_X", "34.70")
            editor.putString("SND_MHX_Y", "76.80")
            editor.putString("SND_OUN_X", "35.23")
            editor.putString("SND_OUN_Y", "97.47")
            editor.putString("SND_LBF_X", "41.13")
            editor.putString("SND_LBF_Y", "100.68")
            editor.putString("SND_OAK_X", "37.73")
            editor.putString("SND_OAK_Y", "122.22")
            editor.putString("SND_FFC_X", "33.36")
            editor.putString("SND_FFC_Y", "84.56")
            editor.putString("SND_PIT_X", "40.50")
            editor.putString("SND_PIT_Y", "80.22")
            editor.putString("SND_DVN_X", "41.60")
            editor.putString("SND_DVN_Y", "90.60")
            editor.putString("SND_UIL_X", "47.95")
            editor.putString("SND_UIL_Y", "124.55")
            editor.putString("SND_UNR_X", "44.07")
            editor.putString("SND_UNR_Y", "103.21")
            editor.putString("SND_REV_X", "39.57")
            editor.putString("SND_REV_Y", "119.80")
            editor.putString("SND_RIW_X", "43.00")
            editor.putString("SND_RIW_Y", "108.50")
            editor.putString("SND_SLE_X", "44.92")
            editor.putString("SND_SLE_Y", "123.00")
            editor.putString("SND_SLC_X", "40.78")
            editor.putString("SND_SLC_Y", "111.97")
            editor.putString("SND_NKX_X", "32.73")
            editor.putString("SND_NKX_Y", "117.17")
            editor.putString("SND_BMX_X", "33.17")
            editor.putString("SND_BMX_Y", "86.77")
            editor.putString("SND_SHV_X", "32.45")
            editor.putString("SND_SHV_Y", "93.83")
            editor.putString("SND_SIL_X", "30.25")
            editor.putString("SND_SIL_Y", "89.77")
            editor.putString("SND_OTX_X", "47.68")
            editor.putString("SND_OTX_Y", "117.63")
            editor.putString("SND_SGF_X", "37.14")
            editor.putString("SND_SGF_Y", "93.23")
            editor.putString("SND_LWX_X", "38.98")
            editor.putString("SND_LWX_Y", "77.47")
            editor.putString("SND_NJM_X", "34.69")
            editor.putString("SND_NJM_Y", "77.03")
            editor.putString("SND_TLH_X", "30.56")
            editor.putString("SND_TLH_Y", "84.45")
            editor.putString("SND_TBW_X", "27.70")
            editor.putString("SND_TBW_Y", "82.40")
            editor.putString("SND_TOP_X", "39.07")
            editor.putString("SND_TOP_Y", "95.62")
            editor.putString("SND_TUS_X", "32.12")
            editor.putString("SND_TUS_Y", "110.93")
            editor.putString("SND_OKX_X", "40.86")
            editor.putString("SND_OKX_Y", "72.86")
            editor.putString("SND_OAX_X", "41.32")
            editor.putString("SND_OAX_Y", "96.37")
            editor.putString("SND_WAL_X", "37.85")
            editor.putString("SND_WAL_Y", "75.48")
            editor.putString("SND_DTX_X", "42.68")
            editor.putString("SND_DTX_Y", "83.47")
            editor.putString("SND_ILN_X", "39.42")
            editor.putString("SND_ILN_Y", "83.82")

            editor.putString("SND_ASY_X", "52.72")
            editor.putString("SND_ASY_Y", "174.12")

            editor.putString("SND_ASN_X", "57.15")
            editor.putString("SND_ASN_Y", "170.22")

            editor.putString("SND_ACD_X", "55.20")
            editor.putString("SND_ACD_Y", "162.73")

            editor.putString("SND_ANT_X", "55.03")
            editor.putString("SND_ANT_Y", "131.57")

            editor.putString("SND_AYA_X", "59.52")
            editor.putString("SND_AYA_Y", "139.67")

            editor.putString("SND_ANC_X", "61.17")
            editor.putString("SND_ANC_Y", "150.02")

            editor.putString("SND_ADQ_X", "57.75")
            editor.putString("SND_ADQ_Y", "152.50")

            editor.putString("SND_AKN_X", "58.68")
            editor.putString("SND_AKN_Y", "156.65")

            editor.putString("SND_ABE_X", "60.78")
            editor.putString("SND_ABE_Y", "161.80")

            editor.putString("SND_AMC_X", "62.95")
            editor.putString("SND_AMC_Y", "155.60")

            editor.putString("SND_AFA_X", "64.82")
            editor.putString("SND_AFA_Y", "147.87")

            editor.putString("SND_AOM_X", "64.52")
            editor.putString("SND_AOM_Y", "165.45")

            editor.putString("SND_AOT_X", "66.87")
            editor.putString("SND_AOT_Y", "162.63")
            editor.apply()
        }
    }

    fun prefInitSoundingSites(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        val value = preferences.getString("NWS_SOUNDINGLOCATION_VBG", null)
        if (value == null) {
            // thanks http://web.stcloudstate.edu/raweisman/courses/upperair.txt
            editor.putString("NWS_SOUNDINGLOCATION_VBG", "CA, Vandenberg AFB")
            editor.putString("NWS_SOUNDINGLOCATION_1Y7", "AZ, Yuma")
            editor.putString("NWS_SOUNDINGLOCATION_76405", "MX, La Paz")
            editor.putString("NWS_SOUNDINGLOCATION_76458", "MX, Mazatlan")
            editor.putString("NWS_SOUNDINGLOCATION_SLE", "OR, Salem")
            editor.putString("NWS_SOUNDINGLOCATION_UIL", "WA, Quillayute")
            editor.putString("NWS_SOUNDINGLOCATION_YLW", ", KELOWNA APT")
            editor.putString("NWS_SOUNDINGLOCATION_OAK", "CA, Oakland")
            editor.putString("NWS_SOUNDINGLOCATION_NKX", "CA, San Diego")
            editor.putString("NWS_SOUNDINGLOCATION_TUS", "AZ, Tucson")
            editor.putString("NWS_SOUNDINGLOCATION_DRT", "TX, Del Rio")
            editor.putString("NWS_SOUNDINGLOCATION_RAP", "SD, Rapid City")
            editor.putString("NWS_SOUNDINGLOCATION_INL", "MN, International Falls")
            editor.putString("NWS_SOUNDINGLOCATION_BNA", "TN, Nashville")
            editor.putString("NWS_SOUNDINGLOCATION_YMO", ", MOOSONEE")
            editor.putString("NWS_SOUNDINGLOCATION_GSO", "NC, Greensboro")
            editor.putString("NWS_SOUNDINGLOCATION_WAL", "VA, Wallops Island")
            editor.putString("NWS_SOUNDINGLOCATION_IAD", "DC, Washington")
            editor.putString("NWS_SOUNDINGLOCATION_PIT", "PA, Pittsburgh")
            editor.putString("NWS_SOUNDINGLOCATION_ALB", "NY, Albany")
            editor.putString("NWS_SOUNDINGLOCATION_CHH", "MA, Chatham")
            editor.putString("NWS_SOUNDINGLOCATION_YQI", "Nova Scotia, Yarmouth")
            editor.putString("NWS_SOUNDINGLOCATION_XMR", "FL, Cape Canaveral")
            editor.putString("NWS_SOUNDINGLOCATION_TLH", "FL, Tallahassee")
            editor.putString("NWS_SOUNDINGLOCATION_WPL", "ON, Pickle Lake")
            editor.putString("NWS_SOUNDINGLOCATION_DNR", "CO, Denver")
            editor.apply()
        }
    }
}


