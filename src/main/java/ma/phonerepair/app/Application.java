package ma.phonerepair.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;
import ma.phonerepair.presentation.LoginFrame;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.util.DatabaseConnection;

/** Initialisation : journalisation, thème, vérification de la base, écran de connexion. */
public final class Application {

    private static final Logger LOG = Logger.getLogger(Application.class.getName());
    /** Référence forte obligatoire : sinon le logger (et son fichier) peut être libéré par le GC. */
    private static final Logger LOG_APPLICATION = Logger.getLogger("ma.phonerepair");

    private Application() {
    }

    static void demarrer() {
        configurerJournal();
        UiTheme.installer();
        try {
            DatabaseConnection.testerConnexion();
        } catch (SQLException | IllegalStateException e) {
            LOG.log(Level.SEVERE, "Base de données inaccessible au démarrage", e);
            JOptionPane.showMessageDialog(null,
                    "Impossible de se connecter à la base de données.\n\n"
                    + "Vérifiez que :\n"
                    + " • MySQL est démarré ;\n"
                    + " • la base a été créée avec database/schema.sql et database/data.sql ;\n"
                    + " • le fichier db.properties existe et contient le bon mot de passe.\n\n"
                    + "Détail : " + e.getMessage(),
                    "RepairTrack — erreur de démarrage", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        new LoginFrame().setVisible(true);
    }

    /** Journal dans logs/repairtrack.log (erreurs techniques, connexions, accès refusés). */
    private static void configurerJournal() {
        try {
            Files.createDirectories(Path.of("logs"));
            FileHandler fichier = new FileHandler("logs/repairtrack.log", true);
            fichier.setEncoding("UTF-8");
            fichier.setFormatter(new SimpleFormatter());
            LOG_APPLICATION.addHandler(fichier);
            LOG_APPLICATION.setLevel(Level.INFO);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Journal fichier indisponible, sortie console uniquement", e);
        }
    }
}
