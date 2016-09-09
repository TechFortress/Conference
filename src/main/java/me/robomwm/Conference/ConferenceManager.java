package me.robomwm.Conference;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by robom on 5/22/2016.
 */
public class ConferenceManager
{
    private Map<String, ConferenceRoom> conferenceRooms = new HashMap<>();
    private Map<Player, ConferenceParticipant> conferenceParticipants = new HashMap<>();

    /**
     * Gets a stored ConferenceRoom
     * @param roomString
     * @return null if the room does not exist
     */
    public ConferenceRoom getConferenceRoom(String roomString)
    {
        return this.conferenceRooms.get(roomString.toLowerCase());
    }

    /**
     * Adds a player to a conference room, and creates one if the room doesn't exist
     * removes player from their prior conference room, if in one
     * Broadcasts join to all conference participants
     * @param player
     * @param roomString
     * @return No longer used, always returns true. (Was previously used for invite)
     */
    public boolean addParticipant(Player player, String roomString)
    {
        roomString = roomString.toLowerCase();

        //check for existence of specified room, and create if it doesn't exist
        if (!this.conferenceRooms.containsKey(roomString))
        {
            removeParticipant(player, true);
            ConferenceRoom room = new ConferenceRoom(roomString, player);
            this.conferenceRooms.put(roomString, room);
            this.conferenceParticipants.put(player, new ConferenceParticipant(room));
            room.sendBroadcast(ChatColor.GREEN + "Successfully created a new conference room");
            room.sendBroadcast("Tell other players to " + ChatColor.GOLD + "/join " + roomString);
            return true;
        }

        ConferenceRoom room = this.conferenceRooms.get(roomString);

        //Otherwise if it exists, just add player to room and broadcast
        removeParticipant(player, true);
        room.addParticipant(player);
        room.sendBroadcast(player.getName() + " joined the conference room.");
        this.conferenceParticipants.put(player, new ConferenceParticipant(this.getRoom(roomString)));
        return true;
    }

    /**
     * Returns the conference room the player is in
     * @param player
     * @return null if it can't find stuff
     */
    public ConferenceRoom getParticipantRoom(Player player)
    {
        ConferenceParticipant participant = this.conferenceParticipants.get(player);
        if (participant == null)
            return null;
        return conferenceParticipants.get(player).getConferenceRoom();
    }

    public ConferenceRoom getRoom(String room)
    {
        return conferenceRooms.get(room);
    }

    /**
     * Removes a player from a conference room
     * Also deletes the conference room, if empty
     * @param player
     * @return false if player was not a participant
     */
    public boolean removeParticipant(Player player, boolean broadcastRemove)
    {
        if (this.conferenceParticipants.containsKey(player))
        {
            ConferenceRoom room = getParticipantRoom(player);
            if (broadcastRemove)
                room.sendBroadcast(player.getName() + " left the conference room.");
            room.removeParticipant(player);
            removeRoomIfEmpty(room.getName());
            this.conferenceParticipants.remove(player);
            return true;
        }
        else
            return false;
    }

    /**
     * Used internally
     * Removes a room if nobody is inside it.
     */
    public void removeRoomIfEmpty(String roomString)
    {
        if (this.conferenceRooms.get(roomString).isEmpty())
            this.conferenceRooms.remove(roomString);
    }

    /**
     * Removes a conference room and its participants
     * @param roomString
     * @return false if room doesn't exist
     */
    public boolean removeConferenceRoom(String roomString)
    {
        roomString = roomString.toLowerCase();
        if (!this.conferenceRooms.containsKey(roomString))
            return false;

        ConferenceRoom room = this.conferenceRooms.get(roomString);

        for (Player participant : room.getParticipants())
            this.conferenceParticipants.remove(participant);

        this.conferenceRooms.remove(roomString);
        return true;
    }
}
