package at.ac.uibk.plant_health.util;

public class StringGenerator {
	private final static String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private final static String SMALL_LETTERS = "abcdefghijklmnopqrstuvwxyz";
	private final static String NUMBERS = "0123456789";

	public static String base(String alphabet, int n) {
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			int index = (int) (Math.random() * alphabet.length());
			sb.append(alphabet.charAt(index));
		}
		return sb.toString();
	}

	public static String macAddress() {
		int macAddressLength = 17;
		return StringGenerator.base(SMALL_LETTERS + CAPITAL_LETTERS + NUMBERS, macAddressLength);
	}
	public static String username() {
		int usernameLength = 30;
		return "username-"
				+ StringGenerator.base(SMALL_LETTERS + CAPITAL_LETTERS + NUMBERS, usernameLength);
	}

	public static String email() {
		int lengthLocalPart = 20;
		int lengthDomainPart = 8;
		String localPart =
				StringGenerator.base(SMALL_LETTERS + CAPITAL_LETTERS + NUMBERS, lengthLocalPart);
		String domainPart = StringGenerator.base(SMALL_LETTERS, lengthDomainPart) + ".com";
		return localPart + "@" + domainPart;
	}

	public static String password() {
		int passwordLength = 30;
		return StringGenerator.base(SMALL_LETTERS + CAPITAL_LETTERS + NUMBERS, passwordLength);
	}

	public static String deckName() {
		int deckNameLength = 30;
		return "Random deck name: "
				+ StringGenerator.base(SMALL_LETTERS + CAPITAL_LETTERS, deckNameLength);
	}

	public static String deckDescription() {
		int deckDescriptionLength = 50;
		return "Random deck description: "
				+ StringGenerator.base(
						SMALL_LETTERS + CAPITAL_LETTERS + NUMBERS + " ", deckDescriptionLength
				);
	}

	public static String cardText() {
		int cardTextLength = 250;
		return "Random card text: "
				+ StringGenerator.base(
						SMALL_LETTERS + CAPITAL_LETTERS + NUMBERS + " ", cardTextLength
				);
	}
}
