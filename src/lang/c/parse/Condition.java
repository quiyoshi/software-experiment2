package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Condition extends CParseRule{
	// condition ::=
	//		expression (conditionLT | conditionLE | conditionGT | conditionGE | conditionEQ | conditionNE)
	//		| TRUE | FALSE
	//	LT='<'	LE='<='	GT='>'	GE='GE'	EQ='=='	NE='!='

	private CParseRule expression, cond;
	private CToken logic;
	public Condition(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Expression.isFirst(tk) || CToken.TK_TRUE == tk.getType() || CToken.TK_FALSE == tk.getType();
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);

			tk = ct.getCurrentToken(pcx);
			if(ConditionLT.isFirst(tk)) { cond = new ConditionLT(pcx); }
			else if(ConditionLE.isFirst(tk)) { cond = new ConditionLE(pcx); }
			else if(ConditionGT.isFirst(tk)) { cond = new ConditionGT(pcx); }
			else if(ConditionGE.isFirst(tk)) { cond = new ConditionGE(pcx); }
			else if(ConditionEQ.isFirst(tk)) { cond = new ConditionEQ(pcx); }
			else if(ConditionNE.isFirst(tk)) { cond = new ConditionNE(pcx); }
			else {
				pcx.fatalError(tk.toExplainString() + "boolean型に解決できません");
			}
			cond.parse(pcx);
		} else {
			logic = tk;					// ture or false
			tk = ct.getNextToken(pcx);
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
