package coffee.khyonieheart.eden.utils.option;

public class None implements Option 
{
    public Object unwrap()
    {
        return unwrap(null);
    }

    @Override
    public <T> T unwrap(Class<? extends T> clazz) 
    {
        throw new UnsupportedOperationException("Attempted to call unwrap() on a NONE value");
    }

    @Override
    public <T> T unwrapOr(Class<? extends T> clazz, T orDefault) 
    {
        return orDefault;
    }

    @Override
    public <T> T unwrapOrElse(Class<? extends T> clazz, OptionComputer<T> computer) 
    {
        return computer.compute();
    }

    @Override
    public <T> T map(Class<? extends T> mappedclazz, OptionMapper<T> mapper) 
    {
        throw new UnsupportedOperationException("Attempted to call map() on a NONE value");
    }

    @Override
    public <T> T mapOr(Class<? extends T> mappedclazz, T mapDefault, OptionMapper<T> mapper) 
    {
        return mapDefault;
    }

    @Override
    public OptionState getState() 
    {
        return OptionState.NONE;
    }

    @Override
    public boolean isSome() 
    {
        return false;
    }

    @Override
    public boolean isNone() 
    {
        return true;
    }
}
