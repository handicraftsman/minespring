package com.github.handicraftsman.minespring;

import com.github.handicraftsman.minespring.behaviour.MsEventAware;
import com.github.handicraftsman.minespring.behaviour.MsModInitAware;

import lombok.val;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
@MsModInitAware
@MsEventAware
public class Minespring {
    public static final String MOD_ID = "minespring";

    public static void init(List<MsConfig> configs) {
        @SuppressWarnings("resource") // we want this to be available during the whole lifecycle of Minecraft
        val ctx = new AnnotationConfigApplicationContext();

        List<String> classpath = new ArrayList<>();

        for (MsConfig config : configs) {
            for (String p : config.packages) {
                System.out.println("Will scan " + p);
            }
            classpath.addAll(config.packages);
        }

        ctx.scan(classpath.toArray(new String[0]));
        ctx.refresh();
    }

    public static <T> void callBehaviours(List<T> behaviours, Consumer<T> fn) {
        for (T behaviour : behaviours) {
            fn.accept(behaviour);
        }
    }

}