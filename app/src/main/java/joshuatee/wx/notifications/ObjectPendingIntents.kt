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

package joshuatee.wx.notifications


import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent

class ObjectPendingIntents {

    var resultPendingIntent: PendingIntent
    var resultPendingIntent2: PendingIntent

    constructor(
        context: Context,
        cl: Class<*>,
        classFlag: String,
        classArgs1: Array<String>,
        classArgs2: Array<String>
    ) {
        val resultIntent = Intent(context, cl)
        val resultIntent2 = Intent(context, cl)
        resultIntent.putExtra(classFlag, classArgs1)
        resultIntent2.putExtra(classFlag, classArgs2)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(cl)
        stackBuilder.addNextIntent(resultIntent)
        val requestID = System.currentTimeMillis().toInt()
        resultPendingIntent =
            stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT)
        resultPendingIntent2 = PendingIntent.getActivity(
            context,
            requestID + 1,
            resultIntent2,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    internal constructor(context: Context, cl: Class<*>) {
        val resultIntent = Intent(context, cl)
        val resultIntent2 = Intent(context, cl)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(cl)
        stackBuilder.addNextIntent(resultIntent)
        val requestID = System.currentTimeMillis().toInt()
        resultPendingIntent =
            stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT)
        resultPendingIntent2 = PendingIntent.getActivity(
            context,
            requestID + 1,
            resultIntent2,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}


