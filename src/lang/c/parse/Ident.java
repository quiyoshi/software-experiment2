package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Ident  extends CParseRule {
	// ident ::= IDENT
	private CToken ident;
	private CSymbolTableEntry entry;
	public Ident(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return CToken.TK_IDENT == tk.getType();
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		ident = tk;							//ここで綴りを保存しておく
		tk = ct.getNextToken(pcx);
		if((entry = pcx.getSymbolTable().searchGlobal(ident.getText())) == null) {
			pcx.fatalError(ident.toExplainString() + "変数" + ident.getText() + "は未定義です");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		this.setCType(entry.getCType());
		this.setConstant(entry.isConst());
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; ident starts");
		if(ident != null) {
			o.println("\tMOV\t#" + ident.getText() + ", (R6)+\t; Ident: 変数アドレスを積む<" + ident.toExplainString() + ">");
		}
		o.println(";;; ident completes");
	}
}
