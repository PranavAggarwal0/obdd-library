package obdd.parser

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.EitherValues
import org.scalatest.matchers.should._
import obdd.core.*

class QDimacsParserSpec extends AnyWordSpec with Matchers with EitherValues {

  def parser = new QDimacsParser
  def x1     = Var("x1")
  def x2     = Var("x2")
  def x3     = Var("x3")

  val runtime = cats.effect.unsafe.IORuntime.global

  "The QDIMACS parser" should {

    "parse file correctly with exists" in {
      parser.parseString(getFileContents("src/test/scala/parser/resource-qdimacs/qdimacs_e.txt")).value shouldBe
        EXISTS(
          Seq(x1, x2),
          And(
            Or(x1, x2, x3),
            Or(Not(x3), Not(x1)),
            Or(x2, x1),
            Or(Not(x2))
          )
        )
    }

    "parse file correctly with forall" in {
      parser.parseString(getFileContents("src/test/scala/parser/resource-qdimacs/qdimacs_a.txt")).value shouldBe
        FORALL(
          Seq(x1, x2),
          And(
            Or(x1, x2, x3),
            Or(Not(x3), Not(x1)),
            Or(x2, x1),
            Or(Not(x2))
          )
        )
    }

    "parse file correctly with a mixture of exists and forall" in {
      parser.parseString(getFileContents("src/test/scala/parser/resource-qdimacs/qdimacs_mix.txt")).value shouldBe
        FORALL(
          Seq(x1),
          EXISTS(
            Seq(x2),
            FORALL(
              Seq(x3),
              And(
                Or(x1, x2, x3),
                Or(Not(x3), Not(x1)),
                Or(x2, x1),
                Or(Not(x2))
              )
            )
          )
        )

    }

  }

}
