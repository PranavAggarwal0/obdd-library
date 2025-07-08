package obdd.parser

import scala.util.parsing.combinator._
import obdd.core.*

class ExpressionParser extends RegexParsers {

  override def skipWhitespace = true

  def parseInput(input: String): Either[ExpressionParserException, BooleanExpression] =
    parse(expression, input) match {
      case Success(matched, _) => Right(matched)
      case Failure(msg, _)     => Left(new ExpressionParserException(s"FAILURE: $msg"))
      case Error(msg, _)       => Left(new ExpressionParserException(s"ERROR: $msg"))
    }

  def variable = "[a-zA-Z_][a-zA-Z0-9_]*".r ^^ { Var(_) }

  def implication =
    l2 ~ rep("->" ~ l2) ^^ { case x ~ xs =>
      xs.foldLeft(x) { case (a, f ~ b) =>
        Implication(a, b)
      }
    }

  def or =
    l3 ~ rep("||" ~ l3) ^^ { case x ~ xs =>
      xs.foldLeft(x) { case (a, f ~ b) =>
        Or(a, b)
      }
    }

  def and =
    l4 ~ rep("&&" ~ l4) ^^ { case x ~ xs =>
      xs.foldLeft(x) { case (a, f ~ b) =>
        And(a, b)
      }
    }

  def not: Parser[Not] =
    ("!" ~ l5) ^^ { case not ~ a => Not(a) }

  def parentheses: Parser[BooleanExpression] =
    ("(" ~ l1 ~ ")") ^^ { case a ~ b ~ c => b }

  def l1         = implication | l2
  def l2         = or | l3
  def l3         = and | l4
  def l4         = not | l5
  def l5         = parentheses | variable
  def expression = phrase(l1)

}
