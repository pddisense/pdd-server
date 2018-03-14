package ucl.pdd.server

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

private[server] final class CorsFilter extends SimpleFilter[Request, Response] {
  private[this] val cors = {
    val allowsOrigin = { origin: String => Some(origin) }
    val allowsMethods = { method: String => Some(Seq("GET", "POST", "PUT", "DELETE")) }
    val allowsHeaders = { headers: Seq[String] => Some(headers) }
    val policy = Cors.Policy(allowsOrigin, allowsMethods, allowsHeaders)
    new Cors.HttpFilter(policy)
  }

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] =
    cors.apply(request, service)
}
