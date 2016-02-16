package lt.vpranckaitis.text.mining

object entities {
  case class Movie(name: String, description: String)

  trait TripletFields {
    val subject: String
    val predicate: String
    val `object`: String
  }

  case class Triplet(subject: String, predicate: String, `object`: String) extends TripletFields
  case class TwoPeopleTriplet(subject: String, predicate: String, `object`: String) extends TripletFields
  case class SubjectPersonTriplet(subject: String, predicate: String, `object`: String) extends TripletFields

  case class Plot(sentences: Seq[String], locations: Seq[String], characters: Seq[String])
}
