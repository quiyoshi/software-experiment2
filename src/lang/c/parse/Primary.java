package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

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
			primary.semanticCheck(pcx);
			this.setCType(primary.getCType());
			this.setConstant(primary.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (primary != null) {
			primary.codeGen(pcx);
		}
	}

	protected CParseRule getChildClass() {
		return primary;
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
			variable.semanticCheck(pcx);
			if(variable.getCType() == CType.getCType(CType.T_int)){
				pcx.fatalError(mult.toExplainString() + "int型をポインタ参照できません");
			}
			this.setCType(variable.getCType());
			this.setConstant(variable.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (variable != null){
			variable.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; PrimaryMult: アドレスを取り出して、内容を参照して、積む<"
					+ mult.toExplainString() + ">");
			o.println("\tMOV\t(R0), (R6)+\t; PrimaryMult:");
		}
	}
}