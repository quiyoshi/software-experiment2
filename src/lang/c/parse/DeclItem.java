package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class DeclItem extends CParseRule {
	// declItem ::= [ MULT ] IDENT [ LBRA number RBRA ]

	private CToken mul, lbra, rbra;
	private CToken ident;
	private CType entry;
	private int words;
	private CSymbolTableEntry symbol;
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
		entry = CType.getCType(CType.T_int);
		words = 1;

		if(CToken.TK_MUL == tk.getType()){
			entry = CType.getCType(CType.T_pint);
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

			entry = (entry == CType.getCType(CType.T_int)) ? CType.getCType(CType.T_aint) : CType.getCType(CType.T_apint);
			words = ((Number)number).getNumber();
		}

		symbol = new CSymbolTableEntry(entry, words, false, true, 0);		// 型, ワード数, 定数, 大域変数, アドレス

		if(pcx.getSymbolTable().searchGlobal(ident.getText()) == null) {
			pcx.getSymbolTable().register(ident.getText(), symbol);
		} else {
			pcx.fatalError(ident.toExplainString() + "変数名" + ident.getText() + "はすでに宣言されています");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {

	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if(entry == CType.getCType(CType.T_int) || entry == CType.getCType(CType.T_pint)){
			o.println("\t" + ident.getText() + ":\t.WORD\t0\t; DeclItem: 単純変数、ポインタ変数用の領域確保");
		} else {
			o.println("\t" + ident.getText() + ":\t.BLKM\t"+ words +"\t; DeclItem: 配列変数用の領域確保");
		}
	}
}
