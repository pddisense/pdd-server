package ucl.pdd.api

/**
 * Record the activity of a client. It is later used to create "optimal" groups when dealing with
 * encrypted sketches collection. An activity is recorded when a client sends a ping request
 * to the server, and at most once per day.
 *
 * @param clientName  Client unique identifier.
 * @param countryCode Code of the country the client is currently located in.
 * @param day         Day of the interaction.
 * @param timezone    Timezone the client is currently located in.
 */
case class Activity(clientName: String, countryCode: String, day: Int, timezone: String)
