package com.yukiemeralis.blogspot.zenith.auth;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class ModulePlayerData 
{
    @Expose
    private String modName;
    @Expose
    private Map<String, String> data = new HashMap<>();

    public String getModuleName()
    {
        return this.modName;
    }

    public Map<String, String> getModuleData()
    {
        return this.data;
    }
}
