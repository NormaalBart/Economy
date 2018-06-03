package me.bartvv.economy.api;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import me.bartvv.economy.manager.User;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class VaultRegister implements Economy {

	private me.bartvv.economy.Economy economy;

	public VaultRegister(me.bartvv.economy.Economy economy) {
		this.economy = economy;
		Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(), economy);
		economy.getLogger().log(Level.INFO, "Vault support enabled.");
	}

	@Override
	public boolean isEnabled() {
		return economy != null;
	}

	@Override
	public String getName() {
		return economy.getDescription().getName();
	}

	@Override
	public String format(double amount) {
		return displayCurrency(new BigDecimal(amount));
	}

	private final NumberFormat PRETTY_FORMAT = NumberFormat.getInstance(Locale.US);

	private String displayCurrency(final BigDecimal value) {
		String currency = formatAsPrettyCurrency(value);
		String sign = "";

		if (value.signum() < 0) {
			currency = currency.substring(1);
			sign = "-";
		}

		return sign + "$" + currency;
	}

	private String formatAsPrettyCurrency(BigDecimal value) {
		String str = PRETTY_FORMAT.format(value);

		if (str.endsWith(".00")) {
			str = str.substring(0, str.length() - 3);
		}

		return str;
	}

	@Override
	public String currencyNameSingular() {
		return "Economy";
	}

	@Override
	public String currencyNamePlural() {
		return "Economy";
	}

	@Override
	public double getBalance(String playerName) {
		return getAccountBalance(playerName, null);
	}

	@Override
	public double getBalance(OfflinePlayer offlinePlayer) {
		return getAccountBalance(offlinePlayer.getName(), offlinePlayer.getUniqueId().toString());
	}

	private double getAccountBalance(String playerName, String uuid) {
		User user = economy.getUser(playerName);

		if (user == null) {
			return 0;
		}

		return user.getTotal().doubleValue();
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {
		return withdraw(playerName, null, amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
		return withdraw(offlinePlayer.getName(), offlinePlayer.getUniqueId().toString(), amount);
	}

	private EconomyResponse withdraw(String playerName, String uuid, double amount) {
		if (amount < 0) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds");
		}

		User user;
		if(uuid == null) {
			user = economy.getUser(playerName);
		} else { 
			user = economy.getUser(UUID.fromString(uuid), playerName);	
		}

		if (user == null) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account doesn't exist");
		}

		BigDecimal decimal = new BigDecimal(amount);

		user.remove(decimal);
		return new EconomyResponse(amount, user.getTotal().doubleValue(), ResponseType.SUCCESS, "");
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {
		return deposit(playerName, null, amount);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
		return deposit(offlinePlayer.getName(), offlinePlayer.getUniqueId().toString(), amount);
	}

	private EconomyResponse deposit(String playerName, String uuid, double amount) {
		if (amount < 0) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot deposit negative funds");
		}

		User user;
		if(uuid == null) {
			user = economy.getUser(playerName);
		} else { 
			user = economy.getUser(UUID.fromString(uuid), playerName);	
		}

		if (user == null) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "User doesn't exist");
		}

		user.add(new BigDecimal(amount));

		return new EconomyResponse(amount, user.getTotal().doubleValue(), ResponseType.SUCCESS, "");
	}

	@Override
	public boolean has(String playerName, double amount) {
		return getBalance(playerName) >= amount;
	}

	@Override
	public boolean has(OfflinePlayer offlinePlayer, double amount) {
		return getBalance(offlinePlayer) >= amount;
	}

	@Override
	public EconomyResponse createBank(String name, String player) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse createBank(String name, OfflinePlayer offlinePlayer) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse deleteBank(String name) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse bankHas(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse bankWithdraw(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse bankDeposit(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse isBankOwner(String name, String playerName) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse isBankOwner(String name, OfflinePlayer offlinePlayer) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse isBankMember(String name, String playerName) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse isBankMember(String name, OfflinePlayer offlinePlayer) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public EconomyResponse bankBalance(String name) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
	}

	@Override
	public List<String> getBanks() {
		return new ArrayList<String>();
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public boolean hasAccount(String playerName) {
		return true;
	}

	@Override
	public boolean hasAccount(OfflinePlayer offlinePlayer) {
		return true;
	}

	@Override
	public boolean createPlayerAccount(String playerName) {
		return createAccount(playerName, null);
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
		return createAccount(offlinePlayer.getName(), offlinePlayer.getUniqueId().toString());
	}

	private boolean createAccount(String playerName, String uuid) {
		if (hasAccount(playerName, uuid)) {
			return false;
		}

		if(uuid == null) {
			economy.getUser(playerName);
		} else { 
			economy.getUser(UUID.fromString(uuid), playerName);	
		}
		return true;
	}

	@Override
	public int fractionalDigits() {
		return -1;
	}

	@Override
	public boolean hasAccount(String playerName, String worldName) {
		return hasAccount(playerName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer offlinePlayer, String worldName) {
		return hasAccount(offlinePlayer);
	}

	@Override
	public double getBalance(String playerName, String worldName) {
		return getBalance(playerName);
	}

	@Override
	public double getBalance(OfflinePlayer offlinePlayer, String worldName) {
		return getBalance(offlinePlayer);
	}

	@Override
	public boolean has(String playerName, String worldName, double amount) {
		return has(playerName, amount);
	}

	@Override
	public boolean has(OfflinePlayer offlinePlayer, String worldName, double amount) {
		return has(offlinePlayer, amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
		return withdrawPlayer(playerName, amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String worldName, double amount) {
		return withdrawPlayer(offlinePlayer, amount);
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
		return depositPlayer(playerName, amount);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String worldName, double amount) {
		return depositPlayer(offlinePlayer, amount);
	}

	@Override
	public boolean createPlayerAccount(String playerName, String worldName) {
		return createPlayerAccount(playerName);
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String worldName) {
		return createPlayerAccount(offlinePlayer);
	}

	public class EconomyServerListener implements Listener {
		@EventHandler(priority = EventPriority.MONITOR)
		public void onPluginEnable(PluginEnableEvent event) {
			if (economy == null) {
				Plugin economy = Bukkit.getServer().getPluginManager().getPlugin("Economy");

				if (economy != null && economy.isEnabled()) {
					VaultRegister.this.economy = (me.bartvv.economy.Economy) economy;

					VaultRegister.this.economy.getLogger().log(Level.INFO, "Vault support enabled.");
				}
			}
		}

		@EventHandler(priority = EventPriority.MONITOR)
		public void onPluginDisable(PluginDisableEvent event) {
			if (economy != null) {
				if (event.getPlugin().getDescription().getName().equals(economy.getDescription().getName())) {
					economy = null;
					Bukkit.getLogger().info("[Economy] Vault support disabled.");
				}
			}
		}
	}
}