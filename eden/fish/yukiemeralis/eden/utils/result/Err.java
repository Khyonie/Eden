package fish.yukiemeralis.eden.utils.result;

public class Err implements Result 
{
    private Object contained;
    public Err(Object contained)
    {
        this.contained = contained;
    }

    @Override
    public <T> T unwrap(Class<? extends T> clazz) 
    {
        return clazz.cast(contained);
    }

    @Override
    public <A, B> B unwrapOrElse(Class<? extends A> classA, Class<? extends B> classB, ResultHandler<A, B> func) 
    {
        throw new UnsupportedOperationException("Attempted to call unwrapOrElse on an ERR value. ERRs cannot support this functionality");
    }
    
}
