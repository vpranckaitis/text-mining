package lt.vpranckaitis.text.mining

object entities {
  case class Movie(name: String, description: String)

  trait Triplet {
    val subject: String
    val predicate: String
    val `object`: String
  }

  case class TwoPeopleTriplet(subject: String, predicate: String, `object`: String) extends Triplet
  case class SubjectPersonTriplet(subject: String, predicate: String, `object`: String) extends Triplet

  case class Plot(description: String, locations: Seq[String], characters: Seq[String])
}
