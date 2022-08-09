package fish.yukiemeralis.eden.utils.result;

public interface Result 
{
    public <T> T unwrap(Class<? extends T> clazz);
 
    public <A, B> B unwrapOrElse(Class<? extends A> classA, Class<? extends B> classB, ResultHandler<A, B> func);

    public static <T> Ok ok(T ok)
    {
        return new Ok(ok);
    }

    public static interface ResultHandler<A, B>
    {
        public B unwrap(A in);
    }
}
