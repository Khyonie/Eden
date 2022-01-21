package com.yukiemeralis.blogspot.eden.auth.old;

import com.google.gson.annotations.Expose;
import com.yukiemeralis.blogspot.eden.Eden;

/**
 * @deprecated Permissions are now handled by permission groups.
 */
@Deprecated(forRemoval = false)
public class SecurePlayerAccount 
{
    @Expose(serialize = true, deserialize = true)
    private String username, password;
    @Expose(serialize = true, deserialize = true)
    private boolean enforce2fa = false, disabled = false;
    @Expose(serialize = true, deserialize = true)
    private AccountType type;

    /**
     * Account rank associated with an account.
     * @deprecated Use {@link com.yukiemeralis.blogspot.eden.auth.PermissionGroup} instead. 
     */
    @Deprecated
    public static enum AccountType
    {
        USER (0),
        STAFF (1),
        ADMIN (2),
        SUPERADMIN (3);

        private int value;

        private AccountType(int value)
        {
            this.value = value;
        }

        public int getRank()
        {
            return this.value;
        }
    }

    /**
     * A player account that must be manually logged into. Passwords are saved as a hash in a local file, and is loaded into memory.
     * @param username The username to this account
     * @param password The password to this account
     * @param type The type/rank of this account
     */
    public SecurePlayerAccount(String username, String password, AccountType type)
    {
        this.username = username;
        this.password = Permissions.genHash(password);
        this.type = type;

        int min_2fa_rank = Integer.parseInt(Eden.getModuleManager().getEnabledModuleByName("EdenAuth").getConfig().get("min_rank_for_2fa"));
        if (type.getRank() >= min_2fa_rank)
        {
            this.enforce2fa = true;
            return;
        }

        this.enforce2fa = false;
    }

    public String getUsername()
    {
        return this.username;
    }

    public String getPassword()
    {
        return this.password;
    }

    public boolean is2FA_Secured()
    {
        return this.enforce2fa;
    }

    public AccountType getAccountType()
    {
        return this.type;
    }

    public void disable2FA()
    {
        this.enforce2fa = false;
    }

    public void enable2FA()
    {
        this.enforce2fa = true;
    }

    public boolean isDisabled()
    {
        return this.disabled;
    }
}
