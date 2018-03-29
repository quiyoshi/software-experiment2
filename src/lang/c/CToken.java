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
	public static final int TK_LCUR			= 11;				// {
	public static final int TK_RCUR			= 12;				// }
	public static final int TK_ASSIGN		= 13;				// =
	public static final int TK_SEMI			= 14;				// ;
	public static final int TK_COMMA			= 15;				// ,

	// Compare Operator
	public static final int TK_LT			= 21;				// <
	public static final int TK_LE			= 22;				// <=
	public static final int TK_GT			= 23;				// >
	public static final int TK_GE			= 24;				// >=
	public static final int TK_EQ			= 25;				// ==
	public static final int TK_NE			= 26;				// !=

	// Variable
	public static final int TK_INT			= 31;				// int
	public static final int TK_CONST			= 32;				// const
	public static final int TK_TRUE			= 33;				// true
	public static final int TK_FALSE			= 34;				// false

	// Condition
	public static final int TK_IF			= 41;				// if
	public static final int TK_ELSE			= 42;				// else
	public static final int TK_WHILE			= 43;				// while
	public static final int TK_INPUT			= 44;				// input
	public static final int TK_OUTPUT		= 45;				// output

	public CToken(int type, int lineNo, int colNo, String s) {
		super(type, lineNo, colNo, s);
	}
}
