package me.bartvv.economy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public class Utils {

	private final static NumberFormat PRETTY_FORMAT = NumberFormat.getInstance(Locale.US);
	private static String moneySign = "$";

	static {
		PRETTY_FORMAT.setRoundingMode(RoundingMode.FLOOR);
		PRETTY_FORMAT.setGroupingUsed(true);
		PRETTY_FORMAT.setMinimumFractionDigits(2);
		PRETTY_FORMAT.setMaximumFractionDigits(2);

		FileManager options = Economy.getOptions();
		moneySign = options.getString("moneySign");
	}

	public static String displayCurrency(final BigDecimal value) {
		String currency = formatAsPrettyCurrency(value);
		String sign = "";

		if (value.signum() < 0) {
			currency = currency.substring(1);
			sign = "-";
		}

		return sign + moneySign + currency;
	}

	private static String formatAsPrettyCurrency(BigDecimal value) {
		String str = PRETTY_FORMAT.format(value);

		if (str.endsWith(".00")) {
			str = str.substring(0, str.length() - 3);
		}

		return str;
	}
	
	public static UUID getUUIDFromString(String string) {
		UUID uuid;
		
		try {
			uuid = UUID.fromString(string);
		}catch(Exception exc) {
			uuid = UUID.nameUUIDFromBytes(("Towny: " + string).getBytes());
		}
		return uuid;
	}

}