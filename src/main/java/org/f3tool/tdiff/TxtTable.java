package org.f3tool.tdiff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxtTable 
{
	private boolean noTitle;
	public static int BUFFER_SIZE = 2048;
	private BufferedReader reader;
	private KeyGenerator keyGenerator;
	private long lineNo;
	private File file;
	
    public static final String DEFAULT_DELIMITER = "\t";
	
    protected List<String> columnNames;
    protected Map<String, Integer> columnMap;
    private String delimiter;
    private char escape;
    private List<ComparisonConfig.Field> primaryFields;
    private Row titleRow;
    private Filter filter;
	
	/**
	 * 
	 * @param fileName
	 * @param fullLoad if it's true, the table will be loaded into memory.
	 * @throws IOException
	 */
	public TxtTable(String fileName, KeyGenerator keyGenerator, Filter filter, boolean noTitle, String delimeter, char escape) throws IOException
	{
		this.noTitle = noTitle;
		
		if (delimeter == null)
			this.delimiter = DEFAULT_DELIMITER;
		else
			this.delimiter = delimeter;
		
		this.escape = escape;
		this.filter = filter;
		
		if (keyGenerator == null)
		{
			this.keyGenerator = new DefaultKeyGenerator();
		}

		else
		{
			this.keyGenerator = keyGenerator;
		}
		
		init(fileName);
	}
	
	public String getDelimiter() {
		return delimiter;
	}

	public char getEscape() {
		return escape;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public List<ComparisonConfig.Field> getPrimaryFields() {
		return primaryFields;
	}

	public void setPrimaryFields(List<ComparisonConfig.Field> primaryFields) {
		this.primaryFields = primaryFields;
	}

	public boolean hasField(String fieldName)
	{
		if (this.columnMap.get(fieldName) != null) 
			return true;
		else
			return false;
	}
	
	public File getFile()
	{
		return this.file;
	}
	
	public void closeFile() throws IOException
	{
		if (this.reader != null)
			this.reader.close();
	}
	
	private void init(String fileName) throws IOException
	{
		this.file = new File(fileName);
		
		this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)), BUFFER_SIZE);
		
		if (!noTitle)
		{
			String title = reader.readLine();
			lineNo++;
			
			if (title != null)
			{
				// build columns
				Row titleRow = new Row(this, "TITLE", title, 0);
				
				this.titleRow = titleRow;
				
				this.columnNames = titleRow.getElements();
				this.columnMap = new HashMap<String, Integer>();
				
				for(int i = 0, size = this.columnNames.size(); i < size; i++)
				{
					this.columnMap.put(this.columnNames.get(i), new Integer(i));
				}
			}
		}
	}
	
	
	public Row getTitleRow() {
		return titleRow;
	}

	public Row getNextRow() throws IOException
	{
		boolean filteredout = true;
		Row row = null;
		
		while (filteredout)
		{
			String line = reader.readLine();
			lineNo++;
			
			if (line == null)
			{
				return null;
			}
			
			row = new Row(this, null, line, lineNo);

			if (filter != null) 
				filteredout = filter.filter(this, row);
			else
				filteredout = false;
		}
		
		row.setKey(this.keyGenerator.generateKey(this, row));
		
		return row;
	}
	
	private int getFieldIndex(String fieldName)
	{
		Integer idx = this.columnMap.get(fieldName);
		
		if (idx == null)
			return -1;
		else
			return idx.intValue();
	}
	
	public String getFieldValue(Row row, String fieldName)
	{
		int idx = this.getFieldIndex(fieldName);
		if (idx < 0)
			return null;
		else
			return row.getFieldValue(idx);
	}

	public String getFieldValueNullStr(Row row, String fieldName)
	{
		String v = getFieldValue(row, fieldName);
		
		if (v == null) 
			return "";
		else
			return v;
	}
	
	private TxtTable.Row cache;
	private boolean empty;
	
	public boolean empty() {
		return this.empty;
	}

	public void reload() throws IOException {
		if ((this.cache = this.getNextRow()) == null) {
			this.empty = true;
			this.cache = null;
		} else {
			this.empty = false;
		}
	}

	public Row peek() {
		if (empty())
			return null;
		return this.cache;
	}

	public Row pop() throws IOException {
		Row answer = peek();
		reload();
		return answer;
	}	
	
	@SuppressWarnings("unchecked")
	public class Row implements Comparable
	{
		private Comparable key;
		private String content;
		private long lineNo;
		private List<String> elements;
		private TxtTable table;
		
		public TxtTable getTable() {
			return table;
		}

		public void setTable(TxtTable table) {
			this.table = table;
		}

		public String getFieldValue(int idx)
		{
			if (idx >= this.elements.size()) 
				return null;
			else
				return elements.get(idx);
		}
		
		public List<String> getElements() {
			return elements;
		}

		public Row(TxtTable table, Comparable key, String content, long lineNo)
		{
			this.key = key;
			this.content = content;
			this.elements = createElements(content, table.delimiter, table.escape);
			this.lineNo = lineNo;
			this.table = table;
		}
		
		public Comparable getKey() {
			return key;
		}
		public void setKey(Comparable key) {
			this.key = key;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		
		public long getLineNo()
		{
			return this.lineNo;
		}
		
	    /**
	     * This method returns a ArrayList which contains tokens from the input line.
	     * Empty element which occurs when there is nothing between two delimiters
	     * will be added in the row ArrayList as null.
	     *
	     * Since java.util.StringTokenizer doesn't work well for empty element,
	     * we need write our own tokenizer
	     *
	     * @param aLine
	     * @param delimiters
	     * @param escape if escape is not 0, escape is supported. for example, if delimiter is ",", "\," will be treated as ","
	     * @return
	     */
	    public List<String> createElements(String aLine, String delimiter, char escape)
	    {
	        int nPos = 0;
	        int nLen = aLine.length();
	        char c;
	        char[] eBuf = new char[nLen];
	        int ePos = 0;
	        char dl = delimiter.charAt(0);
	        
	        ArrayList<String> vRow = new ArrayList<String>();

	        while(nPos < nLen)
	        {
	            c = aLine.charAt(nPos);

	       //     if(delimiter.indexOf(c) >= 0)
	            if(dl == c)
	            {
	                if(ePos == 0)
	                {
	                    vRow.add(null);
	                }
	                else
	                {
	                	char preC = aLine.charAt(nPos - 1);
	                	
	                	if (preC == escape && escape != 0 )
	                	{
	                		eBuf[ePos - 1] = c;
	                	}
	                	else
	                	{
	                		vRow.add(new String(eBuf, 0, ePos));
	                		ePos = 0;
	                	}
	                }
	            }
	            else
	            	eBuf[ePos++] = c;

	            nPos++;
	        }
	        
	        // currently nPos == nLen, end of the line

	        if(ePos != 0)
	        {
	            vRow.add(new String(eBuf, 0, ePos));
	        }
	        else
	        {
	            // the last character is a delimiter
	            vRow.add(null);
	        }
	        return vRow;
	    }

/*	    public List<String> createElements(String aLine, String delimiters, char escape)
	    {
	        int nPos = 0;
	        int nStartPos = 0;
	        int nLen = aLine.length();
	        char c;

	        ArrayList<String> vRow = new ArrayList<String>();

	        while(nPos < nLen)
	        {
	            c = aLine.charAt(nPos);

	            if(delimiters.indexOf(c) >= 0)
	            {
	                if(nStartPos == nPos)
	                {
	                    vRow.add(null);
	                }
	                else
	                {
	                    vRow.add(aLine.substring(nStartPos, nPos));

	                }
	                nStartPos = nPos + 1;
	            }

	            nPos++;
	        }

	        // currently nPos == nLen, end of the line

	        if(nStartPos < nPos)
	        {
	            vRow.add(aLine.substring(nStartPos, nPos));
	        }
	        else
	        {
	            // the last character is a delimiter
	            vRow.add(null);
	        }
	        return vRow;
	    }
*/	    
	    public String getDelimiter()
	    {
	    	return this.table.getDelimiter();
	    }

	    public int compareTo(Object to)
		{
			return this.getKey().compareTo(((Row)to).getKey());
		}
	}
}
