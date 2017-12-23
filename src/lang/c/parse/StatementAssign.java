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

		tk = ct.getNextToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (primary != null && expression != null) {
			primary.semanticCheck(pcx);
			expression.semanticCheck(pcx);

			if (primary.isConstant()) {
				pcx.fatalError(assign.toExplainString() + "定数に代入はできません");
			}
			if (primary.getCType() != expression.getCType()) {
				pcx.fatalError(assign.toExplainString() +
						"左辺の型[" + primary.getCType().toString() +
						"]に右辺の型[" + expression.getCType().toString() + "]は代入できません");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (primary != null && expression != null) {
			expression.codeGen(pcx);
			primary.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; StatementAssign: 左辺の変数に右辺の値を書き込む");
			o.println("\tMOV\t-(R6), (R0)\t; StatementAssign:");
		}

	}
}
