package lang.c.parse;

import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class ConstDecl extends CParseRule {
	// constDecl ::= CONST INT constItem { COMMA constItem } SEMI

	private ArrayList<CParseRule> declarations;
	private CParseRule decl;
	private CToken constant, integer, semi;
	public ConstDecl(CParseContext pcx) {
		declarations = new ArrayList<CParseRule>();
	}
	public static boolean isFirst(CToken tk) {
		return CToken.TK_CONST == tk.getType();
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		constant = tk;

		tk = ct.getNextToken(pcx);
		if (CToken.TK_INT != tk.getType()) {
			pcx.fatalError(tk.toExplainString() + "型を宣言してください");
		}
		integer = tk;

		do {
			tk = ct.getNextToken(pcx);
			if(!ConstItem.isFirst(tk)){
				pcx.fatalError(tk.toExplainString() + "定数を正しく宣言できていません");
			} else {
				decl = new ConstItem(pcx);
				decl.parse(pcx);
				declarations.add(decl);
			}
			tk = ct.getCurrentToken(pcx);
		} while(CToken.TK_COMMA == tk.getType());

		if(CToken.TK_SEMI != tk.getType()){
			pcx.fatalError(tk.toExplainString() + ";を挿入してconstDeclを終了してください");
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
