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
    ConferenceManager conferenceManager = new ConferenceManager();

    String notInAConference = ChatColor.RED + "You are not in a conference; " + ChatColor.GOLD + "/join <conference name>";

    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        /**
         * Administrative commands
         */
        if (sender.hasPermission("topkek"))
        {
            if (cmd.getName().equalsIgnoreCase("conferenceadmin"))
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
                    //Can I do this and just check message for null? String message = conferenceManager.getParticipantRoom(Bukkit.getPlayerExact(args[1])).getName();

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
                        sender.sendMessage("Successfully parted player.");
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
            if (conferenceManager.removeParticipant(player))
                player.sendMessage(ChatColor.GREEN + "You left the conference room.");
            else
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

            String conference = playerConferenceDirectory.get(player);
            if (conference == null) //If not in a conference
            {
                player.sendMessage(notInAConference);
                return true;
            }

            //put message together
            String message = ChatColor.BLUE + "[" + conference + "] " + player.getName() + ":" + ChatColor.DARK_AQUA + args.toString().substring(2);
            //and send it to the conference participants
            for (Player participants : conferenceMap.get(conference))
                participants.sendMessage(message);
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
                player.sendMessage(ChatColor.RED + "Doesn't look like " + ChatColor.AQUA + args[1] + ChatColor.RED + " is online or a valid name.");
                return true;
            }

            if (!conferenceManager.getParticipantRoom(player).invite(invitee))


            return true;
        }
        sender.sendMessage("Whoops, did you make a mistake? Don't forget about " + ChatColor.GOLD + "/help");
        return true;
    }

    /**
     * Causes a player to join a conference.
     * If the player is a participant of another conference, it will remove them from that conference
     * @param conference
     * @param player
     * @return
     */
    void joinConference(String conference, Player player)
    {
        //If they're a participant of another conference, remove them from that conference
        partConference(player);

        //If the specified conference room doesn't exist...
        if (!conferenceMap.containsKey(conference))
        {
            //Creating conferences require permission
            if (!player.hasPermission("tech.supporter") && !player.hasPermission("tf.we"))
            {
                player.sendMessage("In order to create a new conference, please watch an " + ChatColor.GOLD + "/ad");
                player.chat("/ad");
                return;
            }

            conferenceMap.put(conference, new HashSet<>()); //Create the conference room
            conferenceMap.get(conference).add(player); //Add the player to it
            playerConferenceDirectory.put(player, conference);
            player.sendMessage(ChatColor.GREEN + "Successfully created a new conference room named " + ChatColor.BLUE + conference);
            player.sendMessage("Use " + ChatColor.GOLD + "/invite " + ChatColor.RESET + "to allow others to join your conference.");
            player.sendMessage("Use " + ChatColor.GOLD + "/c <message> " + ChatColor.RESET + "to send messages to the conference.");
            player.sendMessage("Use " + ChatColor.GOLD + "/part " + ChatColor.RESET + "to leave the conference.");
            return;
        }

        //If player has not yet been invited to that conference
        if (!conferenceInvitees.containsKey(conference) || !conferenceInvitees.get(conference).contains(player))
        {
            player.sendMessage(ChatColor.RED + "You have not been invited to this conference.");
            return;
        }

        //Add player to conference and notify all participants
        conferenceMap.get(conference).add(player);
        String message = ChatColor.BLUE + "[" + conference + "] " + ChatColor.DARK_AQUA + player.getName() + " joined the conference room.";
        for (Player participants : conferenceMap.get(conference))
            participants.sendMessage(message);
        player.sendMessage("Use " + ChatColor.GOLD + "/c <message> " + ChatColor.RESET + "to send messages to the conference.");
        player.sendMessage("Use " + ChatColor.GOLD + "/part " + ChatColor.RESET + "to leave the conference.");
    }

    /**
     * Causes a player to leave a conference
     * Returns false if player wasn't part of a conference in the first place, true otherwise.
     * Deletes the conference if the conference is empty
     * Otherwise, notifies all remaining participants of that conference of their departure
     * @param player
     */
    boolean partConference(Player player)
    {
        String conference = playerConferenceDirectory.get(player);
        if (conference == null) //No conference to part
            return false;
        playerConferenceDirectory.remove(player);
        conferenceMap.get(conference).remove(player);

        //Remove conference if empty
        if (conferenceMap.get(conference).isEmpty())
        {
            conferenceMap.remove(conference);
            conferenceInvitees.remove(conference);
            return true;
        }

        //Otherwise, notify remaining participants
        for (Player participants : conferenceMap.get(conference))
            participants.sendMessage(ChatColor.BLUE + player.getName() + " left " + conference);
        return true;
    }

    /**
     * Invites a player to a conference
     */
    void invitePlayer(Player player, Player invitee)
    {
        //Check if player is in a conference
        if (!playerConferenceDirectory.containsKey(player))
        {
            player.sendMessage(ChatColor.RED + "You are not in a conference; " + ChatColor.GOLD + "/join <conference name>");
            return;
        }
        //Check if invitee is already in another conference
        if (playerConferenceDirectory.containsKey(invitee))
        {
            player.sendMessage(ChatColor.RED + invitee.getName() + " is already in another conference.");
            return;
        }

        String conference = playerConferenceDirectory.get(player);

        //Check if invitee has already been invited
        if (conferenceInvitees.containsKey(conference) && conferenceInvitees.get(conference).contains(player))
        {
            player.sendMessage(ChatColor.RED + invitee.getName() + " has already been invited to this conference.");
            return;
        }

        String message = ChatColor.BLUE + "[" + conference + "] " + ChatColor.DARK_AQUA + player.getName() + " invited " + invitee.getName();
        conferenceInvitees.get(conference).add(invitee);
        for (Player participants : conferenceMap.get(conference))
            participants.sendMessage(message);
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event)
    {
        partConference(event.getPlayer());
    }
}
