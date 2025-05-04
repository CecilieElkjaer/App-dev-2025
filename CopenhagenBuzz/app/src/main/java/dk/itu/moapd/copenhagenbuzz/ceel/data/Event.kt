/**
 * MIT License
 *
 * Copyright (c) 2025 Cccilie Amalie Wall Elkjær
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dk.itu.moapd.copenhagenbuzz.ceel.data

/**
 * Represents a single event in the application.
 *
 * @property eventPhotoUrl URL pointing to an image for this event. Defaults to `null` when no photo has been uploaded.
 * @property eventName The name of the event.
 * @property eventLocation Instance of [EventLocation] detailing the geographic coordinates and address where the event takes place.
 * @property eventDate Epoch-milliseconds timestamp representing the event’s start date/time
 * @property eventType The category or type of the event.
 * @property eventDescription A brief description of the event.
 * @property userId An id reference to the user, that has created this event
 */
data class Event(
    var eventPhotoUrl: String? = null,
    var eventName: String = "",
    var eventLocation: EventLocation = EventLocation(),
    var eventDate: Long = 0L,
    var eventType: String = "",
    var eventDescription: String = "",
    var userId: String = ""
)