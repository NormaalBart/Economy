package me.bartvv.economy.message;

import me.bartvv.economy.Economy;

public enum Message {
	
	NO_PLAYER("noPlayer"), 
	BALANCE("balance"),
	PLAYER_NEVER_JOINED("playerNeverJoined"),
	GETTING_BALANCE("gettingBalance"),
	READ_NEXT_PAGE("readNextPage"),
	INFO_PAGES("infoPages"),
	ORDENING_BALANCES("ordeningBalances"),
	PAGE_NOT_FOUND("pageNotFound"),
	BALTOP_FORMAT("balTopFormat"),
	WRONG_PAY_FORMAT("wrongPayFormat"), 
	NOT_ENOUGN_MONEY("notEnoughMoney"),
	MONEY_RECEIVE("money.receive"),
	MONEY_PAY("money.pay"),
	MONEY_SET("eco.moneySet"),
	MONEY_TAKE("eco.moneyTake"),
	MONEY_GIVE("eco.moneyGive"),
	MONEY_SET_SENDER("eco.moneySetSender"),
	MONEY_TAKE_SENDER("eco.moneyTakeSender"),
	MONEY_GIVE_SENDER("eco.moneyGiveSender"),
	WRONG_USAGE_ECONOMY("eco.wrongFormat"),
	ERROR("error"),
	NO_UUID("no-uuid"), 
	NO_PERMISSIONS("noPermission"),
	MIGRATE_STARTING("migrate.starting"),
	MIGRATE_DONE("migrate.done"),
	MIGRATE_ERROR("migrate.error");
	
	private String message;
	
	Message(String message) {
		this.message = Economy.getMessages().getString(message);
	}
	
	@Override
	public String toString() {
		return this.message;
	}

}