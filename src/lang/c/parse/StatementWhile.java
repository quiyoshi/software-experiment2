package lang.c.parse;

import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementWhile extends CParseRule{
	// statementWhile ::= WHILE LPAR condition RPAR LCUR { statement } RCUR

	private ArrayList<CParseRule> state;
	private CParseRule condition, program;
	private CToken wh;
	public StatementWhile(CParseContext pcx) {
		state = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return CToken.TK_WHILE == tk.getType();
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		wh = tk;

		tk = ct.getNextToken(pcx);
		if(CToken.TK_LPAR != tk.getType()) {
			pcx.fatalError(tk.toExplainString() + "条件式が誤っています");
		}

		tk = ct.getNextToken(pcx);
		if(!Condition.isFirst(tk)) {
			pcx.fatalError(tk.toExplainString() + "条件式を入れてください");
		}
		condition= new Condition(pcx);
		condition.parse(pcx);
		tk = ct.getCurrentToken(pcx);

		if(CToken.TK_RPAR != tk.getType()) {
			pcx.fatalError(tk.toExplainString() + "')'を付けてください");
		}

		tk = ct.getNextToken(pcx);
		if(CToken.TK_LCUR != tk.getType()) {
			pcx.fatalError(tk.toExplainString() + "{}は省略できません");
		}

		tk = ct.getNextToken(pcx);
		while(Statement.isFirst(tk)) {
			program = new Statement(pcx);
			program.parse(pcx);
			state.add(program);
			tk = ct.getCurrentToken(pcx);
		}

		if(CToken.TK_RCUR != tk.getType()) {
			pcx.fatalError(tk.toExplainString() + "'}'を付けてください");
		}

		tk = ct.getNextToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (condition != null) {
			condition.semanticCheck(pcx);
		}
		for(CParseRule index: state) {
			if (index != null) { index.semanticCheck(pcx); }
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		//PrintStream o = pcx.getIOContext().getOutStream();
		if (condition != null) {
			condition.codeGen(pcx);
		}
	}

}
