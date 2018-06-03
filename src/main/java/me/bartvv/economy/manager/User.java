package me.bartvv.economy.manager;

import java.math.BigDecimal;
import java.util.UUID;

import me.bartvv.economy.Economy;

public class User {

	private static final Economy ECONOMY = Economy.getPlugin(Economy.class);

	private transient BigDecimal money;
	private transient UUID uuid;
	private transient String name;
	private transient boolean changed = false;

	public User(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
		this.money = new BigDecimal(0);	

		ECONOMY.getData().load(this);
	}

	public void add(BigDecimal toAdd) {
		money = this.money.add(toAdd);
		changed = true;
	}

	public void remove(BigDecimal toRemove) {
		money = money.subtract(toRemove);
		changed = true;
	}

	public void set(BigDecimal money) {
		this.money = money;
		changed = true;
	}

	public BigDecimal getTotal() {
		return this.money;
	}

	public UUID getUniqueId() {
		return this.uuid;
	}

	public void save(Boolean async) {
		if (changed) {
			ECONOMY.getData().save(this.getUniqueId(), name, money, async);
		}
		changed = false;
	}
}