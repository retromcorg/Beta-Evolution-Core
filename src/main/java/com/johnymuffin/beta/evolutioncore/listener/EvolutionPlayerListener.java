package com.johnymuffin.beta.evolutioncore.listener;

import com.johnymuffin.beta.evolutioncore.AuthReturnType;
import com.johnymuffin.beta.evolutioncore.utils.BetaEvolutionsUtils;
import com.johnymuffin.beta.evolutioncore.EvolutionCache;
import com.johnymuffin.beta.evolutioncore.EvolutionCore;
import com.johnymuffin.beta.evolutioncore.event.PlayerEvolutionAuthEvent;
import com.legacyminecraft.poseidon.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EvolutionPlayerListener implements Listener {
    private EvolutionCore plugin;

    public EvolutionPlayerListener(EvolutionCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        final String username = event.getName();
        final String ipAddress = event.getAddress().getHostAddress();
        //Check if user is cached, if they are, skip the lookup
        if (EvolutionCache.getInstance().isPlayerCached(username, ipAddress)) {
            return;
        }

        //Check Entries Async
        BetaEvolutionsUtils betaEvolutions = new BetaEvolutionsUtils(false);
        final BetaEvolutionsUtils.VerificationResults verificationResults = betaEvolutions.verifyUser(username, ipAddress);
        plugin.logInfo(username + " has authenticated with " + verificationResults.getSuccessful() + "/" + verificationResults.getTotal() + " nodes.");
        if (verificationResults.getSuccessful() > 0) {
            EvolutionCache.getInstance().addPlayerAuthentication(username, ipAddress);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final String playerName = event.getPlayer().getName();
        final String ip = event.getPlayer().getAddress().getAddress().getHostAddress();

        if (EvolutionCache.getInstance().isPlayerCached(playerName, ip)) {
            //Player is known in the cache with ip
            plugin.logInfo("Received Authentication Status From Cache for: " + playerName + " - User is verified");
            callAuthenticationEvent(event.getPlayer(), true, AuthReturnType.successful);
        } else {
            callAuthenticationEvent(event.getPlayer(), false, AuthReturnType.successful);
        }
    }

    private void callAuthenticationEvent(Player p, Boolean authStatus, AuthReturnType art) {
        final PlayerEvolutionAuthEvent event = new PlayerEvolutionAuthEvent(p, authStatus, art);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

}
