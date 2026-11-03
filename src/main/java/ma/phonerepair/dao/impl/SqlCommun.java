package ma.phonerepair.dao.impl;

/** Fragments SQL partagés entre plusieurs DAO (une seule définition des montants). */
final class SqlCommun {

    private SqlCommun() {
    }

    /**
     * Total d'une réparation (RG-24) : pièces client + main d'œuvre des appareils NON annulés.
     * @param idReparation expression SQL de l'id (« ? » ou « r.id ») — jamais une saisie utilisateur
     */
    static String total(String idReparation) {
        return "(SELECT COALESCE(SUM(i.prix_pieces_client + i.main_oeuvre), 0) "
                + "FROM intervention i JOIN appareil a ON a.id = i.appareil_id "
                + "WHERE a.reparation_id = " + idReparation + " AND a.statut <> 'CANCELLED')";
    }

    /** Somme des paiements d'une réparation. */
    static String paye(String idReparation) {
        return "(SELECT COALESCE(SUM(p.montant), 0) FROM paiement p WHERE p.reparation_id = " + idReparation + ")";
    }
}
