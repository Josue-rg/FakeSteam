package com.example.fakesteam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class FakeSteamApplication {
	public static void main(String[] args) {
		// Carga el archivo .env local (en Render las variables ya vienen del entorno)
		try {
			Dotenv dotenv = Dotenv.configure().load();
			dotenv.entries().forEach(entry -> {
				if (System.getProperty(entry.getKey()) == null
						&& System.getenv(entry.getKey()) == null) {
					System.setProperty(entry.getKey(), entry.getValue());
				}
			});
		} catch (Exception e) {
			// No hay .env (por ejemplo en Render): se usan las variables del entorno
		}
		configurarBaseDeDatos();
		SpringApplication.run(FakeSteamApplication.class, args);
	}

	/**
	 * Neon entrega la URL en formato libpq: postgresql://usuario:clave@host/db?params
	 * JDBC necesita: jdbc:postgresql://host/db?params (con usuario y clave aparte).
	 */
	private static void configurarBaseDeDatos() {
		String dbUrl = System.getProperty("DB_URL", System.getenv("DB_URL"));
		if (dbUrl == null || dbUrl.startsWith("jdbc:")) {
			return;
		}
		if (dbUrl.startsWith("postgresql://") || dbUrl.startsWith("postgres://")) {
			String rest = dbUrl.replaceFirst("^postgres(ql)?://", "");
			int arroba = rest.indexOf('@');
			int barra = rest.indexOf('/', arroba);
			String userInfo = rest.substring(0, arroba);
			String hostYDb = rest.substring(arroba + 1, barra >= 0 ? barra : rest.length());
			String params = barra >= 0 ? rest.substring(barra) : "";
			int dosPuntos = userInfo.indexOf(':');
			if (dosPuntos > 0) {
				System.setProperty("DB_USER", userInfo.substring(0, dosPuntos));
				System.setProperty("DB_PASSWORD", userInfo.substring(dosPuntos + 1));
			}
			System.setProperty("DB_URL", "jdbc:postgresql://" + hostYDb + params);
		} else {
			System.setProperty("DB_URL", "jdbc:" + dbUrl);
		}
	}
}
