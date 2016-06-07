package me.robomwm.Conference;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by robom on 5/21/2016.
 */
public class Conference extends JavaPlugin implements Listener
{
    private ConferenceManager conferenceManager;

    String notInAConference = ChatColor.RED + "You are not in a conference. Use " + ChatColor.GOLD + "/join <room name>";
    String cannotInviteSelf = ChatColor.RED + "Yea um, no need to invite yourself.";

    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        conferenceManager = new ConferenceManager();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        /**
         * Administrative commands
         */
        if (sender.hasPermission("topkek"))
        {
            if (cmd.getName().equalsIgnoreCase("confadmin"))
            {
                if (args.length < 2)
                    return false;

                /**
                 * Delete a conference
                 */
                if (args[0].equalsIgnoreCase("delete"))
                {
                    if (!conferenceManager.removeConferenceRoom(args[1]))
                    {
                        sender.sendMessage(args[1] + ChatColor.RED + " conference does not exist.");
                        return true;
                    }
                    sender.sendMessage("Deleted conference " + args[1]);
                    return true;
                }

                /**
                 * View any conference's participants
                 */
                if (args[0].equalsIgnoreCase("view"))
                {
                    ConferenceRoom room = conferenceManager.getRoom(args[1]);

                    if (room == null)
                    {
                        sender.sendMessage(args[1] + ChatColor.RED + " conference does not exist.");
                        return true;
                    }
                    sender.sendMessage("Participants of " + room.getName());
                    sender.sendMessage(room.getParticipantsToString());
                    return true;
                }

                /**
                 * Check what conference a player is part of
                 */
                if (args[0].equalsIgnoreCase("check"))
                {
                    Player checkPlayer = Bukkit.getPlayerExact(args[1]);
                    if (checkPlayer == null)
                    {
                        sender.sendMessage(args[1] + " does not exist/is not online.");
                        return true;
                    }
                    ConferenceRoom room = conferenceManager.getParticipantRoom(checkPlayer);
                    if (room == null)
                    {
                        sender.sendMessage("Player is not part of a conference");
                        return true;
                    }
                    sender.sendMessage(room.getName());
                    return true;
                }

                /**
                 * Forcequit a player from a conference
                 */
                if (args[0].equalsIgnoreCase("part"))
                {
                    Player partPlayer = Bukkit.getPlayerExact(args[1]);
                    if (partPlayer != null && conferenceManager.removeParticipant(partPlayer, true))
                        sender.sendMessage("Successfully parted " + partPlayer.getName());
                    else
                        sender.sendMessage("Was unable to part player. Either player is not online or is not part of a conference.");
                    return true;
                }

                /**
                 * Broadcast a message to a specific room
                 */
                if (args[0].equalsIgnoreCase("broadcast"))
                {
                    ConferenceRoom room = conferenceManager.getRoom(args[1]);
                    if (room == null)
                    {
                        sender.sendMessage("That room does not exist.");
                        return true;
                    }
                    room.sendBroadcast(StringUtils.join(args, " ", 2, args.length));
                    return true;
                }
                return false;
            }
        }


        /**
         * Player commands
         */
        if (!(sender instanceof Player))
            return false;
        Player player = (Player)sender;

        /**
         * Part a conference
         */
        if (cmd.getName().equalsIgnoreCase("part"))
        {
            if (!conferenceManager.removeParticipant(player, true))
                player.sendMessage(ChatColor.RED + "You are not in a conference room.");
            return true;
        }

        /**
         * Conference chat command
         */
        if (cmd.getName().equalsIgnoreCase("c"))
        {
            if (args.length < 1)
                return false;

            ConferenceRoom room = conferenceManager.getParticipantRoom(player);
            if (room == null) //If not in a conference
            {
                player.sendMessage(notInAConference);
                return true;
            }
            room.sendBroadcast(player.getName() + ": " + ChatColor.AQUA + StringUtils.join(args, " "));
            return true;
        }

        /**
         * Conference join command
         */
        if (cmd.getName().equalsIgnoreCase("join"))
        {
            if (args.length < 1)
                return false;

            joinConference(args[0], player);
            return true;
        }

        /**
         * Conference invite command
         */
        if (cmd.getName().equalsIgnoreCase("invite"))
        {
            if (args.length < 1)
                return false;

            Player invitee = Bukkit.getPlayerExact(args[0]);
            if (invitee == null || !player.canSee(invitee))
            {
                player.sendMessage(ChatColor.RED + "Doesn't look like " + ChatColor.AQUA + args[0] + ChatColor.RED + " is online or a valid name.");
                return true;
            }

            ConferenceRoom room = conferenceManager.getParticipantRoom(player);
            if (room == null)
            {
                player.sendMessage(notInAConference);
                return true;
            }
            if (player == invitee)
            {
                player.sendMessage(cannotInviteSelf);
                return true;
            }

            if (room.invite(invitee))
            {
                room.sendBroadcast(player.getName() + " invited " + invitee.getName());
                player.sendMessage(ChatColor.GREEN + invitee.getName() + " can now " + ChatColor.GOLD + "/join " + room.getName());
            }
            else //already invited
                player.sendMessage(invitee.getName() + " was already invited. Tell them to use " + ChatColor.GOLD + "/join " + room.getName());

            return true;
        }

        /**
         * who command (lists players in current conference room)
         */
        if (cmd.getName().equalsIgnoreCase("who"))
        {
            ConferenceRoom room = conferenceManager.getParticipantRoom(player);
            if (room != null)
            {
                player.sendMessage(ChatColor.DARK_AQUA + "Participants of " + ChatColor.BLUE + room.getName() + ":");
                player.sendMessage(ChatColor.DARK_AQUA + room.getParticipantsToString());
                return true;
            }
            //Don't return false if not in room, instead command doesn't exist.
        }
        sender.sendMessage("Whoops, did you make a mistake? Don't forget about " + ChatColor.GOLD + "/help");
        return true;
    }

    void joinConference(String conference, Player player)
    {
        if (conference.startsWith("#"))
            conference = conference.substring(1);

        //Creating conferences require permission
        if (conferenceManager.getRoom(conference) == null)
        {
            if (!player.hasPermission("tech.supporter") && !player.hasPermission("tf.we"))
            {
                player.sendMessage("In order to create a new conference, please watch an " + ChatColor.GOLD + "/ad");
                player.chat("/ad");
                return;
            }
        }
        //Check if player is attempting to join the same room
        else if((conferenceManager.getParticipantRoom(player) != null) && (conferenceManager.getParticipantRoom(player).equals(conferenceManager.getRoom(conference))))
        {
            player.sendMessage("You are already in room " + conference);
            return;
        }

        if(conferenceManager.addParticipant(player, conference))
        {
            player.sendMessage("Use " + ChatColor.GOLD + "/c <message> " + ChatColor.RESET + "to send messages to the room.");
            player.sendMessage("Use " + ChatColor.GOLD + "/part " + ChatColor.RESET + "to leave the conference.");
            player.sendMessage("Use " + ChatColor.GOLD + "/who " + ChatColor.RESET + "to see who's in this room.");
            return;
        }
        else //not invited
            player.sendMessage(ChatColor.RED + "You have not been invited to this conference.");
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event)
    {
        conferenceManager.removeParticipant(event.getPlayer(), true);
    }
}
