package me.robomwm.Conference;

import org.bukkit.ChatColor;
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

    public String getName()
    {
        return this.name;
    }
    public boolean isEmpty()
    {
        return this.participants.isEmpty();
    }
    public boolean removeParticipant(Player participant)
    {
        return participants.remove(participant);
    }
    public boolean addParticipant(Player player)
    {
        return participants.add(player);
    }
    public boolean invite(Player player)
    {
        return invitees.add(player);
    }

    /**
     * Broadcasts a message to all conference participants
     * @param inputMessage
     */
    public void sendBroadcast(String inputMessage)
    {
        String message = ChatColor.BLUE + "[" + this.name + "] " + ChatColor.DARK_AQUA + inputMessage;
        for (Player participants : this.participants)
            participants.sendMessage(message);
    }

    /**
     * Is the player invited to this conference?
     * @param player
     * @return
     */
    public boolean isInvited(Player player)
    {
        return invitees.contains(player);
    }
}
