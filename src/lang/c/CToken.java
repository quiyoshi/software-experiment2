package lang.c;

import lang.SimpleToken;

public class CToken extends SimpleToken {
	// Operator or Symbol
	public static final int TK_PLUS			= 2;				// +
	public static final int TK_MINUS         = 3;				// -
	public static final int TK_MUL           = 4;				// *
	public static final int TK_DIV           = 5;				// /
	public static final int TK_AMP           = 6;				// &
	public static final int TK_LPAR          = 7;				// (
	public static final int TK_RPAR          = 8;				// )
	public static final int TK_LBRA          = 9;				// [
	public static final int TK_RBRA          = 10;				// ]
	public static final int TK_ASSIGN		= 11;				// =
	public static final int TK_SEMI			= 12;				// ;
	public static final int TK_COMMA			= 13;				// ,

	// Compare Operator
	public static final int TK_LT			= 14;				// <
	public static final int TK_LE			= 15;				// <=
	public static final int TK_GT			= 16;				// >
	public static final int TK_GE			= 17;				// >=
	public static final int TK_EQ			= 18;				// ==
	public static final int TK_NE			= 19;				// !=

	// Variable
	public static final int TK_INT			= 20;				// int
	public static final int TK_CONST			= 21;				// const
	public static final int TK_TRUE			= 22;				// true
	public static final int TK_FALSE			= 23;				// false

	public CToken(int type, int lineNo, int colNo, String s) {
		super(type, lineNo, colNo, s);
	}
}
