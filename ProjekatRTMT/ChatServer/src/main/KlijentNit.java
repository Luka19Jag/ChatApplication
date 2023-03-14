package main;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class KlijentNit extends Thread {

	BufferedReader tokOdKlijenta;
	PrintStream tokKaKlijentu;
	Socket soketZaKomunikaciju;

	String putanjaKaFolderu = "C:\\Users\\Zbook G3\\eclipse-workspace\\ChatServer\\";

	String ime;
	String sifra;
	boolean premiumKorisnik = false;
	int brojac = 0;

	public KlijentNit(Socket soketZaKomunikaciju) {
		this.soketZaKomunikaciju = soketZaKomunikaciju;
	}

	// pocetna funkcija
	private void meni() throws Exception {
		String izborZaMeni = null;

		tokKaKlijentu
				.println("\nDobrodosli u meni\n\n" + "1)Registracija\n" + "2)Prijava\n" + "3)Uneti link za deljenje\n"
						+ "Za izlaz u bilo kom trenutku, uneti ' Izlaz '.\n\n" + "Ukucajte broj koji zelite:");

		try {
			izborZaMeni = tokOdKlijenta.readLine();
		} catch (IOException e) {
			System.out.println("Veza sa klijentom se prekinula...");
		}

		switch (izborZaMeni) {
		case "1":
			registracija();
			break;
		case "2":
			prijavljivanje();
			break;
		case "3":
			linkZaDeljenje();
			meni();
			break;
		case "Izlaz":
			prekiniKomunikaciju();
			break;
		default:
			tokKaKlijentu.println("Izabrali ste nemoguce.\n");
			meni();
		}
	}

	// repna rekurzija
	// funkcija broji broj fajlova radi daljeg koriscenja za obicne i premium
	// korisnike
	private void brojiFajlove(BufferedReader tokOdKlijenta, PrintStream tokKaKlijentu, File[] nizFajlova, int indeks,
			int nivo) {
		// iskljucujuci uslov
		if (indeks == nizFajlova.length)
			return;

		// za fajlove
		if (nizFajlova[indeks].isFile())
			brojac++;

		// za poddirektorijume
		else if (nizFajlova[indeks].isDirectory()) {
			brojiFajlove(tokOdKlijenta, tokKaKlijentu, nizFajlova[indeks].listFiles(), 0, nivo + 1);
		}

		// rekurzija za glavni direktorijum
		brojiFajlove(tokOdKlijenta, tokKaKlijentu, nizFajlova, ++indeks, nivo);
	}

	// funkcija za postavljanje fajlova na server
	private void uploadujFajlove() {
		tokKaKlijentu.println("Napomena: Obicni korisnici mogu da postave maksimalno 5 fajlova na server.");
		try {
			izvrsiBrojanje(this.ime);
			// uslov za max 5 fajlova obicni korisnici ili neograniceno premium
			

				//byte[] nizBajtova = ucitajBajtoveIzFajla(imeFajla);
				
				
				tokKaKlijentu.println("Unesi punu putanju");
				String krajnjaPutanja = tokOdKlijenta.readLine();
				tokKaKlijentu.println("Molim Vas, unesite ime fajla:");
				String imeFajla = tokOdKlijenta.readLine();
				if (imeFajla.startsWith("Izlaz")) {
					prekiniKomunikaciju();
				}
				//Path krajnjaZaFajl = Paths.get(krajnjaPutanja);
				//Files.write(putanjaKaFolderu + "RegistrovaniKorisnici\\" + this.ime + "\\" + imeFajla, nizBajtova);
				
				//Path putanja = Paths.get(putanjaKaFolderu + "RegistrovaniKorisnici\\" + this.ime + "\\" + imeFajla);
				//Files.write(putanja, nizBajtova);
				
				byte[] bFile = ucitajBajtoveIzFajla(krajnjaPutanja + "\\" + imeFajla);
				Path putanja = Paths.get(putanjaKaFolderu + "RegistrovaniKorisnici" + "\\" + this.ime+ "\\" + imeFajla);
				Files.write(putanja, bFile);

				//tokKaKlijentu.println("Fajl je uspesno postavljen na server\n");

				// for (int i = 0; i < nizBajtova.length; i++)
				// tokKaKlijentu.println((char) nizBajtova[i]);

			//} else
				//tokKaKlijentu.println("Kao obican korisnik, postavili ste maksimalnih 5 fajlova.");
		} catch (IOException e) {
			System.out.println("Doslo je do prekida sa vezom.");
		}
}


	private void linkZaDeljenje() {
		String korisnik = null;
		try {
			boolean daLiJeValidno = false, usernamePostoji = false;
			do {
				tokKaKlijentu.println("Molim Vas, unesite link:");
				tokKaKlijentu.println("Napomena: format linka je sledeci: ' www.usernameDrajv.rmt '");
				String link = tokOdKlijenta.readLine();
				if (link.startsWith("Izlaz"))
					prekiniKomunikaciju();
				if (!link.contains("Drajv.rmt") || !link.contains("www.")) {
					tokKaKlijentu.println("Uneti link nije u dobrom formatu. Format linka: (www.usernameDrajv.rmt)");
				} else {
					korisnik = link.substring(4, link.indexOf("Drajv.rmt"));
					BufferedReader br = new BufferedReader(
							new FileReader(putanjaKaFolderu + "RegistrovaniKorisnici\\podaci.txt"));
					String linijaOdPostojecihKorisnika = br.readLine();
					while ((linijaOdPostojecihKorisnika != null)) {
						String[] informacijeOdPostojecihKorisnika = linijaOdPostojecihKorisnika.split(",");
						String usernameOdPostojecihKorisnika = informacijeOdPostojecihKorisnika[0];

						if (usernameOdPostojecihKorisnika.equals(korisnik)) {
							usernamePostoji = true;
							break;
						}

						linijaOdPostojecihKorisnika = br.readLine();
					}

					br.close();

					if (usernamePostoji) {
						daLiJeValidno = true;
						izvrsiListFiles(korisnik);
					} else {
						tokKaKlijentu.println("\nKorisnicko ime ne postoji.");
					}
				}

			} while (!daLiJeValidno);

			try {
				tokKaKlijentu.println("Da li zelite da skinete neki od foldera/fajla(da/ne)");
				String odgovor = tokOdKlijenta.readLine();
				if (odgovor.startsWith("Izlaz")) {
					prekiniKomunikaciju();
				}
				if (odgovor.equalsIgnoreCase("ne")) {
					tokKaKlijentu.println("\nZavrsili ste opciju deljenja linka.\n");
				}
				if (!odgovor.equalsIgnoreCase("da") && !odgovor.equalsIgnoreCase("ne")) {
					tokKaKlijentu.println("\nNemoguc izbor. Molim Vas, pokusajte ponovo.");
				}
				if (odgovor.equalsIgnoreCase("da")) {
					tokKaKlijentu.println("Molim Vas, unesite putanju do foldera:");
					tokKaKlijentu.println("Napomena: pritisnite samo ' enter ', ako je to Vasa jedina putanja");
					String putanjaDoFoldera = tokOdKlijenta.readLine();
					if (putanjaDoFoldera.startsWith("Izlaz")) {
						prekiniKomunikaciju();
					}
					tokKaKlijentu.println("Molim Vas, unesite ime fajla:");
					String imeFajla = tokOdKlijenta.readLine();
					if (imeFajla.startsWith("Izlaz")) {
						prekiniKomunikaciju();
					}
					byte[] bFile = ucitajBajtoveIzFajla(putanjaKaFolderu + "RegistrovaniKorisnici\\" + korisnik + "\\"
							+ putanjaDoFoldera + "\\" + imeFajla);

					Path putanja = Paths.get("C:\\Users\\Zbook G3\\Desktop\\ZaSkidanje\\" + imeFajla);

					Files.write(putanja, bFile);
					tokKaKlijentu.print("\n\n**Pocetak**\n\n");

					// za Base64
					StringBuilder sb = new StringBuilder();

					for (int i = 0; i < bFile.length; i++) {
						sb.append((char) bFile[i]);
					}
					if (imeFajla.contains("txt")) {
						for (int i = 0; i < bFile.length; i++) {
							tokKaKlijentu.print((char) bFile[i]);
						}
					} else {
						String string = sb.toString();
						Base64.Encoder encoder = Base64.getEncoder();
						byte[] nizBajtova = encoder.encode(string.getBytes());
						for (int i = 0; i < nizBajtova.length; i++) {
							tokKaKlijentu.printf("%c", (char) nizBajtova[i]);
							if (i != 0 && i % 4 == 0) {
								tokKaKlijentu.println(" ");
							}
						}
					}
					tokKaKlijentu.println();
					tokKaKlijentu.print("\n\n**Gotovo je**\n\n");

				}

			} catch (IOException e) {
				System.out.println("Greska u deljenju linka.");
			}

		} catch (IOException e) {
			tokKaKlijentu.println("Greska u deljenju linka.");
		}

	}

	// funkcija koja vraca niz bajtova za neki fajl
	private static byte[] ucitajBajtoveIzFajla(String filePath) {

		FileInputStream fileInputStream = null;
		byte[] bytesArray = null;

		try {

			File file = new File(filePath);
			bytesArray = new byte[(int) file.length()];

			// procitaj fajl u bajtovima
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytesArray);

		} catch (IOException e) {
			System.out.println("Greska u funkciji ucitajBajtoveIzFajla");
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					System.out.println("Greska u funkciji ucitajBajtoveIzFajla");
				}
			}

		}
		return bytesArray;
	}

	private String izgenerisiLink() {
		return "www." + this.ime + "Drajv" + ".rmt";
	}

	private void meniZaKorisnikeKojiSuUlogovani() throws Exception {
		String izbor = null;

		if (premiumKorisnik == true) {
			tokKaKlijentu.println("Ulogovani ste kao: ' " + this.ime + " (Premium korisnik)" + " '\n");
			tokKaKlijentu.println("Dobrodosli u meni za korisnike koji su ulogovani (Premium korisnik) \n\n"
					+ "1)Izlistajte moje fajlove\n" + "2)Postavite datoteku na server\n"
					+ "3)Diskovi koji su deljeni sa Vama\n" + "4)Upravljajte direktorijumima\n"
					+ "5)Izaberite datoteku za prikaz\n" + "6)Podelite sa Vasim prijateljem\n" + "7)Generisi link\n"
					+ "8)Izlogujte se\n" + "Za izlaz u bilo kom trenutku, uneti ' Izlaz '.\n\n"
					+ "Ukucajte broj koji zelite: ");
		} else {
			tokKaKlijentu.println("Ulogovani ste kao: ' " + this.ime + " (Obican korisnik)" + " '\n");
			tokKaKlijentu.println("Dobrodosli u meni za korisnike koji su ulogovani (Obican korisnik) \n\n"
					+ "1)Izlistajte moje fajlove\n" + "2)Postavite datoteku na server\n"
					+ "3)Diskovi koji su deljeni sa Vama\n" + "4)Izaberite datoteku za prikaz\n"
					+ "5)Podelite sa Vasim prijateljem\n" + "6)Generisi link\n" + "7)Izlogujte se\n"
					+ "Za izlaz u bilo kom trenutku, uneti ' Izlaz '.\n\n" + "Ukucajte broj koji zelite: ");
		}
		try {
			izbor = tokOdKlijenta.readLine();
		} catch (IOException e) {
			System.out.println("");
		}

		if (premiumKorisnik == true) {
			switch (izbor) {
			case "1":
				izvrsiListFiles(this.ime);
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "2":
				uploadujFajlove();
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "3":
				podeljeniDrajvovi();
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "4":
				upravljajDirektorijumima();
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "5":
				otvoriZeljeniFajl();
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "6":
				podeliSaKorisnikom();
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "7":
				izgenerisiLink();
				tokKaKlijentu.println(izgenerisiLink());// ?
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "8":
				izlogujSe();
				break;
			case "Izlaz":
				prekiniKomunikaciju();
				break;
			default:
				tokKaKlijentu.println("Nemoguc izbor!\n");
				meniZaKorisnikeKojiSuUlogovani();
			}
		} else {
			switch (izbor) {
			case "1":
				izvrsiListFiles(this.ime);
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "2":
				uploadujFajlove();
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "3":
				podeljeniDrajvovi();
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "4":
				otvoriZeljeniFajl();
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "5":
				podeliSaKorisnikom();
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "6":
				izgenerisiLink();
				tokKaKlijentu.println(izgenerisiLink());
				meniZaKorisnikeKojiSuUlogovani();
				break;
			case "7":
				izlogujSe();
				break;
			case "Izlaz":
				prekiniKomunikaciju();
				break;
			default:
				tokKaKlijentu.println("Nemoguc izbor!\n");
				meniZaKorisnikeKojiSuUlogovani();
			}
		}
	}

	private void podeljeniDrajvovi() throws IOException {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(putanjaKaFolderu + "RegistrovaniKorisnici\\sharedLinks.txt"));
			String linijaOdPostojecihKorisnika = br.readLine();
			boolean usernamPostoji = false;
			while ((linijaOdPostojecihKorisnika != null)) {
				String[] informacijeOdPostojecihKorisnika = linijaOdPostojecihKorisnika.split(",");
				String komeJeSerovano = informacijeOdPostojecihKorisnika[0];
				String korisnik = informacijeOdPostojecihKorisnika[1].substring(4,
						informacijeOdPostojecihKorisnika[1].indexOf("Drajv.rmt"));

				if (komeJeSerovano.equals(this.ime)) {
					usernamPostoji = true;
					tokKaKlijentu.println("Drajv od " + korisnik + ", sa deljenim linkom "
							+ informacijeOdPostojecihKorisnika[1] + "\n");
					izvrsiListFiles(korisnik);
				}

				linijaOdPostojecihKorisnika = br.readLine();
			}

			if (!usernamPostoji)
				tokKaKlijentu.println("Nemate serovane drajvove.");

			br.close();

		} catch (FileNotFoundException e) {
			System.out.println("Doslo je do greske u podeljenim drajvovima");
		}

	}

	private void podeliSaKorisnikom() {
		boolean usernamePostoji = false, daLiJeValidan = false;
		try {
			do {
				tokKaKlijentu.println("Molim Vas, unesite ime korisnika sa kojim zelite da podelite Vas drajv:");
				String korisnikKomeCeSerovati = tokOdKlijenta.readLine();
				if (korisnikKomeCeSerovati.equals("Izlaz"))
					prekiniKomunikaciju();
				BufferedReader br = new BufferedReader(
						new FileReader(putanjaKaFolderu + "RegistrovaniKorisnici\\podaci.txt"));
				String linijaOdPostojecihKorisnika = br.readLine();

				while ((linijaOdPostojecihKorisnika != null)) {
					String[] informacijeOdPostojecihKorisnika = linijaOdPostojecihKorisnika.split(",");
					String usernameOdPostojecihKorisnika = informacijeOdPostojecihKorisnika[0];

					if (usernameOdPostojecihKorisnika.equals(korisnikKomeCeSerovati)) {
						usernamePostoji = true;
						break;
					}

					linijaOdPostojecihKorisnika = br.readLine();
				}

				br.close();

				if (usernamePostoji) {
					daLiJeValidan = true;
					// pravi se novi fajl sa podacima
					// upisuje se za svaki share
					String imeFajla = putanjaKaFolderu + "RegistrovaniKorisnici\\sharedLinks.txt";
					FileWriter fw = new FileWriter(imeFajla, true);
					String link = "www." + this.ime + "Drajv.rmt";
					tokKaKlijentu.println(link);
					fw.write(korisnikKomeCeSerovati + "," + link + "\n"); // umesto this.username treba ici link koga
																			// trebam procitati iz podaci.txt
					fw.close();
				} else {
					tokKaKlijentu.println("\nKorisnicko ime koje ste uneli ne postoji.");
				}
			} while (!daLiJeValidan);

		} catch (IOException e) {
			System.out.println("Doslo je do greske prilikom deljenja fajla.");
		}
	}

	// otvara preko Desktopa
	private void otvoriZeljeniFajl() {
		String odgovor, putanjaDoFajla;
		try {

			boolean daLiJeValidan = false;
			do {
				tokKaKlijentu.println("Molim Vas, unesite korisnicko ime ciji direktorijum/fajl zelite da otvorite");
				tokKaKlijentu.println("Napomena: ako zelite Vase fajlove, molim Vas unesite Vase ime.");
				odgovor = tokOdKlijenta.readLine();
				if (odgovor.equals("Izlaz"))
					prekiniKomunikaciju();
				BufferedReader br = new BufferedReader(
						new FileReader(putanjaKaFolderu + "RegistrovaniKorisnici\\podaci.txt"));
				String linijaOdPostojecihKorisnika = br.readLine();

				boolean usernamePostoji = false;
				while ((linijaOdPostojecihKorisnika != null)) {
					String[] informacijeOdPostojecihKorisnika = linijaOdPostojecihKorisnika.split(",");
					String usernameOdPostojecihKorisnika = informacijeOdPostojecihKorisnika[0];

					if (usernameOdPostojecihKorisnika.equals(odgovor)) {
						usernamePostoji = true;
						daLiJeValidan = true;
						break;
					}

					linijaOdPostojecihKorisnika = br.readLine();
				}

				br.close();

				if (!usernamePostoji)
					tokKaKlijentu.println("Korisnik kojeg ste uneli ne postoji. Molim Vas, pokusajte ponovo...");
				else {
					boolean nepostojeciKorisnik = false;
					BufferedReader citajSharedLinks = new BufferedReader(
							new FileReader(putanjaKaFolderu + "RegistrovaniKorisnici\\sharedLinks.txt"));
					String linija = citajSharedLinks.readLine();
					while ((linija != null)) {
						String[] informacije = linija.split(",");
						String korisnik = informacije[0];
						String ceoLink = informacije[1];
						String imeKoJeSerovao = ceoLink.substring(4, ceoLink.indexOf("Drajv.rmt"));

						if ((odgovor.equals(imeKoJeSerovao) && korisnik.equals(this.ime)) || odgovor.equals(this.ime)) {
							nepostojeciKorisnik = true;
							izvrsiListFiles(odgovor);
							tokKaKlijentu.println("\nMolim Vas, unesite putanju fajla/direktoijuma:");
							tokKaKlijentu.println(
									"Napomena: ne unosite ime Vaseg direktorijuma ili ime korisnika koji Vam je serovao fajlove"
											+ ", vec sve ispod njega(\\Folder\\Fajl.extenzija).");
							putanjaDoFajla = tokOdKlijenta.readLine();
							if (putanjaDoFajla.equals("Izlaz"))
								prekiniKomunikaciju();
							File otvoriFajl = new File(
									putanjaKaFolderu + "RegistrovaniKorisnici\\" + odgovor + "\\" + putanjaDoFajla);
							Desktop desktop = Desktop.getDesktop();
							if (otvoriFajl.exists()) {
								desktop.open(otvoriFajl);
								break;
							} else {
								tokKaKlijentu.println("Putanja koju ste uneli nije dobra.");
								break;
							}
						}
						linija = citajSharedLinks.readLine();

					}
					if (nepostojeciKorisnik == false)
						tokKaKlijentu.println("Korisnik " + odgovor + " Vam nije serovao nista.");
					citajSharedLinks.close();
				}
				break;
			} while (!daLiJeValidan);
		} catch (IOException e) {
			tokKaKlijentu.println("Greska prilikom otvaranja fajlova.");
		}
	}

	// glavna za izlistavanje fajlova
	private void izvrsiListFiles(String korisnik) {
		// omoguci put do foldera
		String glavniDirektorijumPut = putanjaKaFolderu + "RegistrovaniKorisnici\\" + korisnik;

		// Fajl objekat
		File glavniDirektorijum = new File(glavniDirektorijumPut);

		if (glavniDirektorijum.exists() && glavniDirektorijum.isDirectory()) {
			// listFiles() vec postoji funkcija
			File niz[] = glavniDirektorijum.listFiles();

			tokKaKlijentu.println("Fajlovi od '" + korisnik + "' direkorijuma: \n");
			tokKaKlijentu.println("**Folderi/Fajlovi:");

			// pozovi rekurzivnu metodu
			listFiles(tokOdKlijenta, tokKaKlijentu, niz, 0, 0);
		}
	}

	// glavna za brojanje faklova
	private void izvrsiBrojanje(String korisnik) {
		// omoguci put do foldera
		String glavniDirektorijumPut = putanjaKaFolderu + "RegistrovaniKorisnici\\" + korisnik;

		// Fajl objekat
		File glavniDirektorijum = new File(glavniDirektorijumPut);

		if (glavniDirektorijum.exists() && glavniDirektorijum.isDirectory()) {
			// listFiles() funkcija koja vec postoji
			File niz[] = glavniDirektorijum.listFiles();

			// osiguraj da je 0, ako je vec bilo brojanje
			brojac = 0;

			// pozovi rekurzivnu metodu
			brojiFajlove(tokOdKlijenta, tokKaKlijentu, niz, 0, 0);
		}
	}

	private void upravljajDirektorijumima() throws Exception {
		String izbor = null;

		tokKaKlijentu.println("Ulogovani ste kao: ' " + this.ime + " '\n");

		tokKaKlijentu.println("Dobrodosli u meni za upravljanje direktorijuma\n\n" + "1)Napraviti direktorijum\n"
				+ "2)Preimenovati direktorijum\n" + "3)Pomeriti fajl u drugi direktorijum\n"
				+ "4)Izbrisati direktorijum\n" + "5)Vrati se u meni za ulogovane korisnike\n" + "6)Izloguj se\n"
				+ "Za izlaz u bilo kom trenutku, uneti ' Izlaz '.\n\n" + "Ukucajte broj koji zelite:");

		try {
			izbor = tokOdKlijenta.readLine();
		} catch (IOException e) {
			System.out.println("");
		}

		switch (izbor) {
		case "1":
			kreirajDirektorijum();
			meniZaKorisnikeKojiSuUlogovani();
			break;
		case "2":
			preimenujDirektorijum();
			meniZaKorisnikeKojiSuUlogovani();
			break;
		case "3":
			premestiFajl();
			meniZaKorisnikeKojiSuUlogovani();
			break;
		case "4":
			izbrisiDirektorijum();
			meniZaKorisnikeKojiSuUlogovani();
			break;
		case "5":
			meniZaKorisnikeKojiSuUlogovani();
			break;
		case "6":
			izlogujSe();
			break;
		case "Izlaz":
			prekiniKomunikaciju();
			break;
		default:
			tokKaKlijentu.println("Izbor nije moguc.\n");
			meniZaKorisnikeKojiSuUlogovani();
		}
	}

	private void izbrisiDirektorijum() {
		tokKaKlijentu.println("\nMolim Vas, unesite putanju foldera koji zelite da obrisete(\\\\Folder\\\\Folder):");
		tokKaKlijentu
				.println("\nNapomena: ne treba da unosite vas Folder, ako nemate putanju samo pritisnite ' enter '");
		try {
			String putanja = tokOdKlijenta.readLine();
			if (putanja.equals("Izlaz"))
				prekiniKomunikaciju();
			tokKaKlijentu.println("\nMolim Vas, unesite naziv foldera koji zelite da obrisete:");
			String imeFolderaZaBrisanje = tokOdKlijenta.readLine();
			if (imeFolderaZaBrisanje.equals("Izlaz"))
				prekiniKomunikaciju();
			File folderZaBrisanje = new File(putanjaKaFolderu + "RegistrovaniKorisnici\\" + this.ime + "\\" + putanja
					+ "\\" + imeFolderaZaBrisanje);

			// delete() funkcija koja vec postoji
			if (folderZaBrisanje.delete())
				tokKaKlijentu.println("\nFolder je uspesno obrisan.");
			else
				tokKaKlijentu.println("\nFolder nije uspesno obrisan.");

		} catch (IOException e) {
			System.out.println("Greska u brisanju direktorijuma");
		}
	}

	private void premestiFajl() {

		try {
			tokKaKlijentu.println(
					"\nMolim Vas, unesite putanju do fajla/foldera koji zelite da premestite(Folder\\\\Folder):");
			tokKaKlijentu.println(
					"Napomena: ne unositi ime Vaseg foldera, vec samo ' enter ', ako je to Vasa jedina putanja");
			String izvornaPutanja = tokOdKlijenta.readLine();
			if (izvornaPutanja.equals("Izlaz"))
				prekiniKomunikaciju();
			tokKaKlijentu.println("\nMolim Vas, unesite ime fajla/foldera koji zelite da premestite: ");
			String imeFF = tokOdKlijenta.readLine();
			if (imeFF.equals("Izlaz"))
				prekiniKomunikaciju();
			tokKaKlijentu.println(
					"\nMolim Vas, unesite putanju do novog foldera gde zelite da premestite fajl/folder(TvojFolder\\\\Folder):");
			tokKaKlijentu.println(
					"Napomena: ne unositi ime Vaseg foldera, vec samo ' enter ', ako je to Vasa jedina putanja");
			String novaPutanja = tokOdKlijenta.readLine();
			if (novaPutanja.equals("Izlaz"))
				prekiniKomunikaciju();
			File izvorniFajl = new File(
					putanjaKaFolderu + "RegistrovaniKorisnici\\" + this.ime + "\\" + izvornaPutanja + "\\" + imeFF);
			File destinacioniFajl = new File(
					putanjaKaFolderu + "RegistrovaniKorisnici\\" + this.ime + "\\" + novaPutanja + "\\" + imeFF);

			// renameTo() funkcija koja vec postoji
			if (izvorniFajl.renameTo(destinacioniFajl)) {
				tokKaKlijentu.println("Fajl/folder je uspesno premesten.");
			} else {
				tokKaKlijentu.println("Fajl/folder nije uspesno premestan.");
			}

		} catch (IOException e) {
			System.out.println("Greska u premestanju fajla/foldera");
		}
	}

	private void preimenujDirektorijum() {
		try {
			tokKaKlijentu
					.println("\nMolim Vas, unesite putanju foldera koji zelite da preimenujete(Folder\\\\Folder):");
			tokKaKlijentu.println(
					"Napomena: ne unositi ime Vaseg foldera, vec samo ' enter ', ako je to Vasa jedina putanja");
			String putanja = tokOdKlijenta.readLine();
			if (putanja.equals("Izlaz"))
				prekiniKomunikaciju();
			tokKaKlijentu.println("\nMolim Vas, unesite naziv foldera koji zelite da preimenujete:");
			String imeStarogFoldera = tokOdKlijenta.readLine();
			if (imeStarogFoldera.equals("Izlaz"))
				prekiniKomunikaciju();
			tokKaKlijentu.println("\nMolim Vas, unesite novo ime foldera: ");
			String imeNovogFoldera = tokOdKlijenta.readLine();
			if (imeNovogFoldera.equals("Izlaz"))
				prekiniKomunikaciju();
			File imeIzvornogFajla = new File(
					putanjaKaFolderu + "RegistrovaniKorisnici\\" + this.ime + "\\" + putanja + "\\" + imeStarogFoldera);
			File imeDestinacionogFajla = new File(
					putanjaKaFolderu + "RegistrovaniKorisnici\\" + this.ime + "\\" + putanja + "\\" + imeNovogFoldera);

			// renameTo() funkcija koja vec postoji
			if (imeIzvornogFajla.renameTo(imeDestinacionogFajla)) {
				tokKaKlijentu.println("Folder je uspesno preimenovan.");
			} else {
				tokKaKlijentu.println("Folder nije uspesno preimenovan.");
			}

		} catch (IOException e) {
			System.out.println("Greska u preimenoavnju foldera");
		}
	}

	private void kreirajDirektorijum() {
		String putanjaFoldera;
		tokKaKlijentu.println("\nMolim Vas, unesite putanju gde zelite da kreirate novi folder");
		tokKaKlijentu.println("Napomena: ako zelite u svom folderu, samo pritisnite enter");
		try {
			putanjaFoldera = tokOdKlijenta.readLine();
			if (putanjaFoldera.equals("Izlaz"))
				prekiniKomunikaciju();
			tokKaKlijentu.println("\nMolim Vas, unesite ime novog foldera:");
			String imeFoldera = tokOdKlijenta.readLine();
			if (imeFoldera.equals("Izlaz"))
				prekiniKomunikaciju();

			// mkdir() funkcija koja vec postoji
			if (new File(
					putanjaKaFolderu + "RegistrovaniKorisnici\\" + this.ime + "\\" + putanjaFoldera + "\\" + imeFoldera)
							.mkdir()) {
				tokKaKlijentu.println("Uspesno ste napravili " + "" + imeFoldera + " folder\n");
			} else
				tokKaKlijentu.println("Niste izabrali dobru putanju.");
		} catch (IOException e) {
			System.out.println("Greska prilikom kreiranja foldera");
		}

	}

	// funkcija za izlistavanje foldera rekurzivno
	private void listFiles(BufferedReader tokOdKlijenta, PrintStream tokKaKlijentu, File[] nizBajtova, int indeks,
			int nivo) {

		// iskljucujuci uslov
		if (indeks == nizBajtova.length)
			return;

		// dodavanje tabova
		for (int i = 0; i < nivo; i++)
			tokKaKlijentu.print("\t");

		// za fajlove
		if (nizBajtova[indeks].isFile())
			tokKaKlijentu.println(nizBajtova[indeks].getName());

		// za podfoldere
		else if (nizBajtova[indeks].isDirectory()) {
			tokKaKlijentu.println("[" + nizBajtova[indeks].getName() + "]");

			// rekurzija za podfoldere
			listFiles(tokOdKlijenta, tokKaKlijentu, nizBajtova[indeks].listFiles(), 0, nivo + 1);
		}

		// rekurzija za glavni direktorijum
		listFiles(tokOdKlijenta, tokKaKlijentu, nizBajtova, ++indeks, nivo);
	}

	private void registracija() throws Exception {
		String username, password;
		boolean imePostoji = false, daLiJeValidno = false;

		tokKaKlijentu.println("Dobrodosli u registraciju");

		do {
			do {
				tokKaKlijentu.println("\nMolim Vas, unesite korisnicko ime:");
				username = tokOdKlijenta.readLine();
				if (username.equals("Izlaz"))
					prekiniKomunikaciju();
				if (username.contains("\\") || username.contains(",") || username.contains("."))
					tokKaKlijentu.println(
							"Korisnicko ime ne sme da sadrzi karaktere kao sto su '\\', ',' i '.'. Molim Vas, pokusajte ponovo...");
			} while (username.contains("\\") || username.contains(","));

			imePostoji = false;

			BufferedReader br = new BufferedReader(
					new FileReader(putanjaKaFolderu + "RegistrovaniKorisnici\\podaci.txt"));
			String linijaOdPostojecihKorisnika = br.readLine();
			while ((linijaOdPostojecihKorisnika != null)) {
				String[] podaciOdPostojecihKorisnika = linijaOdPostojecihKorisnika.split(",");
				String usernameOdPostojecihKorisnika = podaciOdPostojecihKorisnika[0];
				boolean promeniPrem = Boolean.parseBoolean(podaciOdPostojecihKorisnika[2]);

				if (usernameOdPostojecihKorisnika.equals(username)) {
					premiumKorisnik = promeniPrem;
					imePostoji = true;
					break;
				}
				linijaOdPostojecihKorisnika = br.readLine();
			}
			br.close();

			if (imePostoji) {
				tokKaKlijentu.println("\nUneli ste korisnicko ime koje vec postoji. Molim Vas, pokusajte ponovo...");
			} else {
				daLiJeValidno = true;
			}
		} while (!daLiJeValidno);

		this.ime = username;

		daLiJeValidno = false;

		tokKaKlijentu.println("\nMolim Vas, unesite sifru: ");
		tokKaKlijentu.println("Napomena: Sifra ne sme da pocinje sa ' Izlaz ' ");
		password = tokOdKlijenta.readLine();
		if (password.equals("Izlaz"))
			prekiniKomunikaciju();

		while (true) {
			tokKaKlijentu.println("Da li zelite da imate premium mogucnosti?");
			tokKaKlijentu.println("Napomena: odgovorite sa da/ne (placa se dodatno: 10€ mesecno)");
			String premium = tokOdKlijenta.readLine();
			if (premium.startsWith("Izlaz"))
				prekiniKomunikaciju();
			if (premium.equalsIgnoreCase("ne")) {
				premiumKorisnik = false;
				break;
			} else if (premium.equalsIgnoreCase("da")) {
				premiumKorisnik = true;
				break;
			} else {
				tokKaKlijentu.println("Niste lepo uneli, mozete uneti samo da ili ne");
			}
		}

		// obavestenje o supesnoj registraciji
		tokKaKlijentu.println("\nRegistracija je uspesno obavljena( Korisnicko ime: " + username + " )\n");

		// dodaje se link za serovanje
		String linkZaSerovanje = izgenerisiLink();

		// dodaje se direktorijum
		new File(putanjaKaFolderu + "RegistrovaniKorisnici\\" + username).mkdir();

		// zapisuju se podaci u podaci.txt, odvajaju se zarezom
		try {
			String imeFajla = putanjaKaFolderu + "RegistrovaniKorisnici\\podaci.txt";
			FileWriter fw = new FileWriter(imeFajla, true);
			fw.write(username + "," + password + "," + premiumKorisnik + "," + linkZaSerovanje + "\n");
			fw.close();
		} catch (IOException ioe) {
			System.out.println("Doslo je do greske");
		}

		meniZaKorisnikeKojiSuUlogovani();
	}

	private void prijavljivanje() throws Exception {
		String username, password;
		boolean dbPrem = false, daLijeValidno = false, usernamePostoji = false;
		String passwordOdPostojecihKorisnika = "";

		tokKaKlijentu.println("\nDobrodosli u prijavljivanje");

		do {
			tokKaKlijentu.println("\nMolim Vas, unesite korisnicko ime:");
			username = tokOdKlijenta.readLine();
			if (username.equals("Izlaz"))
				prekiniKomunikaciju();
			usernamePostoji = false;

			BufferedReader br = new BufferedReader(
					new FileReader(putanjaKaFolderu + "RegistrovaniKorisnici\\podaci.txt"));
			String linijaOdPostojecihKorisnika = br.readLine();
			while ((linijaOdPostojecihKorisnika != null)) {
				String[] informacijeOdPostojecihKorisnika = linijaOdPostojecihKorisnika.split(",");
				String usernameOdPostojecihKorisnika = informacijeOdPostojecihKorisnika[0];
				passwordOdPostojecihKorisnika = informacijeOdPostojecihKorisnika[1];
				boolean promeniPrem = Boolean.parseBoolean(informacijeOdPostojecihKorisnika[2]);

				if (usernameOdPostojecihKorisnika.equals(username)) {
					dbPrem = promeniPrem;
					usernamePostoji = true;
					break;
				}

				linijaOdPostojecihKorisnika = br.readLine();
			}

			br.close();

			if (usernamePostoji) {
				daLijeValidno = true;
			} else {
				tokKaKlijentu.println("\nKorisnicno ime ne postoji u bazi, molim Vas pokusajte ponovo...");
			}
		} while (!daLijeValidno);

		this.ime = username;

		// vrati validnost na negativno zbog provere sifre
		daLijeValidno = false;

		do {
			tokKaKlijentu.println("\nMolim Vas, unesite sifru:");
			password = tokOdKlijenta.readLine();
			if (password.equals("Izlaz"))
				prekiniKomunikaciju();
			if (passwordOdPostojecihKorisnika != "" && password.equals(passwordOdPostojecihKorisnika)) {
				daLijeValidno = true;
			} else {
				tokKaKlijentu.println("\nSifra koju ste uneli je pogresna, molim Vas pokusajte ponovo.");
			}
		} while (!daLijeValidno);

		this.sifra = password;

		this.premiumKorisnik = dbPrem;

		tokKaKlijentu.println("\nPrijava je uspesno obavljena.\n");
		meniZaKorisnikeKojiSuUlogovani();
	}

	// stavitti sve null, da ne bi ostalo nesto
	private void izlogujSe() throws Exception {
		this.ime = null;
		this.sifra = null;
		this.premiumKorisnik = false;
		tokKaKlijentu.println("\nIzlogovanje je uspesno obavljeno.\n");
		meni();
	}

	private void prekiniKomunikaciju() {

		try {
			// tekst mora biti isti ako u ChatKlijent
			tokKaKlijentu.println("Aplikacija je zavrsena\n");
			soketZaKomunikaciju.close();
		} catch (IOException e) {
			System.err.println("Doslo je do greske prilikom zatvaranja komunikacije.");
		}
	}

	public void run() {

		try {

			tokOdKlijenta = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			tokKaKlijentu = new PrintStream(soketZaKomunikaciju.getOutputStream());

			meni();

		} catch (Exception e) {
			System.out.println("Doslo je do greske prilikom pokretanja RUN metode.");
		}
	}
}