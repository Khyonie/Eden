package fish.yukiemeralis.eden.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * A grouped list of permission and permission states. Players can be assigned groups to have permissions added in bulk.<p>
 * Permissions can be "negative" or revoked, meaning players cannot use the associated command, even if they've been allowed access elsewhere. 
 */
public class PermissionGroup 
{
    @Expose
    private String name;

    @Expose
    private List<String> associatedPermissions = new ArrayList<>();

    public PermissionGroup(String name, String... permissions)
    {
        this.name = name;
        this.associatedPermissions = new ArrayList<>(Arrays.asList(permissions));
    } 

    public PermissionGroup(String name, PermissionGroup base)
    {
        this(name, base.getPermissions().toArray(new String[base.getPermissions().size()]));
    }

    public boolean isAuthorized(String permission)
    {
        if (associatedPermissions.contains(permission)) 
        {
            return true;
        }

        return false;
    }

    public String getName()
    {
        return this.name;
    }

    public boolean hasPermission(String permission)
    {
        return associatedPermissions.contains(permission);
    } 

    public void addPermission(String perm) 
    {
        this.associatedPermissions.add(perm);
    }

    public void addPermissions(String... perms)
    {
        for (String perm : perms)
            addPermission(perm);
    }

    public void addPermissions(Collection<String> perms)
    {
        this.associatedPermissions.addAll(perms);
    }

    public boolean removePermission(String permission)
    {
        return associatedPermissions.remove(permission);
    }

    public List<String> getPermissions()
    {
        return this.associatedPermissions;
    }
}
