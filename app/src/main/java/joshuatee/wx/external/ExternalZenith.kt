/*
 * Copyright 2008-2009 Mike Reedell / LuckyCatLabs.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package joshuatee.wx.external

import java.math.BigDecimal

/**
 * Defines the solar declination used in computing the sunrise/sunset.
 */
class ExternalZenith private constructor(degrees: Double) {

    val degrees: BigDecimal = BigDecimal.valueOf(degrees)

    companion object {

        /** Official sunrise/set is when the sun is 50' below the horizon.  */
        val OFFICIAL = ExternalZenith(90.8333)
    }
}
