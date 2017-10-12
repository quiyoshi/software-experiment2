package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Term extends CParseRule {
	// term ::= factor { termMult | termDiv }
	private CParseRule factor = null,  term = null;
	public Term(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Factor.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		factor = new Factor(pcx);
		factor.parse(pcx);

		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if(TermMult.isFirst(tk)){
			term = new TermMult(pcx);
			term.parse(pcx);
		} else if(TermDiv.isFirst(tk)) {
			term = new TermDiv(pcx);
			term.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factor != null) {
			factor.semanticCheck(pcx);
			this.setCType(factor.getCType());		// factor の型をそのままコピー
			this.setConstant(factor.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; term starts");
		if (factor != null) { factor.codeGen(pcx); }
		o.println(";;; term completes");
	}
}

class TermMult extends CParseRule {
	// termMult ::= MULT factor

	private CToken mul;
	private CParseRule factor;
	public TermMult(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MUL;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		mul = ct.getCurrentToken(pcx);
		// *の次の字句を読む
		CToken tk = ct.getNextToken(pcx);
		if (Factor.isFirst(tk)) {
			factor = new Factor(pcx);
			factor.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "+の後ろはfactorです");
		}
	}
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factor != null) {
			factor.semanticCheck(pcx);
			this.setCType(factor.getCType());		// factor の型をそのままコピー
			this.setConstant(factor.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; termMult starts");
		if (factor != null) { factor.codeGen(pcx); }
		o.println(";;; termMult completes");
	}
}

class TermDiv extends CParseRule {
	// termDiv ::= MULT factor

	private CToken div;
	private CParseRule factor;
	public TermDiv(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_DIV;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		div = ct.getCurrentToken(pcx);
		// *の次の字句を読む
		CToken tk = ct.getNextToken(pcx);
		if (Factor.isFirst(tk)) {
			factor = new Factor(pcx);
			factor.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "+の後ろはfactorです");
		}
	}
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factor != null) {
			factor.semanticCheck(pcx);
			this.setCType(factor.getCType());		// factor の型をそのままコピー
			this.setConstant(factor.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; termDiv starts");
		if (factor != null) { factor.codeGen(pcx); }
		o.println(";;; termDiv completes");
	}
}