package me.robomwm.Conference;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by robom on 5/22/2016.
 */
public class ConferenceRoom
{
    String name = "";
    Set<Player> participants = new HashSet<>();
    Set<Player> invitees = new HashSet<>();

    ConferenceRoom(String name, Player player)
    {
        this.name = name;
        this.participants.add(player);
    }

    public boolean removeParticipant(ConferenceParticipant participant)
    {
        if (participants.remove(participant))
            return true;
        else
            return false;
    }

    public boolean addParticipant()
}
