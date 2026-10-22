package ma.phonerepair.app;

import javax.swing.SwingUtilities;

/** Point d'entrée de RepairTrack. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Toute l'interface Swing doit être créée dans l'Event Dispatch Thread.
        SwingUtilities.invokeLater(Application::demarrer);
    }
}
