package lt.vpranckaitis.text.mining

object TextCrunching extends App {

  /*val count = 100

  val moviesFile = Source.fromFile("plot.list", "ISO-8859-1").getLines

  val withoutHeader = moviesFile dropWhile { !_.matches("^MV:.*") }

  val time = System.currentTimeMillis()

  val plots = Parsers.textFileToMovies(withoutHeader) take count map Parsers.movieToPlot

  def top10(x: Seq[(_, Int)]) = x.sortBy(_._2)(Ordering[Int].reverse) take 10

  val locations = plots flatMap { _.locations } groupBy { l => l } mapValues { _.size }
  val top10Locations = top10(locations.toSeq)

  val characters = plots flatMap { _.characters } groupBy { l => l } mapValues { _.size }
  val top10Characters = top10(characters.toSeq)

  println((System.currentTimeMillis() - time) * 0.001)

  println(top10Locations mkString "\n")

  println("---------------------")

  println(top10Characters mkString "\n")

  val triplets = plots flatMap { x => Parsers.sentencesToTriplets(x.sentences) }

  triplets.toList*/

  Parsers.sentencesToTriplets(Seq("Meanwhile, Herr Flick is recovering in traction and Leclerc and Madame Fanny are going out for a walk."))
}
