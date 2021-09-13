package com.yukiemeralis.blogspot.zenith.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;

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
        // this.revokedPermissions = new ArrayList<>(Arrays.asList(revokedPermissions));
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

    // public void addRevokedPermission(String perm)
    // {
    //     this.revokedPermissions.add(perm);
    // }

    // public boolean hasRevokedPermission(String permission)
    // {
    //     return this.revokedPermissions.contains(permission);
    // }

    public boolean removePermission(String permission)
    {
        return associatedPermissions.remove(permission);
    }

    // public boolean removeRevokedPermission(String permission)
    // {
    //     return revokedPermissions.remove(permission);
    // }

    // public List<String> getRevokedPermissions()
    // {
    //     return this.revokedPermissions;
    // }

    public List<String> getPermissions()
    {
        return this.associatedPermissions;
    }
}
