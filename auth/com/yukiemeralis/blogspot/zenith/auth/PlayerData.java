package com.yukiemeralis.blogspot.zenith.auth;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

import org.bukkit.entity.Player;

public class PlayerData 
{
    @Expose(serialize = true, deserialize = true)
    private boolean autoLogin = false;
    @Expose(serialize = true, deserialize = true)
    private String autoLoginUsername = null, expectedIP = null;
    @Expose(serialize = true, deserialize = true)
    private int autoLoginKey = -1;
    @Expose(serialize = true, deserialize = true)
    private Map<String, ModulePlayerData> moduleData = new HashMap<>();

    /**
     * An instance of a core player account, generated upon first login and saved to a local file.
     * @param player The player this account is tied to
     */
    public PlayerData(Player player)
    {
        this.expectedIP = player.getAddress().getAddress().getHostAddress();
    }

    /**
     * Enables automatic login for this account.
     * @param target The player to target
     * @param username The username to the secure account for autologin
     * @param password_hashed The hashed password to the secure account
     */
    public void enableAutoLogin(Player target, String username, String password_hashed)
    {
        autoLogin = true;
        autoLoginUsername = username;
        autoLoginKey = generateKey(password_hashed);
        this.expectedIP = target.getAddress().getAddress().getHostAddress();
    }

    /**
     * Disables automatic login for this account.
     */
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
     */
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
     */
    public boolean isAutoLogin()
    {
        return autoLogin;
    }

    /**
     * Gets this account's autologin key.
     * @return The autologin key associated with this account.
     */
    public int getAutologinKey()
    {
        return this.autoLoginKey;
    }

    /**
     * Gets this account's autologin account username.
     * @return The account name to autologin to.
     */
    public String getAutologinUsername()
    {
        return this.autoLoginUsername;
    }

    /**
     * Gets the expected IP for the player who owns this account.<p>
     * 
     * If the expected IP and connected IP don't match, automatic login will be cancelled and disabled for this account.
     * @return This account's expected IP
     */
    public String getExpectedIP()
    {
        return this.expectedIP;
    }
}
