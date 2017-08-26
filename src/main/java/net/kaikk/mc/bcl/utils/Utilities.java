package net.kaikk.mc.bcl.utils;

import net.kaikk.mc.bcl.config.Config;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Rob5Underscores on 10/12/2016.
 */
public class Utilities {

    public static UUID getUUIDFromName(String name) {
        Optional<Player> onlinePlayer = Sponge.getServer().getPlayer(name);
        if (onlinePlayer.isPresent()) {
            return onlinePlayer.get().getUniqueId();
        } else {
            Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
            Optional<User> optUser = userStorage.get().get(name);
            if (optUser.isPresent()) {
                User user = optUser.get();
                return user.getUniqueId();
            }
        }
        return null;
    }

    public static int addGroupValue(UUID uuid, String name, int value) {
        Optional<User> userOptional = Utilities.getUserFromUUID(uuid);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : Config.getConfig().get().getNode("Groups").getChildrenMap().entrySet()) {
                if (user.hasPermission("group." + entry.getKey().toString())) {
                    value += entry.getValue().getNode(name).getInt();
                }
            }
        }

        return value;
    }

    public static Optional<User> getUserFromUUID(UUID uuid) {
        Optional<Player> onlinePlayer = Sponge.getServer().getPlayer(uuid);
        if (onlinePlayer.isPresent()) {
            return Optional.of(onlinePlayer.get());
        }

        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        if (userStorage.isPresent()) {
            return userStorage.get().get(uuid);
        }

        return Optional.empty();
    }

    public static Player getPlayerFromName(String name) {
        Optional<Player> onlinePlayer = Sponge.getServer().getPlayer(name);
        if (onlinePlayer.isPresent()) {
            return onlinePlayer.get();
        } else {
            Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
            Optional<User> optUser = userStorage.get().get(name);
            if (optUser.isPresent()) {
                User user = optUser.get();
                if (user.getPlayer().isPresent()) {
                    return user.getPlayer().get();
                }
            }
        }
        return null;
    }
}
