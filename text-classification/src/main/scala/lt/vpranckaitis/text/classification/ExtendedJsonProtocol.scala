package lt.vpranckaitis.text.classification

import lt.vpranckaitis.text.mining.{ExtendedJsonProtocol => ExternalJsonProtocol}
import spray.json.DefaultJsonProtocol

object ExtendedJsonProtocol extends DefaultJsonProtocol {

  implicit val MovieFormat = ExternalJsonProtocol.MovieFormat
  implicit val ClassificationPointFormat = jsonFormat3(ClassificationPoint)
}
