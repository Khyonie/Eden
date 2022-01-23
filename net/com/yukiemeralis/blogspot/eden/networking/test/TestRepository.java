package com.yukiemeralis.blogspot.eden.networking.test;

import com.yukiemeralis.blogspot.eden.networking.repos.EdenRepository;
import com.yukiemeralis.blogspot.eden.networking.repos.EdenRepositoryEntry;

public class TestRepository 
{
    public static EdenRepository repo = new EdenRepository("Test repository");
    
    static {
        repo.getEntries().add(new EdenRepositoryEntry("Compatible/not present", "https://google.com", "1.0", "Yuki_emeralis", "v1_17_R1", "v1_18_R1"));
        repo.getEntries().add(new EdenRepositoryEntry("Not compatible/not present", "https://google.com", "1.0", "Yuki_emeralis", "v1_17_R1"));
        repo.getEntries().add(new EdenRepositoryEntry("EdenAuth", "https://google.com", "1.4.2", "Yuki_emeralis", "v1_18_R1")); // Compatible, present, updatable
        repo.getEntries().add(new EdenRepositoryEntry("RustLS", "https://google.com", "1.0", "Yuki_Emeralis", "v1_18_R1")); // Compatible, present, same version
    }
}
