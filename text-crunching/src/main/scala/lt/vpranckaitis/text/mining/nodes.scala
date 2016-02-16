package lt.vpranckaitis.text.mining

import opennlp.tools.parser.Parse

object nodes {
  class Node(`type`: String*) {
    def unapply(p: Parse) = if (`type`.contains(p.getType)) Some(p) else None
  }

  class CompositeNode(`type`: String*) {
    def unapply(p: Parse) = if (`type`.contains(p.getType)) Some(p.getChildren.toList) else None
  }

  object TOP extends CompositeNode("TOP")

  object S extends CompositeNode("S")

  object NP extends CompositeNode("NP")

  object VP extends CompositeNode("VP")

  object PP extends CompositeNode("PP")

  object ADJP extends CompositeNode("ADJP")

  object NNs extends Node("NN", "NNP", "NNPS", "NNS")

  object VBs extends Node("VB", "VBD", "VBG", "VBN", "VBP", "VBZ")

  object JJs extends Node("JJ", "JJR", "JJS")
}
