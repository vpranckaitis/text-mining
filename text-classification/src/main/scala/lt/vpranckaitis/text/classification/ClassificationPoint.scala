package lt.vpranckaitis.text.classification

case class ClassificationPoint(words: Map[String, Double], category: String, bestMatches: Seq[(Double, String)])
