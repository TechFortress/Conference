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
    Set<ConferenceRoom> conferenceRooms = new HashSet<>();
    Map<Player, String> conferenceDirectory = new HashMap<>();
    ConferenceManager()
    {

    }

    public void addParticipant(Player player, ConferenceRoom room)
    {
        conferenceDirectory.remove(player);
        conferenceDirectory.put(player, new ConferenceParticipant(player, room));
    }

    public ConferenceRoom getParticipantRoom(Player player)
    {
        return getRoom(conferenceDirectory.get(player));
    }

    public ConferenceRoom getRoom(String room)
    {
        return conferenceRooms.;
    }

    public boolean removeParticipant(Player player)
    {
        ConferenceRoom room = getP;
        if (room == null)
            return false;
        return room.removeParticipant(getParticipant(player));
    }

}
