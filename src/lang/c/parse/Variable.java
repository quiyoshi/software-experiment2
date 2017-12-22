package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class Variable extends CParseRule{
	// variable ::= ident [ array ]

	private CToken arr;
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
		arr = tk;
		ident = new Ident(pcx);
		ident.parse(pcx);

		tk = ct.getCurrentToken(pcx);
		if(Array.isFirst(tk)){
			array = new Array(pcx);
			array.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (ident != null) {
			ident.semanticCheck(pcx);
			this.setCType(ident.getCType());
			this.setConstant(ident.isConstant());

			if(array != null){
				array.semanticCheck(pcx);
			} else {
				if(this.getCType() == CType.getCType(CType.T_aint)){
					pcx.fatalError(arr.toExplainString() + "配列名の識別子が誤っています");
				}
			}

		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if(ident != null){
			ident.codeGen(pcx);
		}
		if(array != null){
			array.codeGen(pcx);
		}
	}
}

class Array extends CParseRule{
	// array ::= LBRA expression RBRA

	private CToken bra;
	private CParseRule expression;
	public Array(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return CToken.TK_LBRA == tk.getType();
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		bra = ct.getCurrentToken(pcx);
		CToken tk = ct.getNextToken(pcx);

		if (Expression.isFirst(tk)){
			expression = new Expression(pcx);
			expression.parse(pcx);
			tk = ct.getCurrentToken(pcx);
			if (CToken.TK_RBRA == tk.getType()){
				tk = ct.getNextToken(pcx);
			} else {
				pcx.fatalError(tk.toExplainString() + "expressionの後ろは]です");
			}
		} else {
			pcx.fatalError(tk.toExplainString() + "[の後ろはexpressionです");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		expression.semanticCheck(pcx);
		if (expression != null && expression.getCType().getType() == CType.T_int) {
			this.setCType(expression.getCType());
			this.setConstant(false);
		} else {
			pcx.fatalError(bra.toExplainString() + "配列の添字にはint型を入れてください");
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (expression != null){
			o.println(";;; array starts");
			expression.codeGen(pcx);
			o.println("\tADD\t-(R6), R6\t; Array:基点アドレスから変位だけSPをずらす");
			o.println(";;; array completes");
		}
	}
}
