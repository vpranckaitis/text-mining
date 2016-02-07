package lt.vpranckaitis.text.mining

import lt.vpranckaitis.text.mining.entities.Movie

object Helpers {
  implicit class RitchMovie(m: Movie) {
    lazy val characters = Parsers.textToMovieCharacters(m.plot)
  }
}
