package org.f3tool.tdiff;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ComparisonConfig 
{
	public static int VALUE_COMPARE_METHOD_NORMAL = 0;
	public static int VALUE_COMPARE_METHOD_PECT = 1;
	public static int VALUE_COMPARE_METHOD_ABS = 2;
	
	private static ComparisonConfig config;
	private double percentageThreshold;
	private double absoluteThreshold;
	private int defaultValueCompareMethod;
	private List<ComparisonPair> comparisonPairList;
	private List<Field> leftPrimaryFields;
	private List<Field> rightPrimaryFields;
	private RowComparator rowComparator;
	private KeyGenerator leftKeyGenerator;
	private KeyGenerator rightKeyGenerator;
	private Filter leftFilter;
	private Filter rightFilter;
	private String rightDelimiter;
	private String leftDelimiter;
	private char leftEscape;
	private char rightEscape;
	
	public Filter getLeftFilter() {
		return leftFilter;
	}

	public Filter getRightFilter() {
		return rightFilter;
	}

	public char getLeftEscape() {
		return leftEscape;
	}

	public char getRightEscape() {
		return rightEscape;
	}

	public String getRightDelimiter() {
		return rightDelimiter;
	}

	public String getLeftDelimiter() {
		return leftDelimiter;
	}

	public RowComparator getRowComparator() 
	{
		if (rowComparator == null)
			rowComparator = new DefaultRowComparator();

		return rowComparator;
	}

	public KeyGenerator getLeftKeyGenerator()
	{
		if (leftKeyGenerator == null)
			leftKeyGenerator = new DefaultKeyGenerator();
		
		return leftKeyGenerator;
	}

	public KeyGenerator getRightKeyGenerator()
	{
		if (rightKeyGenerator == null)
			rightKeyGenerator = new DefaultKeyGenerator();
		
		return rightKeyGenerator;
	}
	
	public double getPercentageThreshold() {
		return percentageThreshold;
	}

	public double getAbsoluteThreshold() {
		return absoluteThreshold;
	}

	static public ComparisonConfig getInstance()
	{
		if (config == null)
		{
			config = new ComparisonConfig();
		}

		return config;
	}
	
	private int str2ValueCompareMethod(String methodName)
	{
		if (methodName.equalsIgnoreCase("Normal"))
			return VALUE_COMPARE_METHOD_NORMAL;

		if (methodName.equalsIgnoreCase("Percentage"))
			return VALUE_COMPARE_METHOD_PECT;

		if (methodName.equalsIgnoreCase("Absolute"))
			return VALUE_COMPARE_METHOD_ABS;
		
		return -1;
	}

	public void init(String configFile) throws DiffException
	{
		Properties props = new Properties();
		
		try
		{
			props.load(new FileInputStream(configFile));
		} 
		catch (IOException e)
		{
			throw new DiffException(e);
		}
		
		this.leftPrimaryFields = new ArrayList<Field>();
		this.rightPrimaryFields = new ArrayList<Field>();
		this.comparisonPairList = new ArrayList<ComparisonPair>();
		
		Iterator<String> it =  props.stringPropertyNames().iterator();
		
		while (it.hasNext())
		{
			String key = it.next();
			String originKey = key;
			
			int sequence = -1;
			
			Map<String, Object> breakdown = this.getKeyBreakdown(key);
			
			key = (String)breakdown.get("key");
			
			if (breakdown.get("sequence") != null)
			{
				sequence = ((Integer)breakdown.get("sequence")).intValue();
			}
			
			if (key.equalsIgnoreCase("percentageThreshold"))
			{
				this.percentageThreshold = Double.parseDouble(props.getProperty(key));
			}
			else if (key.equalsIgnoreCase("absoluteThreshold"))
			{
				this.absoluteThreshold = Double.parseDouble(props.getProperty(key));
			}
			else if (key.equalsIgnoreCase("leftDelimiter"))
			{
				this.leftDelimiter = props.getProperty(key);
			}
			else if (key.equalsIgnoreCase("rightDelimiter"))
			{
				this.rightDelimiter = props.getProperty(key);
			}
			else if (key.equalsIgnoreCase("leftEscape"))
			{
				String escapeProp = props.getProperty(key);
				
				if (escapeProp != null) this.leftEscape = escapeProp.charAt(0);
			}
			else if (key.equalsIgnoreCase("rightEscape"))
			{
				String escapeProp = props.getProperty(key);
				
				if (escapeProp != null) this.rightEscape = escapeProp.charAt(0);
			}
			else if (key.equalsIgnoreCase("defaultValueCompareMethod"))
			{
				int method = this.str2ValueCompareMethod(props.getProperty(key));
				
				if (method == -1)
				{
					throw new DiffException("CompareMethod " + props.getProperty(key) + " is not a valid value!");
				}
				
				this.defaultValueCompareMethod = method;
			}
			else if (key.equalsIgnoreCase("rowComparatorClass"))
			{
				try
				{
					this.rowComparator = (RowComparator) Class.forName(props.getProperty(key)).newInstance();
				}
				catch (Exception e)
				{
					throw new DiffException(e);
				}
			}
			else if (key.equalsIgnoreCase("leftKeyGeneratorClass"))
			{
				try
				{
					this.leftKeyGenerator = (KeyGenerator) Class.forName(props.getProperty(key)).newInstance();
				}
				catch (Exception e)
				{
					throw new DiffException(e);
				}
			}
			else if (key.equalsIgnoreCase("rightKeyGeneratorClass"))
			{
				try
				{
					this.rightKeyGenerator = (KeyGenerator) Class.forName(props.getProperty(key)).newInstance();
				}
				catch (Exception e)
				{
					throw new DiffException(e);
				}
			}
			else if (key.equalsIgnoreCase("leftFilterClass"))
			{
				try
				{
					this.leftFilter = (Filter) Class.forName(props.getProperty(key)).newInstance();
				}
				catch (Exception e)
				{
					throw new DiffException(e);
				}
			}
			else if (key.equalsIgnoreCase("rightFilterClass"))
			{
				try
				{
					this.rightFilter = (Filter) Class.forName(props.getProperty(key)).newInstance();
				}
				catch (Exception e)
				{
					throw new DiffException(e);
				}
			}
			else if (key.equalsIgnoreCase("primary.field.left"))
			{
				addField(this.leftPrimaryFields, sequence - 1, props.getProperty(originKey));
			}
			else if (key.equalsIgnoreCase("primary.field.right"))
			{
				addField(this.rightPrimaryFields, sequence - 1, props.getProperty(originKey));
			}
			else if (key.equalsIgnoreCase("compare.field.left"))
			{
				addPairProperty(this.comparisonPairList, sequence - 1, "left", props.getProperty(originKey));
			}
			else if (key.equalsIgnoreCase("compare.field.right"))
			{
				addPairProperty(this.comparisonPairList, sequence - 1, "right", props.getProperty(originKey));
			}
			else if (key.equalsIgnoreCase("compare.field.compareMethod"))
			{
				addPairProperty(this.comparisonPairList, sequence - 1, "compareMethod", props.getProperty(originKey));
			}
		}

		this.leftPrimaryFields = copyWithoutNull(this.leftPrimaryFields);
		this.rightPrimaryFields = copyWithoutNull(this.rightPrimaryFields);
		this.comparisonPairList = copyWithoutNull(this.comparisonPairList);
	}

	private <T> List<T> copyWithoutNull(List<T> src)
	{
		ArrayList<T> retList = new ArrayList<T>();
		
		for (int i = 0, size = src.size(); i < size; i++)
		{
			T obj = src.get(i);
			
			if (obj != null)
			{
				retList.add(obj);
			}
		}
		
		return retList;
	}
	
	private void addField(List<Field> list, int index, String name) throws DiffException
	{
		if (index < 0)
		{
			throw new DiffException("Lack of sequence number!");
		}
		
		fillNullElements(list, index + 1);
		
		Field fld = list.get(index);
		
		if (fld == null)
		{
			fld = new Field(name);
			list.set(index, fld);
		}
	}
	
	private void fillNullElements(List<?> list, int number)
	{
		if (number < list.size()) return;
		
		for (int i = list.size(); i < number; i++)
		{
			list.add(null);
		}
	}
	
	private void addPairProperty(List<ComparisonPair> list, int index, String name, String value) 
			throws DiffException
	{
		if (index < 0)
		{
			throw new DiffException("Lack of sequence number!");
		}
		
		fillNullElements(list, index + 1);
		
		ComparisonPair pair = list.get(index);
		
		if (pair == null)
		{
			pair = new ComparisonPair();
			pair.setCompareMethod(this.defaultValueCompareMethod);
			list.set(index, pair);
		}
		
		if (name.equalsIgnoreCase("left"))
		{
			pair.setLeftField(new Field(value));
		}
		else if (name.equalsIgnoreCase("right"))
		{
			pair.setRightField(new Field(value));
		}
		else if (name.equalsIgnoreCase("compareMethod"))
		{
			pair.setCompareMethod(this.str2ValueCompareMethod(value));
		}
		else
		{
			throw new DiffException("unknown comparison pair property " + name);
		}

	}
	
	/**
	 * Break a key into two parts: key and sequence. For example: compare.field.left.1 will be broken into 
	 * two parts: compare.field.left and 1
	 * @param key
	 * @return
	 */
	private HashMap<String, Object> getKeyBreakdown(String key)
	{
		HashMap<String, Object> retMap = new HashMap<String, Object>();

		retMap.put("key", key);
	
		int pos = key.lastIndexOf('.');
		
		if (pos < 0)
		{
			return retMap;
		}
		
		String lastToken = key.substring(pos + 1);
		
		int sequence = -1;
		
		try
		{
			sequence = Integer.parseInt(lastToken);
		}
		catch (NumberFormatException e)
		{
			sequence = -1;
		}
		
		if (sequence > 0)
		{
			retMap.put("key", key.substring(0, pos));
			retMap.put("sequence", new Integer(sequence));
		}
		
		return retMap;
	}
	
	public class ComparisonPair
	{
		private Field leftField;
		private Field rightField;
		private int compareMethod;
		
		public Field getLeftField() {
			return leftField;
		}
		public void setLeftField(Field leftField) {
			this.leftField = leftField;
		}
		public Field getRightField() {
			return rightField;
		}
		public void setRightField(Field rightField) {
			this.rightField = rightField;
		}
		public int getCompareMethod() {
			return compareMethod;
		}
		public void setCompareMethod(int compareMethod) {
			this.compareMethod = compareMethod;
		}
	}
	
	public List<ComparisonPair> getComparisonPairs()
	{
		return this.comparisonPairList;
	}
	
	public List<Field> getLeftPrimaryFields()
	{
		return this.leftPrimaryFields;
	}
	
	public List<Field> getRightPrimaryFields()
	{
		return this.rightPrimaryFields;
	}
	
	public class Field
	{
		public final static String FIELD_TYPE_String = "String";
		public final static String FIELD_TYPE_Double = "Double";
		
		public Field(String name) 
		{
			this.name = name;
			this.type = FIELD_TYPE_String;
		}
		
		public Field(String name, String type) {
			super();
			this.name = name;
			this.type = type;
		}
		private String name;
		private String type;
		
		
		public void setName(String name) {
			this.name = name;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getName() {
			return name;
		}
		public String getType() {
			return type;
		}
	}
}
