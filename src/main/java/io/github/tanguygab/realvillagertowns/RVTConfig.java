package io.github.tanguygab.realvillagertowns;

import org.bukkit.configuration.file.FileConfiguration;

public class RVTConfig {

    private final RealVillagerTowns rvt;

    public RVTConfig(RealVillagerTowns rvt) {
        this.rvt = rvt;
        rvt.saveDefaultConfig();
        rvt.reloadConfig();
        FileConfiguration cfg = rvt.getConfig();


    }



}
