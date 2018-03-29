package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementIfElse extends CParseRule {
	// statementIfElse ::= statementIf [ ELSE ( statementIfElse | LCUR { statement } RCUR ) ]

	private ArrayList<CParseRule> state;
	private CParseRule ifstate, elseIfstate, program;
	private int seq;
	public StatementIfElse(CParseContext pcx) {
		state = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return StatementIf.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		ifstate = new StatementIf(pcx);
		ifstate.parse(pcx);

		tk = ct.getCurrentToken(pcx);

		if(CToken.TK_ELSE == tk.getType()) {
			tk = ct.getNextToken(pcx);
			if(StatementIfElse.isFirst(tk)) {
				elseIfstate = new StatementIfElse(pcx);
				elseIfstate.parse(pcx);
			} else if(CToken.TK_LCUR == tk.getType()) {
				tk = ct.getNextToken(pcx);
				while(Statement.isFirst(tk)) {
					program = new Statement(pcx);
					program.parse(pcx);
					state.add(program);
					tk = ct.getCurrentToken(pcx);
				}

				if(CToken.TK_RCUR != tk.getType()) {
					pcx.fatalError(tk.toExplainString() + "else節を閉じてください");
				}
				tk = ct.getNextToken(pcx);
			} else {
				pcx.fatalError(tk.toExplainString() + "elseを解決できません");
			}
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if(ifstate != null) {
			ifstate.semanticCheck(pcx);
		}
		if(elseIfstate != null) {
			elseIfstate.semanticCheck(pcx);
		}
		for(CParseRule index: state) {
			if (index != null) { index.semanticCheck(pcx); }
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;statement if starts");

		seq = pcx.getConId();
		if(ifstate != null) {
			ifstate.codeGen(pcx);
		}
		if(elseIfstate != null) {
			elseIfstate.codeGen(pcx);
		}
		for(CParseRule index: state) {
			if (index != null) { index.codeGen(pcx); }
		}
		o.println("EL" + seq + ":");

		o.println(";;;statement if completes");
	}
}

class StatementIf extends CParseRule{
	// statementIf ::= IF LPAR condition RPAR LCUR  { statement } RCUR

	private ArrayList<CParseRule> state;
	private CParseRule condition, program;
	public StatementIf(CParseContext pcx) {
		state = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return CToken.TK_IF == tk.getType();
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

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
			pcx.fatalError(tk.toExplainString() + "if節を閉じてください");
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
		PrintStream o = pcx.getIOContext().getOutStream();

		// 条件文 true or false
		if (condition != null) {
			condition.codeGen(pcx);
		}

		o.println("\tMOV\t-(R6), R2\t; StatementIf: スタックから真偽値を降ろす");
		o.println("\tBRZ\tIF" + pcx.currentConId() + " ; StatementIf:");

		// trueのときに実行するコード
		for(CParseRule index: state) {
			if (index != null) { index.codeGen(pcx); }
		}

		o.println("\tJMP\tEL" + pcx.currentConId() + "; 無条件でelse文にジャンプする");
		o.println("IF" + pcx.currentConId() + ":");
	}
}
