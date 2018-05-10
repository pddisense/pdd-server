package ucl.pdd

import org.joda.time.Instant

package object api {
  implicit val instantOrdering = new Ordering[Instant] {
    override def compare(x: Instant, y: Instant): Int = x.compareTo(y)
  }
}
