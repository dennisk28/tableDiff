package org.f3tool.tdiff;

public interface Filter {
	public boolean filter(TxtTable table, TxtTable.Row row);
}
