package ma.phonerepair.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lit les paramètres de connexion dans le fichier {@code db.properties}.
 * <p>
 * Ordre de recherche : 1) dossier courant (racine du projet), 2) classpath.
 * Le mot de passe n'est jamais écrit dans le code source.
 */
public final class DatabaseConfig {

    public static final String FICHIER = "db.properties";

    private final String url;
    private final String utilisateur;
    private final String motDePasse;

    private DatabaseConfig(String url, String utilisateur, String motDePasse) {
        this.url = url;
        this.utilisateur = utilisateur;
        this.motDePasse = motDePasse;
    }

    public static DatabaseConfig charger() {
        Properties props = new Properties();
        Path fichierLocal = Path.of(FICHIER);
        try {
            if (Files.exists(fichierLocal)) {
                try (InputStream in = Files.newInputStream(fichierLocal)) {
                    props.load(in);
                }
            } else {
                try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream(FICHIER)) {
                    if (in == null) {
                        throw new IllegalStateException("Fichier " + FICHIER + " introuvable. "
                                + "Copiez db.properties.example en db.properties à la racine du projet.");
                    }
                    props.load(in);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire " + FICHIER, e);
        }
        String url = props.getProperty("db.url");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("La propriété db.url est absente de " + FICHIER);
        }
        return new DatabaseConfig(url.trim(),
                props.getProperty("db.user", "root").trim(),
                props.getProperty("db.password", ""));
    }

    public String getUrl() {
        return url;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public String getMotDePasse() {
        return motDePasse;
    }
}
