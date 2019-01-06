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

package joshuatee.wx.objects

import android.content.Context
import android.content.Intent
import android.net.Uri

class ObjectIntent() {

    constructor(
        context: Context,
        clazz: Class<*>,
        url: String,
        stringArray: Array<String>
    ) : this() {
        val intent = Intent(context, clazz)
        intent.putExtra(url, stringArray)
        context.startActivity(intent)
    }

    constructor(
        context: Context,
        clazz: Class<*>,
        url: String,
        stringArray: String,
        dummyFlag: Boolean
    ) : this() {
        val intent = Intent(context, clazz)
        intent.putExtra(url, stringArray)
        context.startService(intent)
    }

    constructor(context: Context, clazz: Class<*>, url: String, string: String) : this() {
        val intent = Intent(context, clazz)
        intent.putExtra(url, string)
        context.startActivity(intent)
    }

    constructor(context: Context, standardAction: String, url: Uri) : this() {
        val intent = Intent(standardAction, url)
        context.startActivity(intent)
    }

    constructor(context: Context, clazz: Class<*>) : this() {
        val intent = Intent(context, clazz)
        context.startActivity(intent)
    }

    companion object {
        fun showWeb(context: Context, url: String) {
            ObjectIntent(
                context,
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        }
    }
}

