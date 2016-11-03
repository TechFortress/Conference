# Conference
An IRC-inspired "group-chat" Bukkit plugin, featuring OOP

### Commands:
- `/join <room name>` - joins a conference room with the specified name. Room is created if it does not exist. Names are not case-sensitive. Runs `/part` if you are already in a conference room.
- `/part` - removes you from the current room.
- `/who` - lists participants in the room.

Participants currently do not persist - i.e., if you disconnect from the server, you will be `/part`ed from the room.

There is a variant (on another branch) that makes use of and requires the `/invite` command to allow participants to join the room.
