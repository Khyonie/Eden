package com.yukiemeralis.blogspot.zenith.utils;

/**
 * Represents the result of an operation where the result is more complicated than value/null, or value/exception.
 * A value of {@code null} for either T or E will throw an UndefinedResultException.</p>
 * 
 * Analagous to Rust lang's {@code Result<T, E>} class:
 * https://doc.rust-lang.org/std/result/</p>
 *
 * To use a result and obtain its contained value, create a new instance of Result with types T and E. Fill in both "Ok"
 * and "Err" values, and return the result as a whole. Create a switch block for {@link Result#getState()}, and write cases for both
 * {@Link ResultState#OK} and {@link ResultState#ERR}. To obtain the contained value, invoke {@link Result#unwrap()}, which will
 * safely obtain the Ok or Err value. Only one value may be defined at a time.
 * @param <T> Type of Ok
 * @param <E> Type of Err
 */
public class Result<T, E>
{
    private T t;
    private E e;

    private Class<? extends T> tClass;
    private Class<? extends E> eClass;

    private ResultState state = null; 

    /**
     * Represents the state of a {@code Result<T, E>} object.
     */
    @SuppressWarnings("javadoc")
    public static enum ResultState
    {
        OK,
        ERR
        ;
    }

    /**
     * A result.
     * @param tClass
     * @param eClass
     */
    public Result(Class<? extends T> tClass, Class<? extends E> eClass)
    {
        this.tClass = tClass;
        this.eClass = eClass;
    }

    /**
     * Sets the "Ok" value
     * @param t
     * @throws UndefinedResultException If T is null or is invalid.
     */
    public void ok(T t) throws UndefinedResultException
    {
        if (t == null)
            throw new UndefinedResultException(this);

        if (!tClass.isAssignableFrom(t.getClass()))
            throw new ClassCastException();        
        
        state = ResultState.OK;
        this.t = t;
    }

    /**
     * Sets the "Err" value
     * @param e
     * @throws UndefinedResultException If E is null or invalid.
     */
    public void err(E e) throws UndefinedResultException
    {
        if (e == null)
            throw new UndefinedResultException(this);

        if (!eClass.isAssignableFrom(e.getClass()))
            throw new ClassCastException();

        state = ResultState.ERR;
        this.e = e;
    }

    /**
     * Unwraps the Ok or Err value. Switch on state and cast to the expected type.
     * @return The value contained in this result.
     * @throws UndefinedResultException If the contained result is null.
     */
    public Object unwrap() throws UndefinedResultException
    {
        if (this.state == null) // Invalid
            throw new UndefinedResultException(this);
        if (this.t != null && this.state.equals(ResultState.OK)) // Valid
            return this.t;
        if (this.e != null && this.state.equals(ResultState.ERR)) // Valid
            return this.e;
        throw new UndefinedResultException(this);
    }

    /**
     * Obtains the state of this result.
     * @return This result's state.
     */
    public ResultState getState()
    {
        return this.state;
    }

    /**
     * Obtains the class of "Ok"
     * @return "Ok" class
     */
    public Class<? extends T> getOkClass()
    {
        return this.tClass;
    }

    /**
     * Obtains the class of "Err"
     * @return "Err" class
     */
    public Class<? extends E> getErrClass()
    {
        return this.eClass;
    }

    /**
     * Exception for when a result's contained value is null.
     * @author Yuki_emeralis
     */
    public static class UndefinedResultException extends RuntimeException
    {
		private static final long serialVersionUID = 4918662729371931947L;

		/** @param result */
		public UndefinedResultException(Result<?, ?> result)
        {
            System.out.println("Object Result<" + result.getOkClass().getName() + ", " + result.getErrClass().getName() +  "> cannot have its contained Ok or Err value be null.");
        }
    }
}
