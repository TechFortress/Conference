package com.robomwm.Conference;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by robom on 5/22/2016.
 */
public class ConferenceRoom
{
    private String name;
    private Set<Player> participants = new HashSet<>();

    ConferenceRoom(String name, Player player)
    {
        this.name = name.toLowerCase();
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
    public Set<Player> getParticipants()
    {
        return participants;
    }

    /**
     * @return A nicely formatted string of participants
     */
    public String getParticipantsToString()
    {
        StringBuilder participantsBuilder = new StringBuilder();
        for (Player participant : getParticipants())
        {
            participantsBuilder.append(", ");
            participantsBuilder.append(participant.getName());
        }
        participantsBuilder.delete(0, 2);
        return participantsBuilder.toString();
    }

    /**
     * Broadcasts a message to all conference participants
     * @param inputMessage
     */
    public void sendBroadcast(String inputMessage)
    {
        String message = ChatColor.BLUE + "#" + this.name + " " + ChatColor.DARK_AQUA + inputMessage;
        for (Player participants : this.participants)
            participants.sendMessage(message);
    }

    /**
     * Sends a chat message to conference participants (i.e. broadcast sans room name)
     * @param inputMessage Message to send
     * @param playerName Player name to send as
     */
    public void sendChat(String playerName, String inputMessage)
    {
        String message = ChatColor.DARK_AQUA + playerName + ": " + ChatColor.AQUA + inputMessage;
        for (Player participants : this.participants)
            participants.sendMessage(message);
    }
}
