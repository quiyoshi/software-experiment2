package lang.c.parse;

import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class IntDecl extends CParseRule {
	// intDecl ::= INT declItem { COMMA declItem } SEMI

	private ArrayList<CParseRule> declarations;
	private CParseRule decl;
	private CToken integer, semi;
	public IntDecl(CParseContext pcx) {
		declarations = new ArrayList<CParseRule>();
	}
	public static boolean isFirst(CToken tk) {
		return CToken.TK_INT == tk.getType();
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		integer = tk;

		do {
			tk = ct.getNextToken(pcx);
			if(!DeclItem.isFirst(tk)){
				pcx.fatalError(tk.toExplainString() + "変数を正しく宣言できていません");
			} else {
				decl = new DeclItem(pcx);
				decl.parse(pcx);
				declarations.add(decl);
			}
			tk = ct.getCurrentToken(pcx);
		} while(CToken.TK_COMMA == tk.getType());

		if(CToken.TK_SEMI != tk.getType()){
			pcx.fatalError(tk.toExplainString() + ";を挿入してintDeclを終了してください");
		}
		semi = tk;

		tk = ct.getNextToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {

	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		//PrintStream o = pcx.getIOContext().getOutStream();
		for(CParseRule index: declarations){
			index.codeGen(pcx);
		}
	}
}
