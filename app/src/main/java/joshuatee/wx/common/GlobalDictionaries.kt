/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
//modded by ELY M. 

package joshuatee.wx.common

object GlobalDictionaries {

    val nexradProductString = mapOf(
            "N0R" to "DS.p19r0",
            "N1R" to "DS.p19r1",
            "N2R" to "DS.p19r2",
            "N3R" to "DS.p19r3",
            "NSP" to "DS.p28sw",
            "NSW" to "DS.p30sw",
            "N0Q" to "DS.p94r0",
            "N1Q" to "DS.p94r1",
            "N2Q" to "DS.p94r2",
            "N3Q" to "DS.p94r3",
            "N0V" to "DS.p27v0",
            "N1V" to "DS.p27v1",
            "N2V" to "DS.p27v2",
            "N3V" to "DS.p27v3",

            "NCR" to "DS.p37cr",
            "NCZ" to "DS.p38cr",
            "ET" to "DS.p41et",
            "N0U" to "DS.p99v0",
            "N1U" to "DS.p99v1",
            "N2U" to "DS.p99v2",
            "N3U" to "DS.p99v3",
            "N0S" to "DS.56rm0",
            "N1S" to "DS.56rm1",
            "N2S" to "DS.56rm2",
            "N3S" to "DS.56rm3",
            "VIL" to "DS.57vil",
            "STI" to "DS.58sti",
            "TVS" to "DS.61tvs",
            "HI" to "DS.p59hi",
            "DVL" to "DS.134il",
            "EET" to "DS.135et",
            "DSP FIXME" to "DS.138dp",
	    "MDA" to "DS.141md",
            "TZ0" to "DS.180z0",
            "TZ1" to "DS.180z1",
            "TZ2" to "DS.180z2",
            "TR0" to "DS.181r0",
            "TR1" to "DS.181r1",
            "TR2" to "DS.181r2",
            "TR3" to "DS.181r3",
            "TV0" to "DS.182v0",
            "TV1" to "DS.182v1",
            "TV2" to "DS.182v2",
            "TV3" to "DS.182v3",
            "TZL" to "DS.186zl",
            "N1P" to "DS.78ohp",
            "NTP" to "DS.80stp",
            "NVL" to "DS.57vil",
            "N0X" to "DS.159x0",
            "N1X" to "DS.159x1",
            "N2X" to "DS.159x2",
            "N3X" to "DS.159x3",
            "N0C" to "DS.161c0",
            "N1C" to "DS.161c1",
            "N2C" to "DS.161c2",
            "N3C" to "DS.161c3",
            "N0K" to "DS.163k0",
            "N1K" to "DS.163k1",
            "N2K" to "DS.163k2",
            "N3K" to "DS.163k3",
            "DAA" to "DS.170aa",
            "DSA" to "DS.172dt",
            "DSP" to "DS.172dt", // FIXME this should be 138
            "H0C" to "DS.165h0",
            "H1C" to "DS.165h1",
            "H2C" to "DS.165h2",
            "H3C" to "DS.165h3",
            "VWP" to "DS.48vwp",
            "N0B" to "DS.00n1b", // TODO FIXME temp since lowest tilt not populating yet
            "N0G" to "DS.00n1g"  // TODO FIXME should be n0b and n0g
    )

    val wfoToRadarSite = mapOf(
            "ABQ" to "ABX",
            "ABR" to "ABR",
            "AFC" to "AHG",
            "AFG" to "APD",
            "AJK" to "ACG",
            "AKQ" to "AKQ",
            "ALY" to "ENX",
            "AMA" to "AMA",
            "ANC" to "ANC",
            "APX" to "APX",
            "ARX" to "ARX",
            "BGM" to "BGM",
            "BIS" to "BIS",
            "BMX" to "BMX",
            "BOI" to "CBX",
            "BOU" to "FTG",
            "BOX" to "BOX",
            "BRO" to "BRO",
            "BTV" to "CXX",
            "BUF" to "BUF",
            "BYZ" to "BLX",
            "CAE" to "CAE",
            "CAR" to "CBW",
            "CHS" to "CLX",
            "CLE" to "CLE",
            "CRP" to "CRP",
            "CTP" to "CCX",
            "CYS" to "CYS",
            "DDC" to "DDC",
            "DLH" to "DLH",
            "DMX" to "DMX",
            "DTX" to "DTX",
            "DVN" to "DVN",
            "EAX" to "EAX",
            "EKA" to "BHX",
            "EPZ" to "EPZ",
            "EWX" to "EWX",
            "FFC" to "FFC",
            "FGF" to "MVX",
            "FGZ" to "FSX",
            "FSD" to "FSD",
            "FWD" to "FWS",
            "GGW" to "GGW",
            "GID" to "UEX",
            "GJT" to "GJX",
            "GLD" to "GLD",
            "GRB" to "GRB",
            "GRR" to "GRR",
            "GSP" to "GSP",
            "GUM" to "GUA",
            "GYX" to "GYX",
            "HFO" to "HMO",
            "HGX" to "HGX",
            "HNX" to "HNX",
            "HUN" to "HTX",
            "ICT" to "ICT",
            "ILM" to "LTX",
            "ILN" to "ILN",
            "ILX" to "ILX",
            "IND" to "IND",
            "IWX" to "IWX",
            "JAN" to "DGX",
            "JAX" to "JAX",
            "JKL" to "JKL",
            "KEY" to "BYX",
            "LBF" to "LNX",
            "LCH" to "LCH",
            "LIX" to "LIX",
            "LKN" to "LRX",
            "LMK" to "LVX",
            "LOT" to "LOT",
            "LOX" to "VTX",
            "LSX" to "LSX",
            "LUB" to "LBB",
            "LWX" to "LWX",
            "LZK" to "LZK",
            "MAF" to "MAF",
            "MEG" to "NQA",
            "MFL" to "AMX",
            "MFR" to "MAX",
            "MHX" to "MHX",
            "MKX" to "MKX",
            "MLB" to "MLB",
            "MOB" to "MOB",
            "MPX" to "MPX",
            "MQT" to "MQT",
            "MRX" to "MRX",
            "MSO" to "MSX",
            "MTR" to "MUX",
            "OAX" to "OAX",
            "OHX" to "OHX",
            "OKX" to "OKX",
            "OTX" to "OTX",
            "OUN" to "TLX",
            "PAH" to "PAH",
            "PBZ" to "PBZ",
            "PDT" to "PDT",
            "PHI" to "DIX",
            "PIH" to "SFX",
            "PQR" to "RTX",
            "PSR" to "IWA",
            "PUB" to "PUX",
            "RAH" to "RAX",
            "REV" to "RGX",
            "RIW" to "RIW",
            "RLX" to "RLX",
            "RNK" to "FCX",
            "SEW" to "ATX",
            "SGF" to "SGF",
            "SGX" to "NKX",
            "SHV" to "SHV",
            "SJT" to "SJT",
            "SJU" to "JUA",
            "JSJ" to "JUA",
            "SLC" to "MTX",
            "STO" to "DAX",
            "TAE" to "TLH",
            "TBW" to "TBW",
            "TFX" to "TFX",
            "TOP" to "TWX",
            "TSA" to "INX",
            "TWC" to "EMX",
            "UNR" to "UDX",
            "VEF" to "ESX"
    )
}