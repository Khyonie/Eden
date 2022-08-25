package fish.yukiemeralis.eden.utils.option;

public class Some implements Option
{
    private Object contained;

    public Some(Object contained)
    {
        this.contained = contained;
    }

    @Override
    public <T> T unwrap(Class<? extends T> clazz) 
    {
        return clazz.cast(contained);
    }

    @Override
    public <T> T unwrapOr(Class<? extends T> clazz, T orDefault) 
    {
        return unwrap(clazz);
    }

    @Override
    public <T> T unwrapOrElse(Class<? extends T> clazz, OptionComputer<T> computer) 
    {
        return unwrap(clazz);
    }

    @Override
    public <T> T map(Class<? extends T> mappedclazz, OptionMapper<T> mapper) 
    {
        return mapper.map(contained);
    }

    @Override
    public <T> T mapOr(Class<? extends T> mappedclazz, T mapDefault, OptionMapper<T> mapper) 
    {
        return map(mappedclazz, mapper);
    }

    @Override
    public OptionState getState() 
    {
        return OptionState.SOME;
    }

    @Override
    public boolean isSome() 
    {
        return true;
    }

    @Override
    public boolean isNone() 
    {
        return false;
    }
}
