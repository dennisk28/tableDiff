package org.f3tool.tdiff;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Use external merge sort algorithm to sort large table
 * @author Dennis Kang, @email denniskds@yahoo.com
 *
 */
public class TableSort
{
	private ComparisonConfig config;
	private static long MAX_FILE_NUMBER = 1024;
	
	public TableSort(String configFile) throws DiffException
	{
		this();
		config.init(configFile);
	}

	public TableSort() throws DiffException
	{
		config = ComparisonConfig.getInstance();
	}

	public long sort(String inputFile, String outputFile, String tmpdirectory, long numberLinePerFile, 
			int maxNumberOfFile, boolean saveKey, boolean keepTmp, String delimiter, 
			char escape, KeyGenerator keyGenerator, Filter filter) throws DiffException
	{
		if (maxNumberOfFile > MAX_FILE_NUMBER)
		{
			throw new DiffException("Maxium number of files can't exceed " + MAX_FILE_NUMBER);
		}
		
		try
		{
			List<File> tmpFileList = new ArrayList<File>();
			
			// first step: read input table, split into sorted temp tables
			
			TxtTable table = new TxtTable(inputFile, keyGenerator, filter, false, delimiter, escape);
			long curSplitRowNo = 0;
			int fileCount = 0;
			
			table.setPrimaryFields(config.getLeftPrimaryFields());
			
			String tableTile = table.getTitleRow().getContent();
			TxtTable.Row row = table.getNextRow();
			
			MemTable tmpTable = new MemTable();
			
			while (row != null && fileCount < maxNumberOfFile)
			{
				tmpTable.addRow(row);
				curSplitRowNo++;
				
				row = table.getNextRow();
				
				if (curSplitRowNo >= numberLinePerFile || row == null)
				{
					File newtmpfile = File.createTempFile("sort_tmp", "flatfile", new File(tmpdirectory));
					
					if (!keepTmp) newtmpfile.deleteOnExit();
					
					tmpFileList.add(newtmpfile);

					tmpTable.sortAndSave(newtmpfile);
					fileCount++;
					
					if (row != null)
					{
						tmpTable = new MemTable();
						curSplitRowNo = 0;
					}
				}
			}
			
			if (fileCount == maxNumberOfFile)
			{
				throw new DiffException("File count exceeds maximum allowed count.");
			}
			
			table.closeFile();
			
			// second step: merge sort
			
			return mergeSortedFiles(tmpFileList, new File(outputFile), tableTile,
					new Comparator<TxtTable.Row>() {
						public int compare(TxtTable.Row i, TxtTable.Row j) { return i.compareTo(j);	}
					},
					delimiter, escape, saveKey);
		}
		catch (IOException e)
		{
			throw new DiffException(e);
		}
	}
	
	public long mergeSortedFiles(List<File> files, File outputfile,
			String tableTitle, final Comparator<TxtTable.Row> cmp,
			String delimiter, char escape, boolean saveKey) throws IOException
	{
		PriorityQueue<TxtTable> pq = new PriorityQueue<TxtTable>(11,
				new Comparator<TxtTable>() {
					public int compare(TxtTable i, TxtTable j) {
						return cmp.compare(i.peek(), j.peek());
					}
				});
		
		if (delimiter == null) delimiter = TxtTable.DEFAULT_DELIMITER;
		
		for (File f : files)
		{
			TxtTable table = new TxtTable(f.getAbsolutePath(),
					new FirstRowKeyGenerator(), null, true, delimiter, escape);

			table.reload();
			pq.add(table);
		}

		BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile)));

		if (saveKey) fbw.write("ID_ID" + delimiter);

		fbw.write(tableTitle);
		fbw.newLine();

		int rowcounter = 0;

		try 
		{
			while (pq.size() > 0) 
			{
				TxtTable table = pq.poll();
				TxtTable.Row r = table.pop();

				if (saveKey)
					fbw.write(r.getContent());
				else
					fbw.write(removeFirstElement(r.getContent(), delimiter));
				
				fbw.newLine();

				++rowcounter;

				if (table.empty()) 
				{
					table.closeFile();
					table.getFile().delete();
				} else 
				{
					pq.add(table); // add it back
				}
			}
		} 
		finally
		{
			fbw.close();
			for (TxtTable table : pq)
				table.closeFile();
		}

		return rowcounter;
	}
	
	/**
	 * This utility function removes the first column of a row
	 * @param content
	 * @return
	 */
	private String removeFirstElement(String content, String delimiter)
	{
		int pos = content.indexOf(delimiter);
		
		if (pos < 0) 
			return content;
		return
			content.substring(pos);
	}
	
	
	class MemTable
	{
		private List<TxtTable.Row> rows;
		private String title;
		
		public String getTitle() 
		{
			return title;
		}

		public void setTitle(String title) 
		{
			this.title = title;
		}

		public MemTable()
		{
			this.rows = new ArrayList<TxtTable.Row>(2048);
		}
		
		public void addRow(TxtTable.Row row)
		{
			this.rows.add(row);
		}
		
		public void sortAndSave(File file)  throws IOException
		{
			if (rows.size() == 0) return;

			Collections.sort(this.rows, 
					new Comparator<TxtTable.Row>() {
						public int compare(TxtTable.Row i, TxtTable.Row j) { return i.compareTo(j);	}
					}
			);
			
			BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			
			if (title != null)
			{
				fbw.write(title);
				fbw.newLine();
			}
			
			String delimiter = rows.get(0).getDelimiter(); 
			
			// add key to the first column
			for (TxtTable.Row row : rows)
			{
				fbw.write(row.getKey().toString());
				fbw.write(delimiter);
				fbw.write(row.getContent());
				fbw.newLine();
			}
			
			fbw.flush();
			fbw.close();
		}
	}
}
