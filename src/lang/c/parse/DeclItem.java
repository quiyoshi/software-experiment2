package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class DeclItem extends CParseRule {
	// declItem ::= [ MULT ] IDENT [ LBRA number RBRA ]

	private CToken mul, lbra, rbra;
	private CToken ident;
	private CParseRule number;
	public DeclItem(CParseContext pcx) {
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
			pcx.fatalError(tk.toExplainString() + "変数を宣言してください");
		} else {
			ident = tk;
			tk = ct.getNextToken(pcx);
		}

		if(CToken.TK_LBRA == tk.getType()){
			lbra = tk;
			tk = ct.getNextToken(pcx);
			if(!Number.isFirst(tk)){
				pcx.fatalError(tk.toExplainString() + "[]の中はNumberです");
			}
			number = new Number(pcx);
			number.parse(pcx);
			tk = ct.getCurrentToken(pcx);
			if(CToken.TK_RBRA != tk.getType()){
				pcx.fatalError(tk.toExplainString() + "]を付けてください");
			}
			rbra = tk;

			tk = ct.getNextToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {

	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
	}
}
