package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementOut extends CParseRule{
	// statementOut ::= OUTPUT expression SEMI

	private CParseRule child;
	public StatementOut(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return CToken.TK_OUTPUT == tk.getType();
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		tk = ct.getNextToken(pcx);
		if(!Expression.isFirst(tk)) {
			pcx.fatalError(tk.toExplainString() + "outputにはexpressionを用いてください");
		}
		child = new Expression(pcx);
		child.parse(pcx);

		tk = ct.getCurrentToken(pcx);
		if(CToken.TK_SEMI != tk.getType()) {
			pcx.fatalError(tk.toExplainString() + "文末には;を挿入してください");
		}

		tk = ct.getNextToken(pcx);
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
