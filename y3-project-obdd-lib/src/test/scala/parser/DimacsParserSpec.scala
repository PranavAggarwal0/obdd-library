package obdd.parser

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.EitherValues
import org.scalatest.matchers.should._
import obdd.core.*

class DimacsParserSpec extends AnyWordSpec with Matchers with EitherValues {

  def parser = new DimacsParser
  def x1     = Var("x1")
  def x2     = Var("x2")
  def x3     = Var("x3")

  val runtime = cats.effect.unsafe.IORuntime.global

  "The DIMACS parser" should {

    "parse file in proper format" in {
      parser.parseString(getFileContents("src/test/scala/parser/resource-dimacs/dimacs.txt")).value shouldBe
        And(
          Or(x1, x2, x3),
          Or(Not(x3), Not(x1)),
          Or(x2, x1),
          Or(Not(x2))
        )
    }

    "parse correctly if there are no comments" in {
      parser
        .parseString(getFileContents("src/test/scala/parser/resource-dimacs/no-comments.txt"))
        .value shouldBe And(Or(x1))
    }

    "error if clauses are on the same line" in {
      parser
        .parseString(getFileContents("src/test/scala/parser/resource-dimacs/clauses-same-line.txt"))
        .left
        .value shouldBe a[DimacsParserException]
    }

    "error if there is no header" in {
      parser
        .parseString(getFileContents("src/test/scala/parser/resource-dimacs/no-header.txt"))
        .left
        .value shouldBe a[DimacsParserException]
    }

    "error if there is no 0 at the end of a clause" in {
      parser
        .parseString(getFileContents("src/test/scala/parser/resource-dimacs/no-zero.txt"))
        .left
        .value shouldBe a[DimacsParserException]
    }

    "error if there are two headers" in {
      parser
        .parseString(getFileContents("src/test/scala/parser/resource-dimacs/two-headers.txt"))
        .left
        .value shouldBe a[DimacsParserException]
    }

    "error if header is in the wrong format" in {
      parser
        .parseString(getFileContents("src/test/scala/parser/resource-dimacs/wrong-header.txt"))
        .left
        .value shouldBe a[DimacsParserException]
    }

    "error if comments occur after header" in {
      parser
        .parseString(getFileContents("src/test/scala/parser/resource-dimacs/wrong-order.txt"))
        .left
        .value shouldBe a[DimacsParserException]
    }

  }

}
