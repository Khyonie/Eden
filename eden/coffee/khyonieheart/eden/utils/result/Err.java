package fish.yukiemeralis.eden.utils.result;

import fish.yukiemeralis.eden.utils.exception.UnwrapException;

public class Err implements Result 
{
    private Object contained;

    public Err(Object contained)
    {
        this.contained = contained;
    }

    @Override
    public <T> T unwrapOk(Class<? extends T> clazz) 
    {
        throw new UnwrapException("Attempted to unwrap an Err into an Ok type");
    }

    @Override
    public <T> T unwrapErr(Class<? extends T> clazz) 
    {
        return clazz.cast(contained);
    }

    @Override
    public boolean isOk() 
    {
        return false;
    }

    @Override
    public boolean isErr() 
    {
        return true;
    }

    @Override
    public ResultState getState() 
    {
        return ResultState.ERR;
    } 
}
