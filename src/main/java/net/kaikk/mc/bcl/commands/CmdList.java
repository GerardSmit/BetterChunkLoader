package net.kaikk.mc.bcl.commands;

import com.google.common.collect.Lists;
import net.kaikk.mc.bcl.BetterChunkLoader;
import net.kaikk.mc.bcl.CChunkLoader;
import net.kaikk.mc.bcl.datastore.DataStoreManager;
import net.kaikk.mc.bcl.utils.BCLPermission;
import net.kaikk.mc.bcl.utils.Messenger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by ROB on 08/12/2016.
 */
public class CmdList implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        Optional<User> optionalUser = commandContext.getOne("user");
        User user;
        String name;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            name = optionalUser.get().getName();
        } else if (commandSource instanceof Player) {
            user = ((Player) commandSource);
            name = commandSource.getName();
        } else {
            Messenger.senderNotPlayerError(commandSource);
            return CommandResult.empty();
        }
        List<CChunkLoader> clList;
        boolean showUser;
        if (commandContext.getOne("all").isPresent()) {
            clList = DataStoreManager.getDataStore().getChunkLoaders();
            name = "all";
            showUser = true;
        } else {
            clList = DataStoreManager.getDataStore().getChunkLoaders(user.getUniqueId());
            showUser = false;
        }

        List<Text> texts = Lists.newArrayList();
        Optional<String> filterString = commandContext.getOne("filter");
        List<String> filters = filterString.<List<String>>map(s -> Lists.newArrayList(s.split("\\|"))).orElse(new ArrayList<>());

        for (String filter : filters) {
            if (!(filter.equals("offline") || filter.equals("active") || filter.equals("online"))) {
                commandSource.sendMessage(BetterChunkLoader.getPrefix().concat(Text.builder("Invalid filter: " + filter).color(TextColors.RED).build()));
                return CommandResult.empty();
            }
        }

        clList.forEach(chunkLoader -> {
            if (filters.contains("offline") && chunkLoader.isAlwaysOn() ||
                    filters.contains("active") && !chunkLoader.isActive() ||
                    filters.contains("online") && !chunkLoader.isAlwaysOn())
                return;

            texts.add(chunkLoader.toText(showUser, commandSource));
        });

        if (texts.isEmpty()) {
            texts.add(Messenger.getNoChunkLoaders(name));
        }

        PaginationList.Builder builder = PaginationList.builder()
                .title(Text.of(TextColors.GOLD,name + (filters.size() > 0 ? " " + String.join(" and ", filters) : "") + " Chunkloaders"))
                .contents(texts)
                .padding(Text.of("-"));

        if (!showUser) {
            String personal = "*";
            String world = "*";

            if (!user.hasPermission(BCLPermission.ABILITY_UNLIMITED)) {
                personal = String.valueOf(DataStoreManager.getDataStore().getOnlineOnlyFreeChunksAmount(user.getUniqueId()));
                world = String.valueOf(DataStoreManager.getDataStore().getAlwaysOnFreeChunksAmount(user.getUniqueId()));
            }

            builder.header(Text.of("Available online chunks: " + personal + ", available offline chunks: " + world));
        }

        builder.sendTo(commandSource);

        return CommandResult.empty();
    }

}
