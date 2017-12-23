package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class ConstItem extends CParseRule {
	// constItem ::= [ MULT ] IDENT ASSIGN [ AMP ] number

	private CToken mul, assign, amp;
	private CToken ident;
	private CParseRule number;
	public ConstItem(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return CToken.TK_MUL == tk.getType() || CToken.TK_IDENT == tk.getType();
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(CToken.TK_MUL == tk.getType()){
			mul = tk;
			tk = ct.getNextToken(pcx);
		}

		if(CToken.TK_IDENT != tk.getType()){
			pcx.fatalError(tk.toExplainString() + "定数を宣言してください");
		} else {
			ident = tk;
			tk = ct.getNextToken(pcx);
		}

		if(CToken.TK_ASSIGN != tk.getType()){
			pcx.fatalError(tk.toExplainString() + "値を入れてください");
		} else {
			assign = tk;
			tk = ct.getNextToken(pcx);
		}

		if(CToken.TK_AMP == tk.getType()){
			amp = tk;
			tk = ct.getNextToken(pcx);
		}

		if(!Number.isFirst(tk)){
			pcx.fatalError(tk.toExplainString() + "数値を代入してください");
		}
		number = new Number(pcx);
		number.parse(pcx);
		tk = ct.getCurrentToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {

	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
	}
}
