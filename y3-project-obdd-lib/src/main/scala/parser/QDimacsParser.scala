package obdd.parser

import scala.util.parsing.combinator._
import obdd.core.*

class QDimacsParser extends RegexParsers {

  override def skipWhitespace: Boolean = false

  private def toQuant(quants: List[(String, List[String])], formula: BooleanExpression): QUANT | BooleanExpression =
    if (quants.size == 0) {
      formula
    } else if (quants.size == 1) {
      quants.head._1 match
        case "exists" => EXISTS(quants.head._2.map(i => Var("x" + i)), formula)
        case "forall" => FORALL(quants.head._2.map(i => Var("x" + i)), formula)
    } else {
      quants.head._1 match
        case "exists" => EXISTS(quants.head._2.map(i => Var("x" + i)), toQuant(quants.tail, formula))
        case "forall" => FORALL(quants.head._2.map(i => Var("x" + i)), toQuant(quants.tail, formula))
    }

  def parseString(dimacs: String): Either[QDimacsParserException, QUANT | BooleanExpression] =
    parse(parser, dimacs) match {
      case Success(matched, _) =>
        def quants  = matched._1
        def clauses = matched._2
        def formula = And({
          for {
            clause <- clauses
          } yield Or(clause.map { v =>
            if (v.toInt < 0)
              Not(Var("x" + (v.toInt * -1).toString))
            else Var("x" + v)
          }: _*)
        }: _*)
        Right(toQuant(quants, formula))
      case Failure(msg, _) => Left(new QDimacsParserException(s"FAILURE: $msg"))
      case Error(msg, _)   => Left(new QDimacsParserException(s"ERROR: $msg"))
    }

  def parser = phrase((comments ~ header ~ quantifiers ~ clauses) ^^ { case c ~ h ~ q ~ cl => (q, cl) })

  private def header = ("p " ~ "cnf " ~ "[1-9][0-9]* ".r ~ "[1-9][0-9]*".r ~ ("\n*".r ^^ { _.toString() })) ^^ {
    case p ~ cnf ~ vars ~ clauses ~ n =>
      (vars, clauses)
  }

  def comment  = ("c" ~ ".*".r) ^^ { case c ~ s => s }
  def comments = (rep(comment <~ "\n") ~ ("\n*".r ^^ { _.toString() })) ^^ { case c ~ n => c }

  def exists = ("e " ~ integer_pos.+ ~ ("0\n".r ^^ { _.toString })) ^^ { case e ~ l ~ zero =>
    ("exists", l.map(_.strip()))
  }
  def forall = ("a " ~ integer_pos.+ ~ ("0\n".r ^^ { _.toString })) ^^ { case e ~ l ~ zero =>
    ("forall", l.map(_.strip()))
  }

  def quantifiers = rep(exists | forall)

  def integer     = "-?[1-9][0-9]* ".r ^^ { _.toString }
  def integer_pos = "[1-9][0-9]* ".r ^^ { _.toString }

  def clause  = (integer.+ ~ ("0\n".r ^^ { _.toString })) ^^ { case l ~ zero => l.map(_.strip()) }
  def clauses = rep1(clause)

}
