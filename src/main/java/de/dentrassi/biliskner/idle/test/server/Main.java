package de.dentrassi.biliskner.idle.test.server;

public class Main {
	public static void main(final String[] args) throws Exception {
		try (IdleServer server = new IdleServer(4242)) {
			server.sleep ();
		}
	}
}
