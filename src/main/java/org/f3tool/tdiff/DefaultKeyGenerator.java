package org.f3tool.tdiff;

import java.util.List;

public class DefaultKeyGenerator implements KeyGenerator
{
	@SuppressWarnings("unchecked")
	public Comparable generateKey(TxtTable table, TxtTable.Row row)
	{
		StringBuffer sb = new StringBuffer();
		
		List<ComparisonConfig.Field> primaryFields = table.getPrimaryFields();
		
		for (ComparisonConfig.Field fld: primaryFields)
		{
			sb.append(table.getFieldValue(row, fld.getName()).toString());
		}
		
		return sb.toString();
	}

}
