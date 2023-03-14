package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatKlijent implements Runnable {

	public static BufferedReader tokOdServera;
	public static PrintStream tokKaServeru;
	public static BufferedReader ulazKonzola;

	public static void main(String[] args) {

		try {
			
			Socket soketZaKomunikaciju = new Socket("localhost", 5000);

			tokOdServera = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			tokKaServeru = new PrintStream(soketZaKomunikaciju.getOutputStream());
			ulazKonzola = new BufferedReader(new InputStreamReader(System.in));

			new Thread(new ChatKlijent()).start();
			String porukaOdServera;

			while (true) {
				porukaOdServera = tokOdServera.readLine();
				System.out.println(porukaOdServera);

				if (porukaOdServera.startsWith("Aplikacija je zavrsena"))
					break;
			}

			soketZaKomunikaciju.close();
			
		} catch (UnknownHostException e) {
			System.out.println("Nepoznat host.");

		} catch (IOException e) {
			System.out.println("Veza sa serverom je prekinuta.");
		}
	}

	@Override
	public void run() {

		String porukaZaServer;

		try {
			while (true) {
				porukaZaServer = ulazKonzola.readLine();
				tokKaServeru.println(porukaZaServer);

				if (porukaZaServer.startsWith("Izlaz")) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}