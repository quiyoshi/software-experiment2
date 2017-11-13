package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Primary extends CParseRule {
	// primary ::= primaryMult | variable
	private CParseRule primary;
	public Primary(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return PrimaryMult.isFirst(tk) || Variable.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if(PrimaryMult.isFirst(tk)){
			primary = new PrimaryMult(pcx);
			primary.parse(pcx);
		} else if(Variable.isFirst(tk)){
			primary = new Variable(pcx);
			primary.parse(pcx);
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

class PrimaryMult  extends CParseRule {
	// primaryMULT ::= MULT variable
	private CParseRule variable;
	private CToken mult;
	public PrimaryMult(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MUL;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		mult = ct.getCurrentToken(pcx);

		// *の次の字句を読む
		CToken tk = ct.getNextToken(pcx);
		if(Variable.isFirst(tk)){
			variable = new Variable(pcx);
			variable.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "ポイント型*の後ろはvariableです");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (variable != null) {

		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
	}
}