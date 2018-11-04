package joshuatee.wx.external

object UtilityStringExternal {

    /*

	  http://svn.apache.org/repos/asf/db/derby/code/trunk/java/engine/org/apache/derby/iapi/util/StringUtil.java

	Derby - Class org.apache.derby.iapi.util.PropertyUtil

	Licensed to the Apache Software Foundation (ASF) under one or more
	contributor license agreements.  See the NOTICE file distributed with
	this work for additional information regarding copyright ownership.
	The ASF licenses this file to you under the Apache License, Version 2.0
	(the "License"); you may not use this file except in compliance with
	the License.  You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

	 */


    /**
     * Truncate a String to the given length with no warnings
     * or error raised if it is bigger.

     * @param  value String to be truncated
     * @param  length  Maximum length of string
     * @return Returns value if value is null or value.length() is less or equal to than length, otherwise a String representing
     * value truncated to length.
     */
    fun truncate(value: String, length: Int): String {
        var valueLocal = value
        if (valueLocal.length > length) {
            valueLocal = valueLocal.substring(0, length)
        }
        return valueLocal
    }
}
