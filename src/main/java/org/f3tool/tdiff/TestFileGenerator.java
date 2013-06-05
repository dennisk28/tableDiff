package org.f3tool.tdiff;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestFileGenerator 
{
	public static void main(String[] argc)
	{
		try
		{
			BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(argc[0])));
			
			fbw.write("id1\tid2\tfield1\tfield2\tfield3\tfield4\tfield5\tfield6\n");

			Random r = new Random();
			r.setSeed(System.currentTimeMillis());
			
			
			for (int i = 0; i < 1000000; i++)
			{
				fbw.write(String.valueOf(Math.abs(r.nextInt())));
				fbw.write("\t");
				fbw.write(String.valueOf(Math.abs(r.nextInt())));
				fbw.write("\t");
				fbw.write(String.valueOf(Math.abs(r.nextInt())));
				fbw.write("\t");
				fbw.write(String.valueOf(Math.abs(r.nextInt())));
				fbw.write("\t");
				fbw.write(String.valueOf(Math.abs(r.nextInt())));
				fbw.write("\t");
				fbw.write(String.valueOf(Math.abs(r.nextInt())));
				fbw.write("\t");
				fbw.write(String.valueOf(Math.abs(r.nextInt())));
				fbw.write("\t");
				fbw.write(String.valueOf(Math.abs(r.nextInt())));
				fbw.write("\n");
			}
			
			fbw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}
