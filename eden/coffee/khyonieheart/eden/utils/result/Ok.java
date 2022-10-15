package coffee.khyonieheart.eden.utils.result;

import coffee.khyonieheart.eden.utils.exception.UnwrapException;

public class Ok implements Result 
{
    private Object contained;

    public Ok(Object contained)
    {
        this.contained = contained;
    }   

    @Override
    public <T> T unwrapOk(Class<? extends T> clazz) 
    {
        return clazz.cast(this.contained);
    }

    @Override
    public <T> T unwrapErr(Class<? extends T> clazz) 
    {
        throw new UnwrapException("Attempted to unwrap an Ok into an Err type");
    }

    @Override
    public boolean isOk() 
    {
        return true;
    }

    @Override
    public boolean isErr() 
    {
        return false;
    }

    @Override
    public ResultState getState() 
    {
        return ResultState.OK;
    }
}
