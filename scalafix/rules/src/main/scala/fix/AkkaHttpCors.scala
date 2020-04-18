package fix

import scalafix.v1._
import scala.meta._

class AkkaHttpCors extends SemanticRule("AkkaHttpCors") {

  private val corsMethod: SymbolMatcher =
    SymbolMatcher.exact("ch/megard/akka/http/cors/scaladsl/CorsDirectives#cors().")

  private val defaultSettings: SymbolMatcher =
    SymbolMatcher.exact("ch/megard/akka/http/cors/scaladsl/settings/CorsSettings.defaultSettings.")

  override def fix(implicit doc: SemanticDocument): Patch = {
    println("Tree.structureLabeled: " + doc.tree.structureLabeled)

    doc.tree.collect {
      case function @ Term.Apply(corsMethod(_: Name), Nil) =>
        Patch.removeTokens(function.tokens.tail)
      case function @ Term.Apply(corsMethod(_: Name), List(_)) =>
        Patch.replaceToken(function.tokens.head, "corsWithSettings")
      case defaultSettings(name: Name) =>
        Patch.replaceTree(name, "default")
    }.asPatch
  }

}
