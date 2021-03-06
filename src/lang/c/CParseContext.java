package lang.c;

import lang.IOContext;
import lang.ParseContext;

public class CParseContext extends ParseContext {
	private CSymbolTable symbolTable;
	public CParseContext(IOContext ioCtx,  CTokenizer tknz) {
		super(ioCtx, tknz);
		symbolTable = new CSymbolTable();
	}

	@Override
	public CTokenizer getTokenizer()		{ return (CTokenizer) super.getTokenizer(); }

	private int seqNo = 0;
	private int conNo = 0;
	public int getSeqId() { return ++seqNo; }
	public int getConId() { return ++ conNo; }
	public int currentConId() { return conNo;}
	public CSymbolTable getSymbolTable() { return symbolTable; }
}
