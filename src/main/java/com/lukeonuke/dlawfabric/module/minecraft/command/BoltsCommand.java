package com.lukeonuke.dlawfabric.module.minecraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

public abstract class BoltsCommand implements Command<CommandSourceStack> {
    public String getRequiredPermission(){
        return "dlaw-fabric.command." + getCommandName();
    }
    abstract public String getCommandName();
    abstract public List<RequiredArgumentBuilder<CommandSourceStack, ?>> getArguments();
}
