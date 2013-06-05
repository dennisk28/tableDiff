package org.f3tool.tdiff;

public class DiffException extends Exception
{
	private static final long serialVersionUID = -8324520203905616377L;

	public DiffException(Exception e)
	{
		super(e);
	}
	
	public DiffException(String message)
    {
        super(message);
    }
}
