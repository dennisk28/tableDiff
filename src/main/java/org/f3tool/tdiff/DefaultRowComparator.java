package org.f3tool.tdiff;

import java.util.List;

public class DefaultRowComparator implements RowComparator 
{
	private ComparisonConfig config;
	
	public DefaultRowComparator()
	{
		config = ComparisonConfig.getInstance();
	}
	
	public String compare(TxtTable leftTable, TxtTable.Row leftRow, TxtTable rightTable, 
			TxtTable.Row rightRow)
	{
		StringBuffer sb = new StringBuffer();
		
		List<ComparisonConfig.ComparisonPair> pairs = config.getComparisonPairs();
		
		for(int i = 0, size = pairs.size(); i < size; i++)
		{
			boolean isEqual = true;
			
			ComparisonConfig.ComparisonPair pair = pairs.get(i);
			
			String leftValue = leftTable.getFieldValue(leftRow, pair.getLeftField().getName());
			String rightValue = rightTable.getFieldValue(rightRow, pair.getRightField().getName());
			
			String type = pair.getLeftField().getType();
			
			if (type.equalsIgnoreCase(ComparisonConfig.Field.FIELD_TYPE_String))
			{
				if (!leftValue.equals(rightValue)) isEqual = false;
			}
			else if (type.equalsIgnoreCase(ComparisonConfig.Field.FIELD_TYPE_Double))
			{
				int compareMethod = pair.getCompareMethod();
				
				double leftValueD = Double.parseDouble(leftValue);
				double rightValueD = Double.parseDouble(rightValue);
				
				if (leftValueD != rightValueD)
				{
					if (compareMethod == ComparisonConfig.VALUE_COMPARE_METHOD_PECT)
					{
						double denom = leftValueD;
					
						if (leftValueD == 0) denom = rightValueD;
							
						if (Math.abs(leftValueD - rightValueD) / denom 
								> config.getPercentageThreshold())
						{
							isEqual = false;
						}
					}
					else if (compareMethod == ComparisonConfig.VALUE_COMPARE_METHOD_ABS)
					{
						if (Math.abs(leftValueD - rightValueD) > config.getAbsoluteThreshold())
						{
							isEqual = false;
						}
					}
					else
					{
						isEqual = false;
					}
				}
			}

			if (!isEqual)
			{
				if (sb.length() > 0) sb.append("\n");
				
				sb.append("Field Mismatch: <<(" + pair.getLeftField().getName() + ")" + leftValue 
						+ ">>(" + pair.getRightField().getName() + ")" + rightValue);
			}
		}
		
		if (sb.length() > 0)
			return sb.toString();
		else
			return null;
	}
}
