package fish.yukiemeralis.eden.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * A grouped list of permission and permission states. Players can be assigned groups to have permissions added in bulk.<p>
 * Permissions can be "negative" or revoked, meaning players cannot use the associated command, even if they've been allowed access elsewhere. 
 * @since 1.4.10
 * @author Yuki_emeralis
 */
public class PermissionGroup 
{
    @Expose
    private String name;

    @Expose
    private List<String> associatedPermissions = new ArrayList<>();

    /**
     * A permissions group with a name and a list of permissions to generate with.
     * @param name This permission group's name
     * @param permissions Variable-args list of permissions to start with
     */
    public PermissionGroup(String name, String... permissions)
    {
        this.name = name;
        this.associatedPermissions = new ArrayList<>(Arrays.asList(permissions));
    } 

    /**
     * A permissions group with a name and a seperate permissions group to clone.
     * @param name This permission group's name
     * @param base Base permissions group
     */
    public PermissionGroup(String name, PermissionGroup base)
    {
        this(name, base.getPermissions().toArray(new String[base.getPermissions().size()]));
    }

    /**
     * Checks if this group contains a permission. Alias of {@link PermissionGroup#hasPermission()}
     * @param permission Permission to check
     * @return Whether this group has the given permission
     */
    public boolean isAuthorized(String permission)
    {
        return associatedPermissions.contains(permission);
    }

    /**
     * 
     * @param permission
     * @return
     */
    public boolean hasPermission(String permission)
    {
        return associatedPermissions.contains(permission);
    } 

    /**
     * Returns this permission group's name.
     * @return This permission group's name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Adds a permission to this group.
     * @param perm Permission to add
     */
    public void addPermission(String perm) 
    {
        this.associatedPermissions.add(perm);
    }

    /**
     * Add one or more permissions to this group.
     * @param perms Variable-length list of permissions. 
     */
    public void addPermissions(String... perms)
    {
        if (perms.length == 0)
            throw new IllegalArgumentException("Must add at least one permission");
        for (String perm : perms)
            addPermission(perm);
    }

    /**
     * Adds a collection of permissions to this group.
     * @param perms Collection of permissions
     */
    public void addPermissions(Collection<String> perms)
    {
        this.associatedPermissions.addAll(perms);
    }

    /**
     * Removes a permission from this group.
     * @param permission Permission to remove
     * @return Whether or not the removal was successful.
     */
    public boolean removePermission(String permission)
    {
        return associatedPermissions.remove(permission);
    }

    /**
     * Returns a list of the permissions associated with this permissions group.
     * @return A list of permissions.
     */
    public List<String> getPermissions()
    {
        return this.associatedPermissions;
    }
}
