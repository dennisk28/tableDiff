package org.f3tool.tdiff;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class DiffResults
{
	private int mismatchedCount;
	private int leftMissCount;
	private int rightMissCount;
	private BufferedWriter writer;
	private int leftDuplicateCount;
	private int rightDuplicateCount;
	
	public int getMismatchedCount()
	{
		return mismatchedCount;
	}
	
	public DiffResults(String resultsFile) throws IOException
	{
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsFile)));
	}
	
	public void addMismatch(TxtTable.Row leftRow, TxtTable.Row rightRow) throws IOException
	{
		mismatchedCount++;
		addMessage("<<(" + leftRow.getLineNo() + ")" + leftRow.getContent());
		addMessage(">>(" + rightRow.getLineNo() + ")" + rightRow.getContent());
	}
	
	public void addLeftMiss(TxtTable.Row rightRow) throws IOException
	{
		leftMissCount++;
		addMessage("<<Left Missing(" + rightRow.getLineNo() + "):" + rightRow.getContent());
	}

	public void addRightMiss(TxtTable.Row leftRow) throws IOException
	{
		rightMissCount++;
		addMessage(">>Right Missing(" + leftRow.getLineNo() + "):" + leftRow.getContent());
	}	
	
	public void addLeftDuplicates(TxtTable.Row preRow, TxtTable.Row curRow) throws IOException
	{
		leftDuplicateCount++;
		addMessage("<<(" + preRow.getLineNo() + ") duplicates with (" + curRow.getLineNo() + ")");
	}

	public void addRightDuplicates(TxtTable.Row preRow, TxtTable.Row curRow) throws IOException
	{
		rightDuplicateCount++;
		addMessage(">>(" + preRow.getLineNo() + ") duplicates with (" + curRow.getLineNo() + ")");
	}
	
	public void addMessage(String message) throws IOException
	{
		writer.write(message);
		writer.write("\n");
	}
	
	public void addSummary() throws IOException
	{
		addMessage("Total mismatched: " + mismatchedCount);
		addMessage("Total missing rows in left side: " + leftMissCount);
		addMessage("Total missing rows in right side: " + rightMissCount);
		addMessage("Total duplicated rows in left side:" + leftDuplicateCount);
		addMessage("Total duplicated rows in right side:" + rightDuplicateCount);
	}

	public boolean isMatched()
	{
		if (mismatchedCount > 0 || leftMissCount > 0 || rightMissCount > 0) 
			return false;
		else
			return true;
	}
	
	public void close() throws IOException
	{
		writer.flush();
		writer.close();
		writer = null;
	}
}
