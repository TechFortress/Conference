package me.robomwm.Conference;

import org.bukkit.entity.Player;

/**
 * Created by robom on 5/22/2016.
 */
public class ConferenceParticipant
{
    Player participant;
    ConferenceRoom conferenceRoom;

    ConferenceParticipant(Player player, ConferenceRoom room)
    {
        this.participant = player;
        this.conferenceRoom = room;
    }

    public void setConferenceRoom(ConferenceRoom room)
    {
        this.conferenceRoom = room;
    }

    public ConferenceRoom getConferenceRoom()
    {
        return conferenceRoom;
    }
}
