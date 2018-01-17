package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementIf extends CParseRule{
	// statementIf ::= IF LPAR condition RPAR LCUR  { statement } RCUR { statementElse }

	private ArrayList<CParseRule> state, other;
	private CParseRule condition, program;
	private int seq;
	public StatementIf(CParseContext pcx) {
		state = new ArrayList<CParseRule>();
		other = new ArrayList<CParseRule>();
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
			pcx.fatalError(tk.toExplainString() + "'}'を付けてください");
		}

		tk = ct.getNextToken(pcx);

		while(StatementElse.isFirst(tk)) {
			program = new StatementElse(pcx);
			program.parse(pcx);
			other.add(program);
			tk = ct.getCurrentToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (condition != null) {
			condition.semanticCheck(pcx);
		}
		for(CParseRule index: state) {
			if (index != null) { index.semanticCheck(pcx); }
		}
		for(CParseRule index: other) {
			if (index != null) { index.semanticCheck(pcx); }
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;;statement if starts");

		// 条件文 true or false
		if (condition != null) {
			condition.codeGen(pcx);
		}

		seq = pcx.getSeqId();
		o.println("\tMOV\t-(R6), R2\t; StatementIf: スタックから真偽値を降ろす");
		o.println("\tBRZ\tIF" + seq + " ; StatementIf:");

		// trueのときに実行するコード
		for(CParseRule index: state) {
			if (index != null) { index.codeGen(pcx); }
		}
		o.println("\tJMP\tEL" + seq + ":無条件でelse文にジャンプする");

		// falseのときに実行するコード
		o.println("IF" + seq + ":");
		for(CParseRule index: other) {
			if (index != null) { index.codeGen(pcx); }
		}

		o.println("EL" + seq + ":");


		o.println(";;;statement if completes");
	}

}

class StatementElse extends CParseRule {
	// statementElse ::= ELSE [ IF LPAR condition RPAR ] LCUR { statement } RCUR

	private ArrayList<CParseRule> state;
	private CParseRule condition, program;
	private int seq;
	public StatementElse(CParseContext pcx) {
		state = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return CToken.TK_ELSE == tk.getType();
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		tk = ct.getNextToken(pcx);

		if(CToken.TK_IF == tk.getType()) {
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
		}

		if(CToken.TK_LCUR != tk.getType()){
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
		PrintStream o = pcx.getIOContext().getOutStream();

		// 条件文 true or false
		if (condition != null) {
			condition.codeGen(pcx);

			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R2\t; StatementElse: スタックから真偽値を降ろす");
			o.println("\tBRZ\tIF" + seq + " ; StatementElse:");
		}

		for(CParseRule index: state) {
			if (index != null) { index.codeGen(pcx); }
		}
		if (condition != null) {
			o.println("IF" + seq + ":");
		}
	}

}
