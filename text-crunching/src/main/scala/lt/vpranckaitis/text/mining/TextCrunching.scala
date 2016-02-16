package lt.vpranckaitis.text.mining

import scala.io.Source

object TextCrunching extends App {

  val moviesFile = Source.fromFile("plot.list", "ISO-8859-1").getLines

  val withoutHeader = moviesFile dropWhile { !_.matches("^MV:.*") }

  val time = System.currentTimeMillis()

  val plots = Parsers.textFileToMovies(withoutHeader) take 5000 map Parsers.movieToPlot

  def top10(x: Seq[(_, Int)]) = x.sortBy(_._2)(Ordering[Int].reverse) take 10

  val locations = plots flatMap { _.locations } groupBy { l => l } mapValues { _.size }
  val top10Locations = top10(locations.toSeq)

  val characters = plots flatMap { _.characters } groupBy { l => l } mapValues { _.size }
  val top10Characters = top10(characters.toSeq)

  println((System.currentTimeMillis() - time) * 0.001)

  println(top10Locations mkString "\n")

  println("---------------------")

  println(top10Characters mkString "\n")
}
