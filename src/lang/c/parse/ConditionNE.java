package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class ConditionNE extends CParseRule{
	// conditionNE ::= NE expression
	//	NE='!='

	private CParseRule expression;
	private CToken ne;
	public ConditionNE(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return CToken.TK_NE == tk.getType();
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		ne = tk;

		if(Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
		} else {
			pcx.fatalError(ne.toExplainString() + "条件判定が誤っています");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (expression != null) {
			expression.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		//PrintStream o = pcx.getIOContext().getOutStream();
		if (expression != null) {
			expression.codeGen(pcx);
		}
	}

}
