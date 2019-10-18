package br.net.fabiozumbi12.RankUpper.hooks;

import br.net.fabiozumbi12.RankUpper.RUUtil;
import br.net.fabiozumbi12.RankUpper.RankUpper;
import br.net.fabiozumbi12.RankUpper.config.StatsCategory;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.concurrent.TimeUnit;

public class PlaceholdersAPI {

    public PlaceholdersAPI(RankUpper rankUpper) {
        PlaceholderService service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);

        service.loadAll(this, rankUpper).forEach(builder -> {
            if (builder.getId().startsWith("rankupper-")) {
                builder.author("FabioZumbi12");
                builder.version(rankUpper.instance().getVersion().get());
                try {
                    builder.buildAndRegister();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Placeholder(id = "rankupper-joindate")
    public String joinDate(@Source Player p) {
        return RankUpper.get().getStats().stats().players.getOrDefault(RankUpper.get().getStats().getPlayerKey(p), new StatsCategory.PlayerInfoCategory()).JoinDate;
    }

    @Placeholder(id = "rankupper-lastvisit")
    public String lastVisit(@Source Player p) {
        return RankUpper.get().getStats().stats().players.getOrDefault(RankUpper.get().getStats().getPlayerKey(p), new StatsCategory.PlayerInfoCategory()).LastVisit;
    }

    @Placeholder(id = "rankupper-timeplayed-formated")
    public String timePlayed(@Source Player p) {
        return RUUtil.timeDescript(RankUpper.get().getStats().stats().players.getOrDefault(RankUpper.get().getStats().getPlayerKey(p), new StatsCategory.PlayerInfoCategory()).TimePlayed);
    }

    @Placeholder(id = "rankupper-timeplayed-days")
    public String timeDays(@Source Player p) {
        return String.valueOf(TimeUnit.MINUTES.toDays(RankUpper.get().getStats().stats().players.getOrDefault(RankUpper.get().getStats().getPlayerKey(p), new StatsCategory.PlayerInfoCategory()).TimePlayed));
    }

    @Placeholder(id = "rankupper-timeplayed-halfhours")
    public String timeHoursHalf(@Source Player p) {
        long time = RankUpper.get().getStats().stats().players.getOrDefault(RankUpper.get().getStats().getPlayerKey(p), new StatsCategory.PlayerInfoCategory()).TimePlayed;
        long day = TimeUnit.MINUTES.toDays(time);
        return String.valueOf(TimeUnit.MINUTES.toHours(time-TimeUnit.DAYS.toMinutes(day)));
    }

    @Placeholder(id = "rankupper-timeplayed-fullhours")
    public String timeHoursFull(@Source Player p) {
        long time = RankUpper.get().getStats().stats().players.getOrDefault(RankUpper.get().getStats().getPlayerKey(p), new StatsCategory.PlayerInfoCategory()).TimePlayed;
        return String.valueOf(TimeUnit.MINUTES.toHours(time));
    }

    @Placeholder(id = "rankupper-timeplayed-halfminutes")
    public String timeMinutesHalf(@Source Player p) {
        long time = RankUpper.get().getStats().stats().players.getOrDefault(RankUpper.get().getStats().getPlayerKey(p), new StatsCategory.PlayerInfoCategory()).TimePlayed;
        long day = TimeUnit.MINUTES.toDays(time);
        long hours = TimeUnit.MINUTES.toHours(time-TimeUnit.DAYS.toMinutes(day));
        return String.valueOf(TimeUnit.MINUTES.toMinutes((time-TimeUnit.DAYS.toMinutes(day))-TimeUnit.HOURS.toMinutes(hours)));
    }

    @Placeholder(id = "rankupper-timeplayed-fullminutes")
    public String timeMinutesFull(@Source Player p) {
        long time = RankUpper.get().getStats().stats().players.getOrDefault(RankUpper.get().getStats().getPlayerKey(p), new StatsCategory.PlayerInfoCategory()).TimePlayed;
        return String.valueOf(TimeUnit.MINUTES.toMinutes(time));
    }
}
