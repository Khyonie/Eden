package coffee.khyonieheart.eden.utils.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coffee.khyonieheart.eden.module.annotation.Unimplemented;

@Unimplemented("Class is not ready for use yet.")
public class LookupList<T> 
{
	List<T> data = new ArrayList<>();

	public LookupList() { }

	@SafeVarargs
	public LookupList(T... data)
	{
		this.data = Arrays.asList(data);
	}
}
