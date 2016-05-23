package me.robomwm.Conference;

import org.bukkit.entity.Player;

/**
 * Created by robom on 5/22/2016.
 */
public class ConferenceParticipant
{
    private ConferenceRoom conferenceRoom;

    ConferenceParticipant(ConferenceRoom room)
    {
        this.conferenceRoom = room;
    }

    public ConferenceRoom getConferenceRoom()
    {
        return conferenceRoom;
    }
}

