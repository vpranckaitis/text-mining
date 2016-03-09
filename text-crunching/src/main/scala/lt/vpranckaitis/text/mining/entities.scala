package lt.vpranckaitis.text.mining

object entities {
  case class Movie(name: String, description: String)

  trait TripletFields {
    val subject: String
    val predicate: String
    val `object`: String
  }

  object Triplet {
    def reverse(t: TripletFields) = t match {
      case Triplet(s, p, o) => ReversedTriplet(o, p, s)
      case ReversedTriplet(s, p, o) => Triplet(o, p, s)
    }
  }

  case class Triplet(subject: String, predicate: String, `object`: String) extends TripletFields
  case class ReversedTriplet(subject: String, predicate: String, `object`: String) extends TripletFields

  case class Plot(sentences: Seq[String], locations: Seq[String], characters: Seq[String])
}
