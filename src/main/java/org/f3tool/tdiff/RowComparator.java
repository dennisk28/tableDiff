package org.f3tool.tdiff;

public interface RowComparator {
	// diff msg is returned when two sides are different
	String compare(TxtTable leftTalbe, TxtTable.Row leftRow, TxtTable rightTable, 
			TxtTable.Row rightRow);
}
