package obdd.parser

class ParserException(msg: String)                 extends RuntimeException
final class ExpressionParserException(msg: String) extends ParserException(msg)
final class DimacsParserException(msg: String)     extends ParserException(msg)
final class QDimacsParserException(msg: String)    extends ParserException(msg)
