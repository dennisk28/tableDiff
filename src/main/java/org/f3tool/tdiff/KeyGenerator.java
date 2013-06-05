package org.f3tool.tdiff;

public interface KeyGenerator {
	@SuppressWarnings("unchecked")
	public Comparable generateKey(TxtTable table, TxtTable.Row row);
}
