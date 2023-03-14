package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

	public static void main(String[] args) {

		ServerSocket serverSoket = null;
		Socket soketZaKomunikaciju = null;
		
		try {
			
			serverSoket = new ServerSocket(5000);

			while (true) {
				System.out.println("Ceka se konekcija...");
				soketZaKomunikaciju = serverSoket.accept();
				System.out.println("Doslo je do konekcije.");
				KlijentNit klijentNit = new KlijentNit(soketZaKomunikaciju);
				klijentNit.start();
			}
			
		} catch (IOException e) {
			System.out.println("Doslo je do greske prilikom pokretanja servera!");
		}
	}
}