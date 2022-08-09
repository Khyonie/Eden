package fish.yukiemeralis.eden.utils.result;

public class Ok implements Result 
{
    private Object contained;

    public Ok(Object contained)
    {
        this.contained = contained;
    }

    @Override
    public <T> T unwrap(Class<? extends T> clazz) 
    {
        return clazz.cast(this.contained);
    }

    @Override
    public <A, B> B unwrapOrElse(Class<? extends A> classA, Class<? extends B> classB, ResultHandler<A, B> func) 
    {
        return func.unwrap(classA.cast(contained));
    }
}
