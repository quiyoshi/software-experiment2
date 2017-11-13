package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Variable extends CParseRule{
	// variable ::= ident [ array ]

	private CParseRule ident, array;
	public Variable(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Ident.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		ident = new Ident(pcx);
		ident.parse(pcx);

		tk = ct.getCurrentToken(pcx);
		if(Array.isFirst(tk)){
			array = new Array(pcx);
			array.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (array != null) {

		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
	}
}

class Array extends CParseRule{
	// array ::= LBRA expression RBRA

	private CParseRule expression;
	public Array(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return CToken.TK_LBRA == tk.getType();
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		tk = ct.getNextToken(pcx);
		if(Expression.isFirst(tk)){
			expression = new Expression(pcx);
			expression.parse(pcx);
			tk = ct.getCurrentToken(pcx);
			if(CToken.TK_RBRA == tk.getType()){
				tk = ct.getNextToken(pcx);
			} else {
				pcx.fatalError(tk.toExplainString() + "expressionの後ろは]です");
			}
		} else {
			pcx.fatalError(tk.toExplainString() + "[の後ろはexpressionです");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (expression != null) {

		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
	}
}