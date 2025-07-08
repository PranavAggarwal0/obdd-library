package obdd.parser

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.EitherValues
import org.scalatest.matchers.should._
import obdd.core.*

class ExpressionParserSpec extends AnyWordSpec with Matchers with EitherValues {

  def parser = new ExpressionParser

  "The expression parser" should {

    "parse variables" in {
      parser.parseInput("abc").value shouldBe Var("abc")
    }

    "parse parentheses correctly" in {
      parser.parseInput("(a || b)").value shouldBe Or(Var("a"), Var("b"))
      parser.parseInput("((a || b))").value shouldBe Or(Var("a"), Var("b"))
      parser.parseInput("(!a)").value shouldBe Not(Var("a"))
      parser.parseInput("(!((a)))").value shouldBe Not(Var("a"))
      parser.parseInput("(a) && (b)").value shouldBe And(Var("a"), Var("b"))
      parser.parseInput("a || (b || c)").value shouldBe Or(Var("a"), Or(Var("b"), Var("c")))
    }

    "fail if parentheses do not match" in {
      parser.parseInput("(a || b))").left.value shouldBe an[ExpressionParserException]
    }

    "fail if operators are separated by space" in {
      parser.parseInput("(a | | b))").left.value shouldBe an[ExpressionParserException]
    }

    "handle left associativity" in {
      parser.parseInput("a && b && c && d").value shouldBe And(And(And(Var("a"), Var("b")), Var("c")), Var("d"))
      parser.parseInput("a && (b && c) && d").value shouldBe And(And(Var("a"), And(Var("b"), Var("c"))), Var("d"))
    }

    "respect order of operators" in {
      parser.parseInput("a && b || c").value shouldBe Or(And(Var("a"), Var("b")), Var("c"))
      parser.parseInput("a || b && !c").value shouldBe Or(Var("a"), And(Var("b"), Not(Var("c"))))
      parser.parseInput("a || b && !c -> d").value shouldBe Implication(
        Or(Var("a"), And(Var("b"), Not(Var("c")))),
        Var("d")
      )
    }

    "fail for mismatched operators" in {
      parser.parseInput("a & b").left.value shouldBe an[ExpressionParserException]
      parser.parseInput("a &| b").left.value shouldBe an[ExpressionParserException]
      parser.parseInput("a && b ||").left.value shouldBe an[ExpressionParserException]
      parser.parseInput("&&a && b").left.value shouldBe an[ExpressionParserException]
      parser.parseInput("&&a !&& !b").left.value shouldBe an[ExpressionParserException]
    }

  }
}
