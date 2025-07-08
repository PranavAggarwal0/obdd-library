package obdd.parser

import scala.util.parsing.combinator._
import obdd.core.*

class DimacsParser extends RegexParsers {

  override def skipWhitespace: Boolean = false

  def parseString(dimacs: String): Either[DimacsParserException, BooleanExpression] =
    parse(parser, dimacs) match {
      case Success(matched, _) =>
        Right(And({
          for {
            clause <- matched
          } yield Or(clause.map { v =>
            if (v.toInt < 0)
              Not(Var("x" + (v.toInt * -1).toString))
            else Var("x" + v)
          }: _*)
        }: _*))
      case Failure(msg, _) => Left(new DimacsParserException(s"FAILURE: $msg"))
      case Error(msg, _)   => Left(new DimacsParserException(s"ERROR: $msg"))
    }

  def parser = phrase((comments ~ header ~ clauses) ^^ { case c ~ h ~ cl => cl })

  private def header = ("p " ~ "cnf " ~ "[1-9][0-9]* ".r ~ "[1-9][0-9]*".r ~ ("\n*".r ^^ { _.toString() })) ^^ {
    case p ~ cnf ~ vars ~ clauses ~ n =>
      (vars, clauses)
  }

  def comment  = ("c" ~ ".*".r) ^^ { case c ~ s => s }
  def comments = (rep(comment <~ "\n") ~ ("\n*".r ^^ { _.toString() })) ^^ { case c ~ n => c }

  def integer = "-?[1-9][0-9]* ".r ^^ { _.toString }

  def clause  = (integer.+ ~ ("0\n".r ^^ { _.toString })) ^^ { case l ~ zero => l.map(_.strip()) }
  def clauses = rep1(clause)
}
