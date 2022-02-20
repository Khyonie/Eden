package com.yukiemeralis.blogspot.eden.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.yukiemeralis.blogspot.eden.utils.HashUtils;

import org.bukkit.entity.Player;

public class PlayerData 
{
    // Deprecated
    private boolean autoLogin = false;
    private String autoLoginUsername = null, expectedIP = null;
    private int autoLoginKey = -1;

    @Expose(serialize = true, deserialize = true)
    private String dataVersion = "1.4.0", accountPassword;
    @Expose(serialize = true, deserialize = true)
    private Map<String, ModulePlayerData> moduleData;
    @Expose(serialize = true, deserialize = true)
    private List<String> permissionGroup, addedPermissions, revokedPermissions; 

    /**
     * An instance of a core player account, generated upon first login and saved to a local file.
     */
    public PlayerData()
    {

    }

    void updateDataVersion(String version)
    {
        this.dataVersion = version;
    }

    public String getDataVersion()
    {
        return this.dataVersion;
    }

    public void addPermissionGroup(String permissionGroup)
    {
        if (this.permissionGroup == null)
            this.permissionGroup = new ArrayList<>(); 
        this.permissionGroup.add(permissionGroup);
    }

    public boolean removePermissionGroup(String name)
    {
        if (this.permissionGroup != null)
            return this.permissionGroup.remove(name);
        return false;
    }

    public List<String> getPermissionGroups()
    {
        return this.permissionGroup;
    }

    public boolean hasPermission(String permission)
    {
        if (this.addedPermissions == null)
            return false;
        return this.addedPermissions.contains(permission);
    }

    public List<String> getPermissions()
    {
        return this.addedPermissions;
    }

    public List<String> getRevokedPermissions()
    {
        return this.revokedPermissions;
    }

    public void addPermission(String permission)
    {
        if (this.addedPermissions == null)
            this.addedPermissions = new ArrayList<>();
        if (this.addedPermissions.contains(permission))
            return;
        addedPermissions.add(permission);
    }

    public boolean removePermission(String permission)
    {
        if (this.addedPermissions == null)
            return false;
        return this.addedPermissions.remove(permission);
    }

    public void addRevokedPermission(String permission)
    {
        if (this.revokedPermissions == null)
            this.revokedPermissions = new ArrayList<>();
        if (this.revokedPermissions.contains(permission))
            return;
        revokedPermissions.add(permission);
    }

    public boolean removeRevokedPermission(String permission)
    {
        if (this.revokedPermissions == null)
            return false;
        return this.revokedPermissions.remove(permission);
    }

    public boolean hasPassword()
    {
        return this.accountPassword != null;
    }

    public void setPassword(String passwordHash)
    {
        this.accountPassword = passwordHash;
    }

    public void removePassword()
    {
        this.accountPassword = null;
    }

    public boolean comparePassword(String input)
    {
        if (!hasPassword())
            return false;
        if (input == null)
            return false;
        return this.accountPassword.equals(HashUtils.hexToString(HashUtils.hashStringSHA256(input)));
    }

    public boolean hasModuleData(String modName)
    {
        if (this.moduleData == null)
            return false;
        return this.moduleData.containsKey(modName);
    }

    public void createModuleData(String modName)
    {
        if (this.moduleData == null)
            this.moduleData = new HashMap<>();
        this.moduleData.put(modName, new ModulePlayerData(modName)); 
    }

    public void createModuleData(String modName, Map<String, Object> defaultData)
    {
        if (this.moduleData == null)
            this.moduleData = new HashMap<>();
        this.moduleData.put(modName, new ModulePlayerData(modName, defaultData));
    }

    public void removeModuleData(String modName)
    {
        if (this.moduleData == null)
            return;
        this.moduleData.remove(modName);
    }

    public ModulePlayerData getModuleData(String modName)
    {
        if (this.moduleData == null)
            return null;
        return moduleData.get(modName);
    }

    // ############################
    // ##### Start deprecated #####
    // ############################

    /**
     * An instance of a core player account, generated upon first login and saved to a local file.
     * @param player The player this account is tied to
     * @deprecated This constuctor sets up for a deprecated feature.
     */
    @Deprecated
    public PlayerData(Player player)
    {
        this.expectedIP = player.getAddress().getAddress().getHostAddress();
    }

    /**
     * Enables automatic login for this account.
     * @param target The player to target
     * @param username The username to the secure account for autologin
     * @param password_hashed The hashed password to the secure account
     * @deprecated Secure player accounts are now deprecated.
     */
    @Deprecated
    public void enableAutoLogin(Player target, String username, String password_hashed)
    {
        autoLogin = true;
        autoLoginUsername = username;
        autoLoginKey = generateKey(password_hashed);
        this.expectedIP = target.getAddress().getAddress().getHostAddress();
    }

    /**
     * Disables automatic login for this account.
     * @deprecated Secure player accounts are now deprecated.
     */
    @Deprecated
    public void disableAutoLogin()
    {
        autoLogin = false;
        autoLoginUsername = null;
        autoLoginKey = -1;
        expectedIP = null;
    }

    /**
     * Generates a key to the SecurePlayerAccount.
     * @param password_hashed
     * @return
     * @deprecated Secure player accounts are now deprecated.
     */
    @Deprecated
    protected static int generateKey(String password_hashed)
    {
        int value = 0;
        
        for (char c : password_hashed.toCharArray())
            value += (byte) c;

        return value;
    }

    /**
     * Gets if this account has automatic login enabled.
     * @return This account's autologin status.
     * @deprecated Secure player accounts are now deprecated.
     */
    @Deprecated
    public boolean isAutoLogin()
    {
        return autoLogin;
    }

    /**
     * Gets this account's autologin key.
     * @return The autologin key associated with this account.
     * @deprecated Secure player accounts are now deprecated.
     */
    @Deprecated
    public int getAutologinKey()
    {
        return this.autoLoginKey;
    }

    /**
     * Gets this account's autologin account username.
     * @return The account name to autologin to.
     * @deprecated Secure player accounts are now deprecated.
     */
    @Deprecated
    public String getAutologinUsername()
    {
        return this.autoLoginUsername;
    }

    /**
     * Gets the expected IP for the player who owns this account.<p>
     * 
     * If the expected IP and connected IP don't match, automatic login will be cancelled and disabled for this account.
     * @return This account's expected IP
     * @deprecated Secure player accounts are now deprecated.
     */
    @Deprecated
    public String getExpectedIP()
    {
        return this.expectedIP;
    }
}
