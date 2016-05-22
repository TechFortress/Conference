package me.robomwm.Conference;

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

    Map<String, HashSet<Player>> conferenceMap = new HashMap<>();
    Map<Player, String> playerConferenceDirectory = new HashMap<>();
    Map<String, HashSet<Player>> conferenceInvitees = new HashMap<>();

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
                    if (!conferenceMap.containsKey(args[1]))
                    {
                        sender.sendMessage(args[1] + ChatColor.RED + " conference does not exist.");
                        return true;
                    }

                    for (Player participant : conferenceMap.get(args[1]))
                        playerConferenceDirectory.remove(participant);
                    conferenceMap.remove(args[1]);
                    sender.sendMessage("Deleted conference " + args[1]);
                    return true;
                }

                /**
                 * View any conference's participants
                 */
                if (args[0].equalsIgnoreCase("view"))
                {
                    if (conferenceMap.containsKey(args[1]))
                    {
                        sender.sendMessage(args[1] + ChatColor.RED + " conference does not exist.");
                        return true;
                    }

                    StringBuilder participantsBuilder = new StringBuilder("Participants of " + args[1]);
                    for (Player participant : conferenceMap.get(args[1]))
                    {
                        participantsBuilder.append(", ");
                        participantsBuilder.append(participant.getName());
                    }
                    sender.sendMessage(participantsBuilder.toString());
                    return true;
                }

                /**
                 * Check what conference a player is part of
                 */
                if (args[0].equalsIgnoreCase("check"))
                {
                    String message = playerConferenceDirectory.get(Bukkit.getPlayerExact(args[1]));
                    if (message == null)
                        sender.sendMessage("Player is not part of a conference");
                    else
                        sender.sendMessage(message);
                    return true;
                }

                /**
                 * Forcequit a player from a conference
                 */
                if (args[0].equalsIgnoreCase("part"))
                {
                    if (partConference(Bukkit.getPlayerExact(args[1])))
                        sender.sendMessage("Successfully parted player.");
                    else
                        sender.sendMessage("Was unable to part player. Either player is not online or is not part of a conference.");
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
            if (partConference(player))
                player.sendMessage(ChatColor.GREEN + "You left the conference.");
            else
                player.sendMessage(ChatColor.RED + "You are not in a conference.");
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
                player.sendMessage(ChatColor.RED + "You are not in a conference; " + ChatColor.GOLD + "/join <conference name>");
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
            else
                invitePlayer(player, invitee);
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
            player.sendMessage(ChatColor.GREEN + "Successfully created a new conference room named " + ChatColor.BLUE + conference);
            player.sendMessage("Use " + ChatColor.GOLD + "/invite " + ChatColor.RESET + "to allow others to join your conference.");
            player.sendMessage("Use " + ChatColor.GOLD + "/c <message> " + ChatColor.RESET + "to send messages to the conference.");
            player.sendMessage("Use " + ChatColor.GOLD + "/part" + ChatColor.RESET + "to leave the conference.");
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
        player.sendMessage("Use " + ChatColor.GOLD + "/part" + ChatColor.RESET + "to leave the conference.");
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
