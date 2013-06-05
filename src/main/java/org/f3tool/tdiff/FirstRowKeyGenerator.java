package org.f3tool.tdiff;

public class FirstRowKeyGenerator implements KeyGenerator
{
	@SuppressWarnings("unchecked")
	public Comparable generateKey(TxtTable table, TxtTable.Row row)
	{
		return row.getFieldValue(0);
	}
}
