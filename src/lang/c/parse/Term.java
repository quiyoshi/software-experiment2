package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

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
		factor = new Factor(pcx); //factor用の節点を作る
		factor.parse(pcx); //factorの解析を行う
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		while(TermMult.isFirst(tk) || TermDiv.isFirst(tk)){
			if(TermMult.isFirst(tk)){
				term = new TermMult(pcx, factor); //TermMult用の節点を作る（解析済みのfactorを渡す）
			} else if(TermDiv.isFirst(tk)) {
				term = new TermDiv(pcx, factor); //TermDiv用の節点を作る（解析済みのfactorを渡す）
			}
			term.parse(pcx);
			factor = term;
			tk = ct.getCurrentToken(pcx);
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
	private CParseRule right, left;
	public TermMult(CParseContext pcx, CParseRule left) {
		this.left = left;
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
			right = new Factor(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "*の後ろはfactorです");
		}
	}
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		// 掛け算の型計算規則
		final int s[][] = {
			//	T_err			T_int			T_pint			T_aint			T_apint
			{	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_err
			{	CType.T_err,	CType.T_int,	CType.T_err	,	CType.T_err,	CType.T_err},	// T_int
			{	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_pint
			{	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_aint
			{	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_apint
		};
		int lt = 0, rt = 0;
		boolean lc = false, rc = false;
		if (left != null) {
			left.semanticCheck(pcx);
			lt = left.getCType().getType();		// *の左辺の型
			lc = left.isConstant();
		} else {
			pcx.fatalError(mul.toExplainString() + "左辺がありません");
		}
		if (right != null) {
			right.semanticCheck(pcx);
			rt = right.getCType().getType();	// *の右辺の型
			rc = right.isConstant();
		} else {
			pcx.fatalError(mul.toExplainString() + "右辺がありません");
		}
		int nt = s[lt][rt];						// 規則による型計算
		if (nt == CType.T_err) {
			pcx.fatalError(mul.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は掛けられません");
		}
		this.setCType(CType.getCType(nt));
		this.setConstant(lc && rc);				// *の左右両方が定数のときだけ定数
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; termMult starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			o.println("\tJSR\tMUL\t; TermMult:");
		}
		o.println(";;; termMult completes");
	}
}

class TermDiv extends CParseRule {
	// termDiv ::= DIV factor

	private CToken div;
	private CParseRule right, left;
	public TermDiv(CParseContext pcx, CParseRule left) {
		this.left = left;
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_DIV;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		div = ct.getCurrentToken(pcx);
		// /の次の字句を読む
		CToken tk = ct.getNextToken(pcx);
		if (Factor.isFirst(tk)) {
			right = new Factor(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "/の後ろはfactorです");
		}
	}
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		// 割り算の型計算規則
		final int s[][] = {
			//	T_err			T_int			T_pint			T_aint			T_apint
			{	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_err
			{	CType.T_err,	CType.T_int,	CType.T_err	,	CType.T_err,	CType.T_err},	// T_int
			{	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_pint
			{	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_aint
			{	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_apint
		};
		int lt = 0, rt = 0;
		boolean lc = false, rc = false;
		if (left != null) {
			left.semanticCheck(pcx);
			lt = left.getCType().getType();		// /の左辺の型
			lc = left.isConstant();
		} else {
			pcx.fatalError(div.toExplainString() + "左辺がありません");
		}
		if (right != null) {
			right.semanticCheck(pcx);
			rt = right.getCType().getType();	// /の右辺の型
			rc = right.isConstant();
		} else {
			pcx.fatalError(div.toExplainString() + "右辺がありません");
		}
		int nt = s[lt][rt];						// 規則による型計算
		if (nt == CType.T_err) {
			pcx.fatalError(div.toExplainString() + "左辺の型[" + left.getCType().toString() + "]から右辺の型[" + right.getCType().toString() + "]は割れません");
		}
		this.setCType(CType.getCType(nt));
		this.setConstant(lc && rc);				// /の左右両方が定数のときだけ定数
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; termDiv starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			o.println("\tJSR\tDIV\t; TermDiv:");
		}
		o.println(";;; termDiv completes");
	}
}