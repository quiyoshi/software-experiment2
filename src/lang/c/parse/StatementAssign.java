package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementAssign extends CParseRule{
	// statementAssign ::= primary ASSIGN expression SEMT

	private CToken assign, semi;
	private CParseRule primary, expression;
	public StatementAssign(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Primary.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		primary = new Primary(pcx);
		primary.parse(pcx);
		assign = ct.getCurrentToken(pcx);
		if(assign.getType() != CToken.TK_ASSIGN){
			pcx.fatalError(assign.toExplainString() + "変数への代入が誤っています");
		}
		tk = ct.getNextToken(pcx);
		if(Expression.isFirst(tk)){
			expression = new Expression(pcx);
			expression.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "=の右辺はexpressionです");
		}
		semi = ct.getCurrentToken(pcx);
		if(semi.getType() != CToken.TK_SEMI){
			pcx.fatalError(assign.toExplainString() + ";を挿入してstatementを終了してください");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (primary != null) {

		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
	}
}
