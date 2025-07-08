package obdd.core

import org.scalatest.matchers.should._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.EitherValues

class UtilSpec extends AnyWordSpec with Matchers with EitherValues {

  "getVars" should {
    "work for an expression" when {
      "given a variable" in {
        getVars(Var("x1")) shouldBe Set(Var("x1"))
      }
      "given a boolean" in {
        getVars(true) shouldBe Set()
      }
      "given an expression" in {
        getVars(And(Var("a"), Var("b"))) shouldBe Set(Var("a"), Var("b"))
      }
      "given an expression with more than one occurence of a variable" in {
        getVars(Or(Var("b"), And(Var("a"), Var("b")))) shouldBe Set(Var("a"), Var("b"))
      }
    }

  }

  "validateOrdering" should {
    "return true when there's no duplicates" in {
      validateOrdering(Seq(Var("a"), Var("b"))) shouldBe true
    }
    "return false when there's duplicates" in {
      validateOrdering(Seq(Var("a"), Var("a"))) shouldBe false
    }
  }

  "validateExpression" should {
    "return true when vars in expression are a subset of ordering" in {
      validateExpression(And(Var("a"), Var("b")), Seq(Var("a"), Var("b"), Var("c")))
    }
    "return false when vars in expression are not a subset of ordering" in {
      validateExpression(And(Var("a"), Var("b")), Seq(Var("a"), Var("c"), Var("d")))
    }
  }
}
