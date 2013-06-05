package org.f3tool.tdiff;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Dennis Kang 
 *
 */
class TableDiff 
{
	private TxtTable.Row preLeftRow;
	private TxtTable.Row preRightRow;
	private ComparisonConfig config;
	private RowComparator rowComparator;
	
	public static void main(String[] argc)
	{
		try
		{
			if (argc.length < 4)
			{
				usage();
				System.exit(-1);
			}

			long start = System.currentTimeMillis();
			
			if (argc[0].equalsIgnoreCase("-c"))
			{
				if (argc.length < 5) 
				{
					usage();
					System.exit(-1);
				}
				
				TableDiff diff = new TableDiff(argc[1]);
				diff.diff(null, null, null, null, argc[2], argc[3], argc[4]);
			}
			else if (argc[0].equalsIgnoreCase("-s"))
			{
				if (argc.length < 9) 
				{
					usage();
					System.exit(-1);
				}
				
				String configFile = argc[1];
				String inputFile = argc[2];
				String outputFile = argc[3];
				String tmpdirectory = argc[4];

				long numberLinePerFile = Integer.parseInt(argc[5]);
				int maxNumberOfFile = Integer.parseInt(argc[6]);
				
				boolean saveKey = false;
				boolean keepTmp = false;
				
				if (argc[7].equalsIgnoreCase("Y")) saveKey = true;
				if (argc[8].equalsIgnoreCase("Y")) keepTmp = true;
				
				TableSort sorter = new TableSort(configFile);
				ComparisonConfig config = ComparisonConfig.getInstance();
				
				sorter.sort(inputFile, outputFile, tmpdirectory, numberLinePerFile, maxNumberOfFile, saveKey, 
						keepTmp, config.getLeftDelimiter(), config.getLeftEscape(), config.getLeftKeyGenerator(), config.getLeftFilter());
			}
			else if (argc[0].equalsIgnoreCase("-sc"))
			{
				if (argc.length < 9) 
				{
					usage();
					System.exit(-1);
				}
				
				String configFile = argc[1];
				String inputFile1 = argc[2];
				String inputFile2 = argc[3];
				String outputFile = argc[4];
				String tmpdirectory = argc[5];

				long numberLinePerFile = Integer.parseInt(argc[6]);
				int maxNumberOfFile = Integer.parseInt(argc[7]);

				boolean keepTmp = false;
				
				if (argc[8].equalsIgnoreCase("Y")) keepTmp = true;
				
				TableSort sorter = new TableSort(configFile);
				ComparisonConfig config = ComparisonConfig.getInstance();

				File tmpfile1 = File.createTempFile(removeFileExtention(new File(inputFile1).getName()) + "_tmp", "flatfile", new File(tmpdirectory));
				if (!keepTmp) tmpfile1.deleteOnExit();
				
				sorter.sort(inputFile1, tmpfile1.getAbsolutePath(), tmpdirectory, numberLinePerFile, 
						maxNumberOfFile, true, keepTmp, config.getLeftDelimiter(), 
						config.getLeftEscape(), config.getLeftKeyGenerator(), config.getLeftFilter());
				
				File tmpfile2 = File.createTempFile(removeFileExtention(new File(inputFile2).getName()) + "_tmp", "flatfile", new File(tmpdirectory));
				if (!keepTmp) tmpfile2.deleteOnExit();

				sorter.sort(inputFile2, tmpfile2.getAbsolutePath(), tmpdirectory, numberLinePerFile, 
						maxNumberOfFile, true, keepTmp, config.getRightDelimiter(), 
						config.getRightEscape(), config.getRightKeyGenerator(), config.getRightFilter());

				TableDiff diff = new TableDiff(argc[1]);
				diff.diff(new FirstRowKeyGenerator(), new FirstRowKeyGenerator(), null, null, tmpfile1.getAbsolutePath(), 
						tmpfile2.getAbsolutePath(), outputFile);
			}
			else
			{
				usage();
			}

			System.out.println("Total milli seconds: " + String.valueOf(System.currentTimeMillis() - start));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static String removeFileExtention(String fileName)
	{
		int pos = fileName.indexOf('.');
		
		if (pos < 0) 
			return fileName;
		else
			return fileName.substring(0, pos);
	}
	
	private static void usage()
	{
		System.out.println("Usage: TableDiff [-c | -s | -sc] options...");
		System.out.println("-c: compare two sorted files. options: configFile file1 file2 file3, compare file1 with file2, comparison reults is in file3");
		System.out.println("-s: sort input file. options: configFile inputFile outputFile tmpdirectory numberLinePerFile maxNumberOfFile saveKey keepTmp");
		System.out.println("    sort inputFile into outputFile, saveKey: Y=Save the key in output file, keepTmp: Y=keep temp files");
		System.out.println("-sc: sort input files then compare them. options: configFile inputFile1 inputFile2 resultfile tmpdirectory numberLinePerFile maxNumberOfFile keepTmp");
	}
	
	public TableDiff(String configFile) throws DiffException
	{
		config = ComparisonConfig.getInstance();
		config.init(configFile);
		rowComparator = config.getRowComparator();
	}
	
	public void diff(KeyGenerator leftKeyGenerator, KeyGenerator rightKeyGenerator, Filter leftFilter, 
			Filter rightFilter, String leftFile, String rightFile, String resultFile) throws IOException
	{
		if (leftKeyGenerator == null) leftKeyGenerator = config.getLeftKeyGenerator();
		if (rightKeyGenerator == null) rightKeyGenerator = config.getRightKeyGenerator();
		
		TxtTable leftT = new TxtTable(leftFile, leftKeyGenerator, leftFilter, 
				false, config.getLeftDelimiter(), config.getLeftEscape());
		leftT.setPrimaryFields(config.getLeftPrimaryFields());
		
		TxtTable rightT = new TxtTable(rightFile, rightKeyGenerator, rightFilter,
				false, config.getRightDelimiter(), config.getRightEscape());
		rightT.setPrimaryFields(config.getRightPrimaryFields());
		
		DiffResults result = new DiffResults(resultFile);
	
		TxtTable.Row leftRow = leftT.getNextRow();
		TxtTable.Row rightRow = rightT.getNextRow();
		
		while(leftRow != null && rightRow != null)
		{
			leftRow = handleDuplicates(result, leftT, preLeftRow, leftRow, false);
			rightRow = handleDuplicates(result, rightT, preRightRow, rightRow, true);
			
			@SuppressWarnings("unchecked")
			int compare = leftRow.getKey().compareTo(rightRow.getKey());
			
			if (compare < 0)
			{
				// right side is missing
				result.addRightMiss(leftRow);
				preLeftRow = leftRow;
				leftRow = leftT.getNextRow();
				continue;
			}
			else if (compare > 0)
			{
				// left side is missing
				result.addLeftMiss(rightRow);
				preRightRow = rightRow;
				rightRow = rightT.getNextRow();
				continue;
			}

			// key matched, compare content
			
			String msg = null;
			
			try
			{
				msg = rowComparator.compare(leftT, leftRow, rightT, rightRow);
			
				if (msg != null)
				{
					result.addMessage(msg);
					result.addMismatch(leftRow, rightRow);
				}
			}
			catch (Exception e)
			{
				result.addMessage(e.toString());
				result.addMismatch(leftRow, rightRow);
			}
			
			preLeftRow = leftRow;
			preRightRow = rightRow;
			
			leftRow = leftT.getNextRow();
			rightRow = rightT.getNextRow();
		}
		
		checkMissing(result, leftT, leftRow, false);
		checkMissing(result, rightT, rightRow, true);
		
		leftT.closeFile();
		rightT.closeFile();
		
		result.addSummary();
		result.close();
	}
	
	/**
	 * If the current row is not empty, loop through the remaining which are missing in the other side
	 * @param result
	 * @param tbl
	 * @param curRow
	 * @param isRightSide
	 * @throws IOException
	 */
	private void checkMissing(DiffResults result, TxtTable tbl, TxtTable.Row curRow, boolean isRightSide) throws IOException
	{
		while (curRow != null)
		{
			if (isRightSide) 
				result.addLeftMiss(curRow);
			else
				result.addRightMiss(curRow);
			
			curRow = tbl.getNextRow();
		}
	}
	
	private TxtTable.Row handleDuplicates(DiffResults result, TxtTable tbl, TxtTable.Row preRow, 
			TxtTable.Row curRow, boolean isRightTable) throws IOException
	{
		if (preRow == null) return curRow;
		
		while (curRow != null && preRow.getKey().equals(curRow.getKey()))
		{
			if (isRightTable)
				result.addRightDuplicates(preRow, curRow);
			else
				result.addLeftDuplicates(preRow, curRow);
			
			curRow = tbl.getNextRow();
		}
		
		return curRow;
	}
}
