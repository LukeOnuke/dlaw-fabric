package com.lukeonuke.dlawfabric.module.minecraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public abstract class BoltsCommand implements Command<ServerCommandSource> {
    public String getRequiredPermission(){
        return "dlaw-fabric.command." + getCommandName();
    }
    abstract public String getCommandName();
    abstract public List<RequiredArgumentBuilder<ServerCommandSource, ?>> getArguments();
}
