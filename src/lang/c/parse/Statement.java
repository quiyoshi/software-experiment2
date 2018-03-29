package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Statement extends CParseRule{
	// statement ::= statementIfElse | statementWhile | statementAssign | statementIn | statementOut

	private CParseRule child;
	public Statement(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return StatementIfElse.isFirst(tk) | StatementWhile.isFirst(tk) | StatementAssign.isFirst(tk)
				| StatementIn.isFirst(tk) | StatementOut.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(StatementIfElse.isFirst(tk)) {
			child = new StatementIfElse(pcx);
			child.parse(pcx);
		} else if(StatementWhile.isFirst(tk)) {
			child = new StatementWhile(pcx);
			child.parse(pcx);
		} else if(StatementAssign.isFirst(tk)){
			child = new StatementAssign(pcx);
			child.parse(pcx);
		} else if(StatementIn.isFirst(tk)) {
			child = new StatementIn(pcx);
			child.parse(pcx);
		} else {
			child = new StatementOut(pcx);
			child.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (child != null) {
			child.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		//PrintStream o = pcx.getIOContext().getOutStream();
		if (child != null) {
			child.codeGen(pcx);
		}
	}
}
