package lt.vpranckaitis.text.mining

import lt.vpranckaitis.text.mining.entities.{Movie, Plot, Triplet}
import lt.vpranckaitis.text.mining.nodes._
import opennlp.tools.namefind.{NameFinderME, TokenNameFinderModel}
import opennlp.tools.parser.{AbstractBottomUpParser, Parse, ParserFactory, ParserModel}
import opennlp.tools.sentdetect.{SentenceDetector, SentenceDetectorME, SentenceModel}
import opennlp.tools.tokenize.{Tokenizer, TokenizerME, TokenizerModel}
import opennlp.tools.util.Span

import scala.annotation.tailrec

object Parsers {
  private lazy val personModel = new TokenNameFinderModel(getClass.getResourceAsStream("/en-ner-person.bin"))
  private lazy val locationModel = new TokenNameFinderModel(getClass.getResourceAsStream("/en-ner-location.bin"))
  private lazy val sentenceModel = new SentenceModel(getClass.getResourceAsStream("/en-sent.bin"))
  private lazy val tokenModel = new TokenizerModel(getClass.getResourceAsStream("/en-token.bin"))
  private lazy val parserModel = new ParserModel(getClass.getResourceAsStream("/en-parser-chunking.bin"))

  def textFileToMovies(lines: Iterator[String]): Stream[Movie] = {
    val (before, after) = lines span { !_.matches("^[-]+$") }
    val groupedByPrefix = before.toList filter { _.length > 3 } takeWhile { !_.startsWith("BY:") } groupBy { _ take 3 }
    val name = groupedByPrefix("MV:").head stripPrefix "MV: "
    val plot = groupedByPrefix("PL:") map { _ stripPrefix "PL: " } mkString " "
    Movie(name, plot) #:: textFileToMovies(after drop 1)
  }

  def textToMovieCharacters(text: String) = {
    val sentenceDetector: SentenceDetector = new SentenceDetectorME(sentenceModel)
    val tokenizer: Tokenizer = new TokenizerME(tokenModel)

    val nameFinder = new NameFinderME(personModel)
    val spans = for {
      sentence <- sentenceDetector.sentDetect(text)
      tokens = tokenizer.tokenize(sentence)
      spans = nameFinder.find(tokens)
      strings = spans map { s => tokens.slice(s.getStart, s.getEnd) mkString " " }
    } yield strings

    spans.flatten.distinct.toSeq
  }

  def movieToPlot(movie: Movie) = {

    def deSpan(sentence: Seq[String])(s: Span) = sentence.slice(s.getStart, s.getEnd) mkString " "

    val sentenceDetector = new SentenceDetectorME(sentenceModel).sentDetect _
    val tokenize = new TokenizerME(tokenModel).tokenize _
    val findLocationSpans: (Array[String] => Array[Span]) = new NameFinderME(locationModel).find _
    val findPeopleSpans: (Array[String] => Array[Span]) = new NameFinderME(personModel).find _

    val sentences = sentenceDetector(movie.description)

    val tokenizedSentences = sentences map tokenize

    val locations = for {
      sentence <- tokenizedSentences
      span <- findLocationSpans(sentence)
    } yield deSpan(sentence)(span)

    val characters = for {
      sentence <- tokenizedSentences
      span <- findPeopleSpans(sentence)
    } yield deSpan(sentence)(span)

    Plot(sentences, locations.distinct, characters.distinct)
  }

  private def first(f: Node)(parse: List[Parse]): Option[Parse] = parse match {
    case f(_) :: f(_) :: f(x) :: _ => Some(x)
    case f(_) :: f(x) :: _ => Some(x)
    case f(x) :: _ => Some(x)
    case x :: xs => firstNoun(x.getChildren.toList) orElse firstNoun(xs)
    case _ => None
  }

  def firstNoun = first(NNs) _

  def firstAdjective = first(JJs) _

  def deepestVerb(parse: List[Parse]): Option[Parse] = {
    def deeperOne(a: Option[(Parse, Int)], b: Option[(Parse, Int)]) = (a, b) match {
      case (x, None) => x
      case (None, y) => y
      case (Some(x), Some(y)) if x._2 < y._2 => Some(y)
      case (x, _)  => x
    }

    def dv(parse: List[Parse], depth: Int): Option[(Parse, Int)] = parse match {
      case VBs(x) :: Nil => Some((x, depth))
      case VBs(x) :: xs => deeperOne(Some((x, depth)), dv(xs, depth))
      case x :: xs => deeperOne(dv(x.getChildren.toList, depth + 1), dv(xs, depth))
      case _ => None
    }

    dv(parse, 0) map { _._1 }
  }

  def findObject(parse: List[Parse]): Option[Parse] = parse match {
    case PP(x) :: _ => firstNoun(x)
    case NP(x) :: _ => firstNoun(x)
    case ADJP(x) :: _ => firstAdjective(x)
    case x :: xs => findObject(x.getChildren.toList) orElse findObject(xs)
    case _ => None
  }

  def parseToTriplets(parse: List[Parse]): Vector[Triplet] = parse match {
    case S(x) :: xs =>
      parseToTriplets(x.toList) ++ parseToTriplets(xs)
    case NP(x) :: VP(y) :: xs =>
      val triplet = for {
        subject <- firstNoun(x)
        predicate <- deepestVerb(y)
        objectt <- findObject(predicate.getParent.getChildren.toList)
        punctuation = ".,!?/)(".toSet
        s = subject.getCoveredText filterNot punctuation
        p = predicate.getCoveredText filterNot punctuation
        o = objectt.getCoveredText filterNot punctuation
      } yield Triplet(s, p, o)
      triplet ++: parseToTriplets(xs)
    case _ :: xs => parseToTriplets(xs)
    case _ => Vector()
  }

  private def initialParse(sentence: String) = {
    val tokenizer = new TokenizerME(tokenModel)
    val findPeopleSpans: (Array[String] => Array[Span]) = new NameFinderME(personModel).find _

    val p = new Parse(sentence, new Span(0, sentence.length), AbstractBottomUpParser.INC_NODE, 1, 0)

    val spans = tokenizer.tokenizePos(sentence)
    val tokens = spans map { s => sentence.substring(s.getStart, s.getEnd) }
    val peopleSpans = findPeopleSpans(tokens) filter { s => s.getEnd - s.getStart > 1 }

    @tailrec
    def mergeSpans(spans: Seq[Span], merges: Seq[Span], i: Int = 0, acc: Vector[Span] = Vector()): Vector[Span] = {
      if (merges.isEmpty) acc ++ spans
      else if (i < merges.head.getStart) {
        val n = merges.head.getStart - i
        val (heads, tails) = spans splitAt n
        mergeSpans(tails, merges, i + n, acc ++ heads)
      } else {
        val n = merges.head.getEnd - i
        val (heads, tails) = spans splitAt n
        val mergedSpan = new Span(heads.head.getStart, heads.last.getEnd)
        mergeSpans(tails, merges.tail, i + n, acc :+ mergedSpan)
      }
    }

    val mergedSpans = mergeSpans(spans, peopleSpans)

    for ((span, i) <- mergedSpans.zipWithIndex) {
      p.insert(new Parse(sentence, span, AbstractBottomUpParser.TOK_NODE, 0, i))
    }

    p
  }

  def sentencesToTriplets(sentences: Seq[String]) = {
    val parser = ParserFactory.create(parserModel)

    for {
      sentence <- sentences
      parse = parser.parse(initialParse(sentence))
      _ = println(sentence)
      _ = parse.show
      triplet <- parseToTriplets(parse.getChildren.toList)
      _ = println(triplet)
    } yield triplet
  }
}
