package com.snackhole.simplechunkloader.chunkloading;

import com.snackhole.simplechunkloader.SimpleChunkloaderMain;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.HashMap;
import java.util.List;

public class ChunkloadManager implements ForgeChunkManager.LoadingCallback {
    private HashMap<String, ForgeChunkManager.Ticket> ticketsMap = new HashMap<String, ForgeChunkManager.Ticket>();

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        if (!world.isRemote) {
            ticketsMap.clear();
            boolean ticketsDropped = false;
            String logMessage = "Chunkload tickets dropped due to excess chunks:  ";
            int maxRadius = getMaxValidRadiusFromChunks(getMaximumChunksPerTicket());
            for (ForgeChunkManager.Ticket ticket : tickets) {
                NBTTagCompound ticketData = ticket.getModData();
                String nickname = ticketData.getString("nickname");
                ChunkPos centerChunkPos = new ChunkPos(ticketData.getInteger("chunkPosX"), ticketData.getInteger("chunkPosZ"));
                int radius = ticketData.getInteger("radius");
                if (radius <= maxRadius) {
                    forceChunksInRadius(centerChunkPos, radius, ticket);
                    ticketsMap.put(nickname, ticket);
                } else {
                    ticketsDropped = true;
                    logMessage += nickname + ", chunk " + centerChunkPos.toString() + ", radius " + radius + "; ";
                    ForgeChunkManager.releaseTicket(ticket);
                }
            }
            if (ticketsDropped) {
                logMessage = logMessage.trim();
                SimpleChunkloaderMain.logger.warn(logMessage);
            }
        }
    }

    public void forceChunksInRadius(ChunkPos centerChunkPos, int radius, ForgeChunkManager.Ticket ticket) {
        for (int i = centerChunkPos.x - radius; i <= centerChunkPos.x + radius; i++) {
            for (int j = centerChunkPos.z - radius; j <= centerChunkPos.z + radius; j++) {
                ForgeChunkManager.forceChunk(ticket, new ChunkPos(i, j));
            }
        }
    }

    public void requestTicketAndForceChunks(String nickname, ChunkPos playerChunk, int radius, World world, ForgeChunkManager.Type type) {
        ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(SimpleChunkloaderMain.simpleChunkloaderMainInstance, world, type);
        forceChunksInRadius(playerChunk, radius, ticket);
        NBTTagCompound ticketData = ticket.getModData();
        ticketData.setString("nickname", nickname);
        ticketData.setInteger("chunkPosX", playerChunk.x);
        ticketData.setInteger("chunkPosZ", playerChunk.z);
        ticketData.setInteger("radius", radius);
        ticketsMap.put(nickname, ticket);
    }

    public void releaseTicket(String nickname) {
        ForgeChunkManager.releaseTicket(ticketsMap.get(nickname));
        ticketsMap.remove(nickname);
    }

    public HashMap<String, ForgeChunkManager.Ticket> getTicketsMap() {
        return ticketsMap;
    }

    public int getMaximumChunksPerTicket() {
        return ForgeChunkManager.getMaxChunkDepthFor(SimpleChunkloaderMain.MODID);
    }

    public int getTicketsRemaining() {
        return (ForgeChunkManager.getMaxTicketLengthFor(SimpleChunkloaderMain.MODID) - ticketsMap.size());
    }

    public int getMaxValidRadiusFromChunks(int chunks) {
        int sideLength = (int) Math.sqrt((double) chunks);
        return (sideLength - 1) / 2;
    }
}
