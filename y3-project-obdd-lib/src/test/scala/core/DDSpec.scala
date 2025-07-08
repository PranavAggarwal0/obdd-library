package obdd.core

import org.scalatest.matchers.should._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}
import obdd.core.OBDDFixture.*
import obdd.core.OBDD.*
import obdd.parser.ExpressionParser
import scala.collection.immutable.HashMap

class DDSpec extends AnyWordSpec with Matchers with EitherValues with OptionValues {

  def ctx = DDContext(
    ordering,
    nodes = HashMap(
      ((Var("true"), -3, -4), -1),
      ((Var("false"), -3, -4), -2),
      ((Var("x1"), -1, -2), 1),
      ((Var("x2"), -1, -2), 2),
      ((Var("x1"), 2, -2), 3)
    )
  )

  "isbigger" should {
    "return the correct results" when {
      "one variable is a true" in {
        isBigger(Var("true"), Var("x1"), ctx) shouldBe false
      }
      "both variables are vars" in {
        isBigger(Var("x1"), Var("x2"), ctx) shouldBe true
        isBigger(Var("x4"), Var("x3"), ctx) shouldBe false
      }
    }
  }

  "getHigh" should {
    "return the correct high node" in {
      getHigh(Node(3, Var("x1"), 2, -2), ctx).value shouldBe Node(2, Var("x2"), -1, -2)
    }
  }

  "getLow" should {
    "return the correct low node" in {
      getLow(Node(3, Var("x1"), 2, -2), ctx).value shouldBe Node(-2, Var("false"), -3, -4)
    }
  }

}
