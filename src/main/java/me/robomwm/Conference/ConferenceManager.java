package me.robomwm.Conference;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by robom on 5/22/2016.
 */
public class ConferenceManager
{
    Map<String, ConferenceRoom> conferenceRooms = new HashMap<>();
    Map<Player, String> conferenceDirectory = new HashMap<>();

    /**
     * Adds a player to a conference room
     * returns -1 if the room does not exist
     * returns 0 if the player was not invited to the room
     * returns 1 if successful
     * Removes player from their previous conference room if successful
     * Broadcasts successful join to the room
     * @param player
     * @param roomString
     * @return
     */
    public int addParticipant(Player player, String roomString)
    {
        if (!this.conferenceRooms.containsKey(roomString))
            return -1;

        ConferenceRoom room = this.conferenceRooms.get(roomString);

        if (!this.conferenceRooms.get(room).isInvited(player))
            return 0;


        removeParticipant(player);
        room.addParticipant(player);
        room.sendBroadcast(player.getName() + " joined the conference room.");
        conferenceDirectory.put(player, roomString);
        return 1;
    }

    /**
     * Adds a player to a conference room, and creates one if the room doesn't exist
     * returns -1 if the room did not exist and had to be created
     * returns 0 if the player was not invited to the room
     * returns 1 if successful
     * removes player from their current conference room, if in one
     * @param player
     * @param room
     * @return
     */
    public int createOrAddParticipant(Player player, String room)
    {
        if (this.conferenceDirectory.containsKey(player))
            this.conferenceRooms.get(this.conferenceDirectory.get(player)).removeParticipant(player);
        if (!this.conferenceRooms.containsKey(room))
        {
            conferenceRooms.put(room, new ConferenceRoom(room, player));
            return -1;
        }
        //Otherwise if such a conference room exists, use addParticipant
        return addParticipant(player, room);
    }

    public String getParticipantRoom(Player player)
    {
        return conferenceDirectory.get(player);
    }

    public ConferenceRoom getRoom(String room)
    {
        return conferenceRooms.get(room);
    }

    /**
     * Used internally
     * Removes a player from a conference room
     * @param player
     */
    public void removeParticipant(Player player)
    {
        if (this.conferenceDirectory.containsKey(player))
        {
            ConferenceRoom room = this.conferenceRooms.get(this.conferenceDirectory.get(player));
            room.removeParticipant(player);
            removeRoomIfEmpty(room.getName());
            this.conferenceDirectory.remove(player);
        }
    }

    /**
     * Used internally
     * Removes a room if nobody is inside it.
     * @param room
     * @return
     */
    public void removeRoomIfEmpty(String room)
    {
        if (this.conferenceRooms.get(room).isEmpty())
            conferenceRooms.remove(room);
    }
}
