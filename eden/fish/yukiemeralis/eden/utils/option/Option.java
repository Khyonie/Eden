package fish.yukiemeralis.eden.utils.option;

/**
 * Represents the result of an operation in which said operation may fail or produce an unexpected result.
 * Used as an alternative to "throws" or outright returning null, forcing handling of an operation failure,
 * instead of potentially having a NullPointerException.<p>
 * 
 * Analagous to Rust lang's {@code Option<T>} class:<p>
 * https://doc.rust-lang.org/std/option/enum.Option.html#<p>
 * 
 * To use an Option, call either static methods <code>Option.some(Object contained)</code> or
 * <code>Option.none()</code>.<p>
 * 
 * To obtain the contained value of a SOME, call {@code .unwrap(Class<? extends T> clazz)} where T is the class to unwrap into. 
 * In the case of NONE, unwrapping will throw an {@link UnwrapException}.<p>
 * 
 * As of Java 17 preview, instanceof switching is supported.
 * <pre><code>
 *Object contained = switch(option)
 *{
 *   case Some some -> some.unwrap(Option.class);
 *   case None none -> null;
 *}; 
 * </code></pre>
 */
public interface Option
{
    /**
     * Obtains the contained value.
     * @param <T> Type of contained value
     * @param clazz Class of contained value
     * @return Contained value of T
     * @throws UnwrapException When the option is a state of NONE
     */
    public <T> T unwrap(Class<? extends T> clazz);

    /**
     * Obtains the contained value, or if the state is NONE, returns the default provided.
     * @param <T> Type of contained value
     * @param clazz Class of contained value
     * @param orDefault Value to return if state is NONE
     * @return Contained value of T or provided default
     */
    public <T> T unwrapOr(Class<? extends T> clazz, T orDefault);

    /**
     * Obtains the contained value, or if the state is NONE, computes it from the supplied interface.
     * @param <T> Type of contained value
     * @param clazz Class of contained value
     * @param computer Function to compute new value
     * @return Contained or computed value
     */
    public <T> T unwrapOrElse(Class<? extends T> clazz, OptionComputer<T> computer);

    /**
     * Obtains the contained value in the case of SOME, then maps it to a new type "T" using the supplied mapping function.
     * @param <O> Output type of contained after mapping
     * @param mappedclazz Output class of contained after mapping
     * @param mapper Function to map output value
     * @return Contained value mapped to type "O"
     * @throws UnwrapException When the option is a state of NONE
     */
    public <T> T map(Class<? extends T> mappedclazz, OptionMapper<T> mapper);

    /**
     * Obtains the contained value in the case of SOME, then maps it to a new type "T" using the supplied mapping function.<p>
     * If the state is NONE, returns the default provided.
     * @param <O> Output type of contained after mapping
     * @param mappedclazz Output class of contained after mapping
     * @param mapDefault Value to return if state is NONE
     * @param mapper Function to map output value
     * @return Contained value mapped to type "O" or provided default
     */
    public <T> T mapOr(Class<? extends T> mappedclazz, T mapDefault, OptionMapper<T> mapper);

    public OptionState getState();
    public boolean isSome();
    public boolean isNone();

    public static Some some(Object contained)
    {
        return new Some(contained);
    }

    public static None none()
    {
        return new None();
    }

    public static interface OptionComputer<T>
    {
        public T compute();
    }

    public static interface OptionMapper<T>
    {
        public T map(Object in);
    }
}
