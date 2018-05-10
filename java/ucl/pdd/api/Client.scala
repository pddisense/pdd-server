package ucl.pdd.api

import org.joda.time.Instant

/**
 * A client corresponds to a registered browser extension. We do not track users across multiple
 * browsers, i.e, they would each behave as a different client.
 *
 * @param name         Client name, unique among all clients.
 * @param createTime   Time at which the client was created.
 * @param publicKey    Public key.
 * @param browser      Identifier of the browser used by this client.
 * @param externalName A name voluntarily filled by the user allowing to identify him.
 * @param leaveTime    Time at which the client left the campaign. Shall he choose to come back, he
 *                     will be considered as a new client.
 */
case class Client(
  name: String,
  createTime: Instant,
  publicKey: String,
  browser: String,
  externalName: Option[String],
  leaveTime: Option[Instant] = None) {

  def hasLeft: Boolean = leaveTime.nonEmpty
}
