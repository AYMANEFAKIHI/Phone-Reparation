package ma.phonerepair.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * Dates d'affichage et bornes de périodes pour les statistiques.
 * Les bornes sont [début inclus ; fin exclue[.
 */
public final class DateUtil {

    public static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMAT_DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateUtil() {
    }

    public static String formater(LocalDate date) {
        return date == null ? "" : date.format(FORMAT_DATE);
    }

    public static String formater(LocalDateTime dateHeure) {
        return dateHeure == null ? "" : dateHeure.format(FORMAT_DATE_HEURE);
    }

    /**
     * Lit une date saisie au format jj/mm/aaaa.
     *
     * @throws java.time.format.DateTimeParseException si le format est incorrect
     */
    public static LocalDate parser(String saisie) {
        return LocalDate.parse(saisie.trim(), FORMAT_DATE);
    }

    public static LocalDateTime debutJour(LocalDate jour) {
        return jour.atStartOfDay();
    }

    /** Semaine du lundi au dimanche. */
    public static LocalDateTime debutSemaine(LocalDate jour) {
        return jour.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
    }

    public static LocalDateTime debutMois(LocalDate jour) {
        return jour.withDayOfMonth(1).atStartOfDay();
    }
}
