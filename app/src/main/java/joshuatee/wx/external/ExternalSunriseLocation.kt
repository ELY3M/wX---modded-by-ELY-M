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
 * Simple VO class to store latitude/longitude information.
 */
class ExternalSunriseLocation(latitude: String, longitude: String) {
    /**
     * Creates a new instance of `Location` with the given parameters.
     *
     *  latitude
     * the latitude, in degrees, of this location. North latitude is positive, south negative.
     *  longitude
     * the longitude, in degrees, of this location. East longitude is positive, east negative.
     */
    val latitude = latitude.toBigDecimalOrNull() ?: BigDecimal("0.0")

    val longitude = longitude.toBigDecimalOrNull() ?: BigDecimal("0.0")
}
