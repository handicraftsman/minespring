package com.github.handicraftsman.minespring.forge;

import dev.architectury.platform.forge.EventBuses;
import com.github.handicraftsman.minespring.Minespring;
import lombok.val;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FMLServiceProvider;
import net.minecraftforge.fml.loading.moddiscovery.ClasspathLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModDiscoverer;
import net.minecraftforge.forgespi.locating.IModFile;

import java.nio.file.Path;
import java.util.stream.Collectors;

@Mod(Minespring.MOD_ID)
public class MinespringForge {
    public MinespringForge() {
        EventBuses.registerModEventBus(Minespring.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        //val mods = new ClasspathLocator().scanCandidates().distinct().collect(Collectors.toList());

        //for (Path mod : mods) {
        //    System.out.println(mod.toString());
        //}

        //Minespring.init();
    }
}