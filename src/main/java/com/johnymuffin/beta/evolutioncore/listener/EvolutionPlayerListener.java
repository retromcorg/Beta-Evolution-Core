package com.johnymuffin.beta.evolutioncore.listener;

import com.johnymuffin.beta.evolutioncore.AuthReturnType;
import com.johnymuffin.beta.evolutioncore.utils.BetaEvolutionsUtils;
import com.johnymuffin.beta.evolutioncore.EvolutionCache;
import com.johnymuffin.beta.evolutioncore.EvolutionCore;
import com.johnymuffin.beta.evolutioncore.event.PlayerEvolutionAuthEvent;
import com.projectposeidon.johnymuffin.ConnectionPause;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPreLoginEvent;

public class EvolutionPlayerListener extends PlayerListener {
    private EvolutionCore plugin;

    public EvolutionPlayerListener(EvolutionCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {

        final String username = event.getName();
        final String ipAddress = event.getAddress().getHostAddress();
        //Check if user is cached, if they are, skip the lookup
        if (EvolutionCache.getInstance().isPlayerCached(username, ipAddress)) {
            return;
        }

        //Add Connection Pause
        ConnectionPause connectionPause = event.addConnectionPause(plugin, "BetaEvolutions");
        //Check Entries Async
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            BetaEvolutionsUtils betaEvolutions = new BetaEvolutionsUtils(false);
            final BetaEvolutionsUtils.VerificationResults verificationResults = betaEvolutions.verifyUser(username, ipAddress);

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                plugin.logInfo(username + " has authenticated with " + verificationResults.getSuccessful() + "/" + verificationResults.getTotal() + " nodes.");
                if (verificationResults.getSuccessful() > 0) {
                    EvolutionCache.getInstance().addPlayerAuthentication(username, ipAddress);
                }
                event.removeConnectionPause(connectionPause);
            });


        });
    }

    private void callAuthenticationEvent(Player p, Boolean authStatus, AuthReturnType art) {
        final PlayerEvolutionAuthEvent event = new PlayerEvolutionAuthEvent(p, authStatus, art);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

}
