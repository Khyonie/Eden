package coffee.khyonieheart.eden.utils.result;

/**
 * Represents the result of an operation where the output is more complicated than output/exception or output/null.<p>
 * 
 * Analagous to Rust lang's {@code Result<T, E>} class:<p>
 * https://doc.rust-lang.org/std/result/enum.Result.html<p>
 * 
 * To use a result, call either static method <code>Result.ok(Object contained)</code> or <code>Result.err(Object error)</code>.<p>
 * 
 * To obtain the contained value, call {@code .unwrapOk(Class<? extends T> clazz)} or {@code .unwrapErr(Class<? extends T> clazz)} where T is the class to unwrap the result into.
 * If the result is in a state of Err, unwrapping into an Ok will throw an UnwrapException, and likewise unwrapping a result of Ok into an Err
 * will also throw an UnwrapException.
 */
public interface Result 
{
    /**
     * Obtains the contained Ok value.
     * @param <T> Type of contained Ok value
     * @param clazz Class of contained Ok value
     * @return Contained value of Ok
     * @throws UnwrapException When this result is a state of Err
     */
    public <T> T unwrapOk(Class<? extends T> clazz);

    /**
     * Obtains the contained Err value.
     * @param <T> Type of contained Err value
     * @param clazz Class of contained Err value
     * @return Contained value of Err
     * @throws UnwrapException When this result is a state of Ok
     */
    public <T> T unwrapErr(Class<? extends T> clazz);

    public boolean isOk();
    public boolean isErr();
    public ResultState getState();

    public static Ok ok(Object contained)
    {
        return new Ok(contained);
    }

    public static Err err(Object contained)
    {
        return new Err(contained);
    }
}