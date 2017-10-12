package lang.c;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import lang.FatalErrorException;
import lang.Tokenizer;

public class CTokenizer extends Tokenizer<CToken, CParseContext> {
	private int			lineNo, colNo;
	private char		backCh;
	private boolean		backChExist = false;

	public CTokenizer(CTokenRule rule) {
		this.setRule(rule);
		lineNo = 1; colNo = 1;
	}

	private CTokenRule						rule;
	public void setRule(CTokenRule rule)	{ this.rule = rule; }
	public CTokenRule getRule()				{ return rule; }

	private InputStream in;
	private PrintStream err;

	private char readChar() {
		char ch;
		if (backChExist) {
			ch = backCh;
			backChExist = false;
		} else {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace(err);
				ch = (char) -1;
			}
		}
		++colNo;
		if (ch == '\n')  { colNo = 1; ++lineNo; }
//		System.out.print("'"+ch+"'("+(int)ch+")");
		return ch;
	}
	private void backChar(char c) {
		backCh = c;
		backChExist = true;
		--colNo;
		if (c == '\n') { --lineNo; }
	}

	// 現在読み込まれているトークンを返す
	private CToken currentTk = null;
	public CToken getCurrentToken(CParseContext pctx) {
		return currentTk;
	}
	// 新しく次のトークンを読み込んで返す
	public CToken getNextToken(CParseContext pctx) {
		in = pctx.getIOContext().getInStream();
		err = pctx.getIOContext().getErrStream();
		currentTk = readToken();
		if(currentTk.getType() == CToken.TK_NUM){			//16ビット
			int range = (int) Math.pow(2, 15);
			try {
				if(currentTk.getIntValue() <= range && currentTk.getIntValue() >= -range + 1) {
				} else {
					pctx.fatalError(currentTk.toExplainString() + "扱える数値は16ビットです");
				}
			} catch (FatalErrorException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("Token='" + currentTk.toString());
		return currentTk;
	}
	private CToken readToken() {
		CToken tk = null;
		char ch;
		int  startCol = colNo;
		StringBuffer text = new StringBuffer();

		int state = 0;
		boolean accept = false;
		while (!accept) {
			switch (state) {
			case 0:					// 初期状態
				ch = readChar();
				if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
				} else if (ch == (char) -1) {	// EOF
					startCol = colNo - 1;
					state = 1;
				} else if (ch >= '1' && ch <= '9') {
					startCol = colNo - 1;
					text.append(ch);
					state = 3;
				} else if (ch == '+') {
					startCol = colNo - 1;
					text.append(ch);
					state = 5;
				} else if (ch == '-') {
					startCol = colNo - 1;
					text.append(ch);
					state = 6;
				} else if (ch == '*') {
					startCol = colNo - 1;
					state = 7;
				} else if (ch == '/') {
					startCol = colNo - 1;
					state = 8;
				} else if (ch == '0') {
				    startCol = colNo - 1;
					text.append(ch);
					state = 12;
				} else if (ch == '&') {
					startCol = colNo - 1;
					text.append(ch);
					state = 15;
				} else if (ch == '(') {
					startCol = colNo - 1;
					text.append(ch);
					state = 16;
				} else if (ch == ')') {
					startCol = colNo - 1;
					text.append(ch);
					state = 17;
				} else {			// ヘンな文字を読んだ
					startCol = colNo - 1;
					text.append(ch);
					state = 2;
				}
				break;
			case 1:					// EOFを読んだ
				tk = new CToken(CToken.TK_EOF, lineNo, startCol, "end_of_file");
				accept = true;
				break;
			case 2:					// ヘンな文字を読んだ
				tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
				accept = true;
				break;
			case 3:					// 数（10進数）の開始
				ch = readChar();
				if (ch >= '0' && ch <= '9') {
					text.append(ch);
				} else {
					backChar(ch);
					state = 4;
				}
				break;
			case 4:					// 数の終わり
				tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
				accept = true;
				break;
			case 5:					// +を読んだ
				tk = new CToken(CToken.TK_PLUS, lineNo, startCol, "+");
				accept = true;
				break;
			case 6:                // -を読んだ
				tk = new CToken(CToken.TK_MINUS, lineNo, startCol, "-");
				accept = true;
				break;
			case 7:                // *を読んだ
				tk = new CToken(CToken.TK_MUL, lineNo, startCol, "*");
				accept = true;
				break;
			case 8:                // /を読んだ
				ch = readChar();
				if(ch == '/'){
					state = 9;
				} else if(ch == '*'){
					state = 10;
				} else {
					text.append(ch);
					backChar(ch);
					tk = new CToken(CToken.TK_DIV, lineNo, startCol, "/");
					accept = true;
				}
				break;
			case 9:                // コメント（単一行）
				ch = readChar();
				if(ch == '\n'){
					state = 0;
				} else if(ch == (char) -1)  {
					state = 1;
				} else {
				}
				break;
			case 10:               // コメント（複数行）の開始
				ch = readChar();
				if(ch == '*'){     // '*'/
					state = 11;
				} else if(ch == (char) -1) {  // EOF
					state = 1;
			    } else {
				}
				break;
			case 11:               // コメント（複数行）終わり
				ch = readChar();
				if(ch == '/'){     // *'/'
					state = 0;
				} else if(ch == (char) -1) {  // EOF
					state = 1;
				} else if(ch == '*') {
				} else {           // 終わりの記号（*/）でなければ復帰
					state = 10;
				}
				break;
			case 12:				// 数（8進数 or 16進数 or 0）の開始
				ch = readChar();
				if(ch == 'x' || ch == 'X'){      // 数（16進数）の開始
					text.append(ch);
					state = 14;
				} else if(ch >= '0' && ch <= '9'){  // 数（8進数）の開始
					text.append(ch);
					state = 13;
				} else {           //0
					state = 4;
					backChar(ch);
				}
				break;
			case 13:				// 数（8進数）の開始
				ch = readChar();
				if(ch >= '0' && ch <= '9'){
					text.append(ch);
				} else {
					state = 4;
					backChar(ch);
				}
				break;
			case 14:				// 数（16進数）の開始
				ch = readChar();
				if((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f')){
					text.append(ch);
				} else  {
					state = 4;
					backChar(ch);
				}
				break;
			case 15:				// アドレス値（&）
				tk = new CToken(CToken.TK_AMP, lineNo, startCol, text.toString());
				accept = true;
				break;
			case 16:				// 左括弧
				tk = new CToken(CToken.TK_LPAR, lineNo, startCol, text.toString());
				accept = true;
				break;
			case 17:				// 右括弧
				tk = new CToken(CToken.TK_RPAR, lineNo, startCol, text.toString());
				accept = true;
				break;
			}
		}
		return tk;
	}

	public void skipTo(CParseContext pctx, int t) {
		int i = getCurrentToken(pctx).getType();
		while (i != t && i != CToken.TK_EOF) {
			i = getNextToken(pctx).getType();
		}
		pctx.warning(getCurrentToken(pctx).toExplainString() + "まで読み飛ばしました");
	}
	public void skipTo(CParseContext pctx, int t1, int t2) {
		int i = getCurrentToken(pctx).getType();
		while (i != t1 && i != t2 && i != CToken.TK_EOF) {
			i = getNextToken(pctx).getType();
		}
		pctx.warning(getCurrentToken(pctx).toExplainString() + "まで読み飛ばしました");
	}
	public void skipTo(CParseContext pctx, int t1, int t2, int t3, int t4, int t5, int t6) {
		int i = getCurrentToken(pctx).getType();
		while (i != t1 && i != t2 && i != t3 && i != t4 && i != t5 && i != t6 && i != CToken.TK_EOF) {
			i = getNextToken(pctx).getType();
		}
		pctx.warning(getCurrentToken(pctx).toExplainString() + "まで読み飛ばしました");
	}
}
