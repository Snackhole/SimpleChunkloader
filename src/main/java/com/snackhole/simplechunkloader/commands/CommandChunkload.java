package com.snackhole.simplechunkloader.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandChunkload implements ICommand {
    private final List commandChunkloadAliases;
    public CommandChunkload() {
        commandChunkloadAliases = new ArrayList();
        commandChunkloadAliases.add("chunkload");
        commandChunkloadAliases.add("chunk");
    }

    @Override
    public String getName() {
        return "chunkload";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "\"/chunkload\" or \"/chunk\" displays a list of existing chunkload tickets by nickname.  Add a nickname and a radius between 0 and 2 to load chunks in that radius around the chunk you're in.  Add \"delete\" instead of a radius to delete an existing ticket.  Nicknames must be unbroken strings, with no spaces, but try to be descriptive so you know where the chunks in question are.";
    }

    @Override
    public List<String> getAliases() {
        return this.commandChunkloadAliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        if (!validArgs(args)) {
            player.sendMessage(new TextComponentString("Invalid command!  Use \"/help chunkload\" to see usage information."));
            return;
        }
        if (args.length == 0){
            player.sendMessage(new TextComponentString("Current chunkload tickets:  ")); // Get list of tickets from chunkload manager.
        } else if (args.length == 2) {
            // Check whether nickname already exists.  If user is trying to delete, proceed only if nickname exists.  If user is trying to add, proceed only if nickname doesn't exist.
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayerMP;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

    private boolean validArgs(String[] args) {
        if (args.length > 2) {
            return false;
        }
        if (args.length == 1) {
            return false;
        }
        if (args.length == 2){
            if (!args[1].equals("0") && !args[1].equals("1") && !args[1].equals("2") && !args[1].equals("delete")){
                return false;
            }
        }
        return true;
    }
}
