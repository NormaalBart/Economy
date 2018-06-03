package me.bartvv.economy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Maps;

import net.md_5.bungee.api.ChatColor;

public class FileManager {

	private JavaPlugin javaPlugin;
	private File file;
	private YamlConfiguration configuration;
	private HashMap<String, Object> cache;
	private Integer unsavedChanges = 0;
	private Integer maxUnsavedChanges = 10;

	@Deprecated
	public FileManager(JavaPlugin javaPlugin, String name) {
		new FileManager(javaPlugin, name, 10);
	}

	public FileManager(JavaPlugin javaPlugin, String name, Integer maxUnsavedChanges) {
		Validate.notNull(javaPlugin, "JavaPlugin cannot be null");
		Validate.notNull(name, "Name cannot be null!");
		Validate.notNull(maxUnsavedChanges, "unsaved changes cannot be null!");
		if (!name.endsWith(".yml"))
			name = name + ".yml";

		this.javaPlugin = javaPlugin;
		this.file = new File(this.javaPlugin.getDataFolder(), name);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();

		this.cache = Maps.newHashMap();
		if (!file.exists())
			this.javaPlugin.saveResource(name, false);

		this.configuration = YamlConfiguration.loadConfiguration(this.file);
	}

	public List<String> getStringList(String path) {
		return getStringList(path, false);
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String path, Boolean load) {
		List<String> str;
		if (load) {
			str = getConfig().getStringList(path);
			cache.put(path, str);
		} else {
			if (cache.get(path) != null && cache.get(path) instanceof List<?>) {
				str = (List<String>) cache.get(path);
			} else {
				str = getStringList(path, true);
			}
		}
		if (str != null) {
			for (int i = 0; i < str.size(); i++) {
				str.set(i, ChatColor.translateAlternateColorCodes('&', str.get(i)));
			}
		}
		return str;
	}

	public void set(String path, Object obj) {
		set(path, obj, false);
	}

	public void set(String path, Object obj, Boolean save) {
		configuration.set(path, obj);

		if (save) {
			if (save()) {
				unsavedChanges = 0;
			} else {
				javaPlugin.getLogger().log(Level.WARNING,
						"Config could not be saved for plugin: " + javaPlugin.getName() + "!");
				javaPlugin.getLogger().log(Level.WARNING, "Please contact plugin owner to fix this! ");
				javaPlugin.getLogger().log(Level.WARNING, "Also include the stacktrace shown above!");
			}
		} else {
			unsavedChanges++;
			if (unsavedChanges >= maxUnsavedChanges) {
				if (save()) {
					unsavedChanges = 0;
				} else {
					javaPlugin.getLogger().log(Level.WARNING,
							"Config could not be saved for plugin: " + javaPlugin.getDescription().getName()
									+ "! (Version: " + javaPlugin.getDescription().getVersion() + ")");
					javaPlugin.getLogger().log(Level.WARNING, "Please contact plugin owner ("
							+ javaPlugin.getDescription().getAuthors().toString().replace("[", "").replace("]", "")
							+ ") to fix this! ");
					javaPlugin.getLogger().log(Level.WARNING, "Also include the stacktrace shown above!");
				}
			}
		}
	}

	public String getString(String path) {
		return getString(path, false, null);
	}

	public String getString(String path, Boolean load) {
		return getString(path, load, null);
	}

	public String getString(String path, Boolean load, String def) {
		Validate.notNull(path, "Path cannot be null");
		if (load) {
			def = configuration.getString(path);
			cache.put(path, def);
		} else {
			Object obj = cache.get(path);
			if (obj != null && obj instanceof String) {
				def = (String) obj;
			} else {
				def = getString(path, true);
			}
		}
		return def == null ? def : ChatColor.translateAlternateColorCodes('&', def);
	}

	public Location getLocation(String path) {
		return getLocation(path, false);
	}

	public Location getLocation(String path, Boolean load) {
		Validate.notNull(path, "Path cannot be null");
		Location loc = null;
		Object obj;
		if (load) {
			obj = get(path);
			if (obj != null && obj instanceof Location) {
				cache.put(path, obj);
				loc = (Location) obj;
			}
		} else {
			obj = cache.get(path);
			if (obj != null && obj instanceof Location) {
				loc = (Location) obj;
			} else {
				obj = get(path);
				if (obj != null && obj instanceof Location) {
					cache.put(path, obj);
					loc = (Location) obj;
				}
			}
		}
		return loc;
	}

	public Integer getInteger(String path) {
		return getInteger(path, false, -1);
	}

	public Integer getInteger(String path, Boolean load) {
		return getInteger(path, load, -1);
	}

	public Integer getInteger(String path, Boolean load, Integer def) {
		Validate.notNull(path, "Path cannot be null");
		if (load) {
			def = configuration.getInt(path, -1);
			cache.put(path, def);
		} else {
			Object obj = cache.get(path);
			if (obj != null && obj instanceof Integer) {
				def = (Integer) obj;
			} else {
				def = getInteger(path, true);
			}
		}
		return def;
	}

	public Double getDouble(String path) {
		return getDouble(path, false, -1.0D);
	}

	public Double getDouble(String path, Boolean load) {
		return getDouble(path, load, -1.0D);
	}

	public Double getDouble(String path, Boolean load, Double def) {
		Validate.notNull(path, "Path cannot be null");
		if (load) {
			def = configuration.getDouble(path, -1.0D);
			cache.put(path, def);
		} else {
			Object obj = cache.get(path);
			if (obj != null && obj instanceof Double) {
				def = (Double) obj;
			} else {
				def = getDouble(path, true);
			}
		}
		return def;
	}

	public Object get(String path) {
		return get(path, false);
	}

	public Object get(String path, Boolean load) {
		Validate.notNull(path, "Path cannot be null");
		Object obj;
		if (load) {
			obj = configuration.get(path);
		} else {
			obj = cache.get(path);
			if (obj == null) {
				obj = get(path, true);
				cache.put(path, obj);
			}
		}
		return obj;
	}

	public List<?> getList(String path) {
		return getList(path, false, null);
	}

	public List<?> getList(String path, Boolean load) {
		return getList(path, load, null);
	}

	public List<?> getList(String path, Boolean load, List<?> def) {
		Validate.notNull(path, "Path cannot be null");
		if (load) {
			def = configuration.getList(path);
			cache.put(path, def);
		} else {
			Object obj = cache.get(path);
			if (obj != null && obj instanceof List<?>) {
				def = (List<?>) obj;
			} else {
				def = getList(path, true);
			}
		}
		return def;
	}

	public Boolean getBoolean(String path) {
		return getBoolean(path, false, false);
	}

	public Boolean getBoolean(String path, Boolean load) {
		return getBoolean(path, load, false);
	}

	public Boolean getBoolean(String path, Boolean load, Boolean def) {
		Validate.notNull(path, "Path cannot be null");
		if (load) {
			def = configuration.getBoolean(path);
			cache.put(path, def);
		} else {
			Object obj = cache.get(path);
			if (obj != null && obj instanceof Boolean) {
				def = (Boolean) obj;
			} else {
				def = getBoolean(path, true);
			}

		}
		return def;
	}

	public ItemStack getItemStack(String path) {
		return getItemStack(path, false);
	}

	public ItemStack getItemStack(String path, Boolean load) {
		Validate.notNull(path, "Path cannot be null");
		ItemStack itemStack;

		if (load) {
			itemStack = configuration.getItemStack(path);
			cache.put(path, itemStack);
		} else {
			Object obj = cache.get(path);
			if (obj != null && obj instanceof ItemStack) {
				itemStack = (ItemStack) obj;
			} else {
				itemStack = getItemStack(path, true);
			}

		}
		return itemStack;
	}

	public ConfigurationSection getSection(String path) {
		return getSection(path, false);
	}

	public ConfigurationSection getSection(String path, Boolean load) {
		Validate.notNull(path, "Path cannot be null");
		ConfigurationSection section;
		if (load) {
			section = configuration.getConfigurationSection(path);
			cache.put(path, section);
		} else {
			Object obj = cache.get(path);
			if (obj != null && obj instanceof ConfigurationSection) {
				section = (ConfigurationSection) obj;
			} else {
				section = getSection(path, true);
			}
		}
		return section;
	}

	public YamlConfiguration getConfig() {
		return configuration;
	}

	public void resetCache(Boolean save) {
		if (save)
			this.save();
		cache.clear();
	}

	public boolean save() {
		try {
			if (unsavedChanges != 0) {
				configuration.save(new File(this.file.getParentFile().getName(), this.file.getName()));
				unsavedChanges = 0;
				cache.clear();
			}
			return true;
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
			return false;
		}
	}
}