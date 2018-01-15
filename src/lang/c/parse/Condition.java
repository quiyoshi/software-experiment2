package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class Condition extends CParseRule{
	// condition ::=
	//		expression (conditionLT | conditionLE | conditionGT | conditionGE | conditionEQ | conditionNE)
	//		| TRUE | FALSE
	//	LT='<'	LE='<='	GT='>'	GE='>='	EQ='=='	NE='!='

	private CParseRule expression, cond;
	private CToken logic;
	private int seq;
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
			logic = tk;
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
		if (expression != null && cond != null) {
			expression.semanticCheck(pcx);
			cond.semanticCheck(pcx);

			if(expression.getCType() != cond.getCType()) {
				pcx.fatalError(logic.toExplainString() +
						"左辺[" + expression.getCType().toString() + "]と右辺[" + cond.getCType().toString() + "]は比較できません");
			}
		}
		this.setCType(CType.getCType(CType.T_bool));
		this.setConstant(true);
	}

	// 真は1、偽は0
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		if (expression != null) {
			expression.codeGen(pcx);		// 左辺
			cond.codeGen(pcx);				// 右辺
			seq = pcx.getSeqId();
			if(cond instanceof ConditionLT) { this.codeGenLT(pcx); }
			else if(cond instanceof ConditionLE) { this.codeGenLE(pcx); }
			else if(cond instanceof ConditionGT) { this.codeGenGT(pcx); }
			else if(cond instanceof ConditionGE) { this.codeGenGE(pcx); }
			else if(cond instanceof ConditionEQ) { this.codeGenEQ(pcx); }
			else if(cond instanceof ConditionNE) { this.codeGenNE(pcx); }
			else {
				pcx.fatalError(logic.toExplainString() + "条件を解決できません");
			}
		} else {
			if(logic.getType() == CToken.TK_TRUE) { this.codeGenTrue(pcx); }
			else if(logic.getType() == CToken.TK_FALSE) { this.codeGenFalse(pcx); }
			else {
				pcx.fatalError(logic.toExplainString() + "boolean変数を解決できません");
			}
		}
	}

	private void codeGenLT(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;condition < (compare) starts");

		o.println("\tMOV\t-(R6), R0\t; ConditionLT: 2数を取り出して、比べる");		// 右辺
		o.println("\tMOV\t-(R6), R1\t; ConditionLT: ");								// 左辺
		o.println("\tMOV\t0x0001, R2\t; ConditionLT: set true");
		o.println("\tCMP\tR0, R1\t; ConditionLT: R1<R0 = R1-R0<0");		// CMP F, T :  T - F → PSW ; 左辺 < 右辺
		o.println("\tBRN\tLT" + seq + " ; ConditionLT:");				// 左辺 - 右辺 < 0 ならジャンプ
		o.println("\tCLR\tR2\t\t; ConditionLT: set false");
		o.println("LT" + seq + ":\tMOV\tR2, (R6)+\t; ConditionLT");

		o.println(";;; condition < (compare) completes");
	}
	private void codeGenLE(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;condition <= (compare) starts");

		o.println("\tMOV\t-(R6), R0\t; ConditionLE: 2数を取り出して、比べる");
		o.println("\tMOV\t-(R6), R1\t; ConditionLE: ");
		o.println("\tMOV\t0x0001, R2\t; ConditionLE: set true");
		o.println("\tCMP\tR0, R1\t; ConditionLE: R1<=R0 = R1-R0<=0");		// CMP F, T :  T - F → PSW ; 左辺 <= 右辺
		o.println("\tBRN\tLE" + seq + " ; ConditionLE:");				// 左辺 - 右辺 < 0 ならジャンプ
		o.println("\tBRZ\tLE" + seq + " ; ConditionLE:");				// 左辺 - 右辺 = 0 ならジャンプ
		o.println("\tCLR\tR2\t\t; ConditionLE: set false");
		o.println("LE" + seq + ":\tMOV\tR2, (R6)+\t; ConditionLE");

		o.println(";;; condition <= (compare) completes");
	}
	private void codeGenGT(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;condition > (compare) starts");

		o.println("\tMOV\t-(R6), R0\t; ConditionGT: 2数を取り出して、比べる");
		o.println("\tMOV\t-(R6), R1\t; ConditionGT: ");
		o.println("\tMOV\t0x0001, R2\t; ConditionGT: set true");
		o.println("\tCMP\tR1, R0\t; ConditionGT: R1>R0 = 0>R0-R1");		// CMP F, T :  T - F → PSW ; 左辺 > 右辺
		o.println("\tBRN\tGT" + seq + " ; ConditionGT:");				// 左辺 - 右辺 > 0 ならジャンプ
		o.println("\tCLR\tR2\t\t; ConditionGT: set false");
		o.println("GT" + seq + ":\tMOV\tR2, (R6)+\t; ConditionGT");

		o.println(";;;condition > (compare) completes");
	}
	private void codeGenGE(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;condition >= (compare) starts");

		o.println("\tMOV\t-(R6), R0\t; ConditionGE: 2数を取り出して、比べる");
		o.println("\tMOV\t-(R6), R1\t; ConditionGE: ");
		o.println("\tMOV\t0x0001, R2\t; ConditionGE: set true");
		o.println("\tCMP\tR1, R0\t; ConditionGE: R1>=R0 = 0>=R0-R1");		// CMP F, T :  T - F → PSW ; 左辺 >= 右辺
		o.println("\tBRN\tGE" + seq + " ; ConditionGE:");				// 左辺 - 右辺 > 0 ならジャンプ
		o.println("\tBRZ\tGE" + seq + " ; ConditionGE:");				// 左辺 - 右辺 = 0 ならジャンプ
		o.println("\tCLR\tR2\t\t; ConditionGE: set false");
		o.println("GE" + seq + ":\tMOV\tR2, (R6)+\t; ConditionGE");

		o.println(";;;condition >= (compare) completes");
	}
	private void codeGenEQ(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;condition == (compare) starts");

		o.println("\tMOV\t-(R6), R0\t; ConditionEQ: 2数を取り出して、比べる");
		o.println("\tMOV\t-(R6), R1\t; ConditionEQ: ");
		o.println("\tMOV\t0x0001, R2\t; ConditionEQ: set true");
		o.println("\tCMP\tR0, R1\t; ConditionEQ: R1=R0 = R1-R0=0");		// CMP F, T :  T - F → PSW ; 左辺 = 右辺
		o.println("\tBRZ\tEQ" + seq + " ; ConditionEQ:");				// 左辺 - 右辺 = 0 ならジャンプ
		o.println("\tCLR\tR2\t\t; ConditionLT: set false");
		o.println("EQ" + seq + ":\tMOV\tR2, (R6)+\t; ConditionEQ");

		o.println(";;;condition == (compare) completes");
	}
	private void codeGenNE(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;condition != (compare) starts");

		o.println("\tMOV\t-(R6), R0\t; ConditionNE: 2数を取り出して、比べる");
		o.println("\tMOV\t-(R6), R1\t; ConditionNE: ");
		o.println("\tCLR\tR2\t\t; ConditionNE: set false");
		o.println("\tCMP\tR0, R1\t; ConditionNE: R1=R0 = R1-R0=0");		// CMP F, T :  T - F → PSW ; 左辺 = 右辺
		o.println("\tBRZ\tNE" + seq + " ; ConditionNE:");				// 左辺 - 右辺 = 0 ならジャンプ
		o.println("\tMOV\t0x0001, R2\t; ConditionNE: set true");
		o.println("NE" + seq + ":\tMOV\tR2, (R6)+\t; ConditionNE");

		o.println(";;;condition != (compare) completes");
	}
	private void codeGenTrue(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;condition true starts");

		o.println("\tMOV\t#0x0001, (R6)+\t; Condition: set true");

		o.println(";;;condition true completes");
	}
	private void codeGenFalse(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;condition false starts");

		o.println("\tMOV\t#0x0000, (R6)+\t; Condition: set false");

		o.println(";;;condition false completes");
	}
}
