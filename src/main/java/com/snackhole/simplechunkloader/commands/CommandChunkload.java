package com.snackhole.simplechunkloader.commands;

import com.snackhole.simplechunkloader.SimpleChunkloaderMain;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeChunkManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
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
        return "\"/chunkload\" or \"/chunk\" displays a list of existing chunkload tickets by nickname.  Add a nickname to get a list of chunks in the ticket.  Add to the nickname a radius between 0 and 2 to create a new ticket loading chunks in that radius around the chunk you're in, or add \"release\" to release an existing ticket.";
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
        HashMap<String, ForgeChunkManager.Ticket> ticketsMap = SimpleChunkloaderMain.chunkloadManager.getTicketsMap();
        if (args.length == 0) {
            String ticketMessage = "Chunkload tickets:  ";
            for (String nickname : ticketsMap.keySet()) {
                ticketMessage += nickname + "; ";
            }
            if (ticketMessage.endsWith(":  ")) {
                ticketMessage += " None; ";
            }
            ticketMessage += "maximum chunks per ticket:  " + ForgeChunkManager.getMaxChunkDepthFor(SimpleChunkloaderMain.MODID) + "; tickets remaining:  " + (ForgeChunkManager.getMaxTicketLengthFor(SimpleChunkloaderMain.MODID) - SimpleChunkloaderMain.chunkloadManager.getTicketsMap().size());
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
            if (!SimpleChunkloaderMain.chunkloadManager.getTicketsMap().containsKey(args[0])) {
                return false;
            }
        }
        if (args.length == 2) {
            if (args[1].equals("release")) {
                if (!SimpleChunkloaderMain.chunkloadManager.getTicketsMap().containsKey(args[0])) {
                    return false;
                }
            } else {
                if (SimpleChunkloaderMain.chunkloadManager.getTicketsMap().containsKey(args[0])) {
                    return false;
                }
                int radius = -1;
                try {
                    radius = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    return false;
                }
                if (radius < 0 || radius > 2) {
                    return false;
                }
            }
        }
        return true;
    }
}
