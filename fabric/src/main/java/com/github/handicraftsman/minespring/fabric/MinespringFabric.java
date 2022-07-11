package com.github.handicraftsman.minespring.fabric;

import com.github.handicraftsman.minespring.Minespring;
import com.github.handicraftsman.minespring.MsConfig;
import com.google.gson.Gson;
import com.mojang.authlib.minecraft.client.ObjectMapper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.game.minecraft.patch.ModClassLoader_125_FML;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.fabricmc.loader.impl.launch.knot.KnotClient;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class MinespringFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricLoader loader = FabricLoader.getInstance();
        List<MsConfig> configs = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper(new Gson());
        try {
            Enumeration<URL> mods = FabricLauncherBase.getLauncher().getTargetClassLoader().getResources("minespring.json");
            for (Iterator<URL> it = mods.asIterator(); it.hasNext(); ) {
                URL url = it.next();
                try {
                    String rawJson = Files.readString(Path.of(url.toURI()));
                    MsConfig config = mapper.readValue(rawJson, MsConfig.class);
                    configs.add(config);
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Minespring.init(configs);
    }
}