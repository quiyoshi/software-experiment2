package lang.c;

import lang.SymbolTable;

public class CSymbolTable {
	private class OneSymbolTable extends SymbolTable<CSymbolTableEntry> {
		@Override
		public CSymbolTableEntry register(String name, CSymbolTableEntry e) { return put(name, e); }
		@Override
		public CSymbolTableEntry search(String name) { return get(name); }
	}

	private OneSymbolTable global;
	private OneSymbolTable local;

	public CSymbolTable() {
		global = new OneSymbolTable();
		local = new OneSymbolTable();
	}

	public void register(String name, CSymbolTableEntry e) {
		if(e.isGlobal()) {
			if(global.search(name) == null) {
				global.register(name, e);
			}
		} else {
			if(local.search(name) == null) {
				local.register(name, e);
			}
		}
	}

	public CSymbolTableEntry searchGlobal(String name) { return global.search(name); }
	public CSymbolTableEntry searchLocal(String name) { return local.search(name); }
	public void showGlobal(){ global.show(); }
	public void showLocal(){ local.show(); }
}
