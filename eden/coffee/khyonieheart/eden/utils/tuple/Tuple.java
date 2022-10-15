package coffee.khyonieheart.eden.utils.tuple;

import java.lang.reflect.Field;

public class Tuple 
{
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("[");

        for (Field f : getClass().getFields())
            try {
                builder.append(f.get(this).toString() + ", ");
            } catch (IllegalAccessException e) {
                builder.append("exception!, ");
            }

        builder.delete(builder.length() - 3, builder.length() - 1);
        return builder.append(")").toString();
    }
}
