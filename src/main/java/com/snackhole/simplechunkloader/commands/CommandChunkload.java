package com.snackhole.simplechunkloader.commands;

import com.snackhole.simplechunkloader.SimpleChunkloaderMain;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandChunkload extends CommandBase {
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
        return "\"/chunkload\" or \"/chunk\" displays a list of existing chunkload tickets by nickname, plus chunkloading limits.  If a nickname already exists, you can add it to the command to get information about it; further add \"release\" to release its chunks and the name.  If a nickname doesn't exist, add it to the command followed by a radius as low as 0 to create a new ticket loading chunks in that radius around the chunk you're in.";
    }

    @Override
    public List<String> getAliases() {
        return this.commandChunkloadAliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        if (!validArgs(args, player)) {
            return;
        }
        HashMap<String, ForgeChunkManager.Ticket> ticketsMap = SimpleChunkloaderMain.chunkloadManager.getTicketsMap();
        if (args.length == 0) {
            String ticketMessage = "Chunkload tickets:  ";
            for (String nickname : ticketsMap.keySet()) {
                ticketMessage += nickname + "; ";
            }
            if (ticketMessage.endsWith(":  ")) {
                ticketMessage += " None; ";
            }
            ticketMessage += "maximum radius per ticket:  " + SimpleChunkloaderMain.chunkloadManager.getMaxValidRadiusFromChunks(SimpleChunkloaderMain.chunkloadManager.getMaximumChunksPerTicket()) + "; tickets remaining:  " + SimpleChunkloaderMain.chunkloadManager.getTicketsRemaining();
            player.sendMessage(new TextComponentString(ticketMessage));
        } else if (args.length == 1) {
            ForgeChunkManager.Ticket ticket = ticketsMap.get(args[0]);
            NBTTagCompound ticketData = ticket.getModData();
            int chunkPosX = ticketData.getInteger("chunkPosX");
            int chunkPosZ = ticketData.getInteger("chunkPosZ");
            ChunkPos centerChunkPos = new ChunkPos(chunkPosX, chunkPosZ);
            String ticketMessage = args[0] + ":  Chunk " + centerChunkPos.toString() + "; radius " + ticketData.getInteger("radius") + "; dimension " + ticket.world.provider.getDimension() + "; total chunks " + ticket.getChunkList().size() + ".";
            player.sendMessage(new TextComponentString(ticketMessage));
        } else if (args.length == 2) {
            if (args[1].equals("release")) {
                SimpleChunkloaderMain.chunkloadManager.releaseTicket(args[0]);
                player.sendMessage(new TextComponentString(args[0] + " released!"));
            } else {
                SimpleChunkloaderMain.chunkloadManager.requestTicketAndForceChunks(args[0], new ChunkPos(player.getPosition()), Integer.parseInt(args[1]), player.world, ForgeChunkManager.Type.NORMAL);
                player.sendMessage(new TextComponentString(args[0] + " loaded!  " + ticketsMap.get(args[0]).getChunkList().size() + " chunks."));
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayerMP;
    }

    private boolean validArgs(String[] args, EntityPlayerMP player) {
        if (args.length > 2) {
            player.sendMessage(new TextComponentString("Too many arguments!"));
            return false;
        }
        if (args.length == 1) {
            if (!SimpleChunkloaderMain.chunkloadManager.getTicketsMap().containsKey(args[0])) {
                player.sendMessage(new TextComponentString("No ticket exists with that nickname!"));
                return false;
            }
        }
        if (args.length == 2) {
            if (args[1].equals("release")) {
                if (!SimpleChunkloaderMain.chunkloadManager.getTicketsMap().containsKey(args[0])) {
                    player.sendMessage(new TextComponentString("No ticket exists with that nickname!"));
                    return false;
                }
            } else {
                if (SimpleChunkloaderMain.chunkloadManager.getTicketsMap().containsKey(args[0])) {
                    player.sendMessage(new TextComponentString("A ticket with that nickname already exists!"));
                    return false;
                }
                if (SimpleChunkloaderMain.chunkloadManager.getTicketsRemaining() < 1) {
                    player.sendMessage(new TextComponentString("No tickets remaining!  Either release tickets or increase tickets available in config."));
                    return false;
                }
                int radius = -1;
                try {
                    radius = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(new TextComponentString("Radius must be an integer!"));
                    return false;
                }
                int maximumRadius = SimpleChunkloaderMain.chunkloadManager.getMaxValidRadiusFromChunks(SimpleChunkloaderMain.chunkloadManager.getMaximumChunksPerTicket());
                if (radius < 0 || radius > maximumRadius) {
                    player.sendMessage(new TextComponentString("Radius must be between 0 and " + maximumRadius + "!"));
                    return false;
                }
            }
        }
        return true;
    }
}
