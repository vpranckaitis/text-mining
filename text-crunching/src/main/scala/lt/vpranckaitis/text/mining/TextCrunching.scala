package lt.vpranckaitis.text.mining

import lt.vpranckaitis.text.mining.entities.MoviePlot

import scala.io.Source

object TextCrunching extends App {

  def parseMovies(lines: Iterator[String]): Stream[MoviePlot] = {
    val (before, after) = lines span { !_.matches("^[-]+$") }
    val groupedByPrefix = before.toList filter { _.length > 3 } takeWhile { !_.startsWith("BY:") } groupBy { _ take 3 }
    val name = groupedByPrefix("MV:").head stripPrefix "MV: "
    val plot = groupedByPrefix("PL:") map { _ stripPrefix "PL: " } mkString " "
    MoviePlot(name, plot) #:: parseMovies(after drop 1)
  }

  val moviesFile = Source.fromFile("plot.list").getLines

  val withoutHeader = moviesFile dropWhile { !_.matches("^MV:.*") }

  println(parseMovies(withoutHeader) take 10 mkString "\n")
}
