package coffee.khyonieheart.eden.utils;

import org.bukkit.inventory.ItemStack;

/**
 * A generic item parser that can read and write objects of type T to an itemstack's persistent data container.
 * @author Yuki_emeralis
 *
 * @param <T> Type of object to read/write
 */
public interface ItemParser<T>
{
    /**
     * Writes an object to an itemstack's persistent data container, in a format such that {@link #read(ItemStack)} can retrieve the object.
     * @param target The itemstack to write obj to.
     * @param obj The object to write.
     */
    public void write(ItemStack target, T obj);

    /**
     * Reads data from an itemstack's persistent data container, and returns an instance of T.
     * @param target
     * @return
     */
    public T read(ItemStack target);
}
