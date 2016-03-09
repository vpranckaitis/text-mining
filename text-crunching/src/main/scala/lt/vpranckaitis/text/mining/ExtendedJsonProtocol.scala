package lt.vpranckaitis.text.mining

import lt.vpranckaitis.text.mining.entities.Movie
import spray.json.DefaultJsonProtocol

object ExtendedJsonProtocol extends DefaultJsonProtocol {
  implicit val MovieFormat = jsonFormat2(Movie)
}
