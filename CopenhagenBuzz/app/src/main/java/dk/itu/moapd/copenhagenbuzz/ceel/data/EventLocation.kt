package dk.itu.moapd.copenhagenbuzz.ceel.data

/**
 * Encapsulates the geographic and human‐readable location of an event.
 *
 * @property latitude The GPS latitude coordinate of the event venue.
 * @property longitude The GPS longitude coordinate of the event venue.
 * @property address The full postal address.
 */
data class EventLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)
