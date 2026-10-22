-- =====================================================================
-- RepairTrack — schéma MySQL 8
-- Rejouable : ATTENTION, supprime et recrée la base `repairtrack`.
-- Usage : mysql -u root -p < database/schema.sql
-- Règles de gestion : docs/business-rules.md (RG-xx)
-- =====================================================================

SET NAMES utf8mb4;   -- indispensable pour les accents (é, è...)

DROP DATABASE IF EXISTS repairtrack;
CREATE DATABASE repairtrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE repairtrack;

-- ---------------------------------------------------------------------
-- Boutique (RG-06) : une seule ligne autorisée (id = 1)
-- ---------------------------------------------------------------------
CREATE TABLE boutique (
    id              TINYINT       NOT NULL DEFAULT 1,
    nom             VARCHAR(100)  NOT NULL,
    adresse         VARCHAR(255)  NOT NULL,
    telephone       VARCHAR(20)   NOT NULL,
    numero_patente  VARCHAR(30)   NOT NULL,
    date_creation   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_boutique PRIMARY KEY (id),
    CONSTRAINT ck_boutique_unique CHECK (id = 1)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Utilisateur (RG-01, RG-03, RG-04)
-- ---------------------------------------------------------------------
CREATE TABLE utilisateur (
    id              INT           NOT NULL AUTO_INCREMENT,
    nom_complet     VARCHAR(100)  NOT NULL,
    telephone       VARCHAR(20)   NULL,
    username        VARCHAR(50)   NOT NULL,
    password_hash   VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL,
    actif           BOOLEAN       NOT NULL DEFAULT TRUE,
    date_creation   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_utilisateur PRIMARY KEY (id),
    CONSTRAINT uq_utilisateur_username UNIQUE (username),
    CONSTRAINT ck_utilisateur_role CHECK (role IN ('PROPRIETAIRE', 'REPARATEUR'))
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Profil réparateur (RG-02) : 0..1 par utilisateur
-- ---------------------------------------------------------------------
CREATE TABLE reparateur (
    id               INT           NOT NULL AUTO_INCREMENT,
    utilisateur_id   INT           NOT NULL,
    taux_commission  DECIMAL(5,2)  NOT NULL,
    CONSTRAINT pk_reparateur PRIMARY KEY (id),
    CONSTRAINT uq_reparateur_utilisateur UNIQUE (utilisateur_id),
    CONSTRAINT fk_reparateur_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id),
    CONSTRAINT ck_reparateur_taux CHECK (taux_commission BETWEEN 0 AND 100)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Caisse : une par profil réparateur. Le solde est calculé (RG-37).
-- ---------------------------------------------------------------------
CREATE TABLE caisse (
    id              INT       NOT NULL AUTO_INCREMENT,
    reparateur_id   INT       NOT NULL,
    date_creation   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_caisse PRIMARY KEY (id),
    CONSTRAINT uq_caisse_reparateur UNIQUE (reparateur_id),
    CONSTRAINT fk_caisse_reparateur FOREIGN KEY (reparateur_id) REFERENCES reparateur (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Client (RG-08, RG-09) : pas de compte, téléphone indexé non unique
-- ---------------------------------------------------------------------
CREATE TABLE client (
    id              INT           NOT NULL AUTO_INCREMENT,
    nom_complet     VARCHAR(100)  NOT NULL,
    telephone       VARCHAR(20)   NOT NULL,
    date_creation   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_client PRIMARY KEY (id),
    INDEX idx_client_telephone (telephone),
    INDEX idx_client_nom (nom_complet)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Réparation = commande (RG-10 à RG-14). Un seul code de suivi.
-- Statut global et montants : calculés, non stockés (RG-19, RG-24).
-- ---------------------------------------------------------------------
CREATE TABLE reparation (
    id              INT           NOT NULL AUTO_INCREMENT,
    code_suivi      VARCHAR(20)   NOT NULL,
    client_id       INT           NOT NULL,
    reparateur_id   INT           NOT NULL,          -- responsable
    cree_par        INT           NOT NULL,          -- utilisateur qui a saisi
    date_creation   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarque        VARCHAR(500)  NULL,
    CONSTRAINT pk_reparation PRIMARY KEY (id),
    CONSTRAINT uq_reparation_code UNIQUE (code_suivi),
    CONSTRAINT fk_reparation_client FOREIGN KEY (client_id) REFERENCES client (id),
    CONSTRAINT fk_reparation_reparateur FOREIGN KEY (reparateur_id) REFERENCES reparateur (id),
    CONSTRAINT fk_reparation_cree_par FOREIGN KEY (cree_par) REFERENCES utilisateur (id),
    INDEX idx_reparation_date (date_creation)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Appareil : chacun son statut (RG-15). Quand statut = DELIVERED,
-- date_maj_statut = date de livraison (état final).
-- ---------------------------------------------------------------------
CREATE TABLE appareil (
    id                INT           NOT NULL AUTO_INCREMENT,
    reparation_id     INT           NOT NULL,
    marque            VARCHAR(50)   NOT NULL,
    modele            VARCHAR(80)   NOT NULL,
    imei_serie        VARCHAR(30)   NULL,
    description       VARCHAR(255)  NULL,
    etat_physique     VARCHAR(255)  NULL,
    probleme_signale  VARCHAR(500)  NOT NULL,
    statut            VARCHAR(30)   NOT NULL DEFAULT 'RECEIVED',
    date_maj_statut   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_appareil PRIMARY KEY (id),
    CONSTRAINT fk_appareil_reparation FOREIGN KEY (reparation_id) REFERENCES reparation (id),
    CONSTRAINT ck_appareil_statut CHECK (statut IN ('RECEIVED', 'DIAGNOSING', 'WAITING_FOR_APPROVAL',
        'WAITING_FOR_PART', 'REPAIRING', 'READY', 'DELIVERED', 'CANCELLED')),
    INDEX idx_appareil_statut (statut, date_maj_statut)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Intervention : diagnostic + pièces + main d'œuvre (RG-20 à RG-24)
-- taux_commission_applique = copie figée du taux du réparateur (RG-33)
-- ---------------------------------------------------------------------
CREATE TABLE intervention (
    id                        INT            NOT NULL AUTO_INCREMENT,
    appareil_id               INT            NOT NULL,
    reparateur_id             INT            NOT NULL,
    diagnostic                VARCHAR(500)   NOT NULL,
    pieces_description        VARCHAR(500)   NULL,
    cout_achat_pieces         DECIMAL(10,2)  NOT NULL DEFAULT 0,
    prix_pieces_client        DECIMAL(10,2)  NOT NULL DEFAULT 0,
    main_oeuvre               DECIMAL(10,2)  NOT NULL DEFAULT 0,
    taux_commission_applique  DECIMAL(5,2)   NOT NULL,
    date_intervention         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_intervention PRIMARY KEY (id),
    CONSTRAINT fk_intervention_appareil FOREIGN KEY (appareil_id) REFERENCES appareil (id),
    CONSTRAINT fk_intervention_reparateur FOREIGN KEY (reparateur_id) REFERENCES reparateur (id),
    CONSTRAINT ck_intervention_montants CHECK (cout_achat_pieces >= 0 AND prix_pieces_client >= 0 AND main_oeuvre >= 0),
    CONSTRAINT ck_intervention_taux CHECK (taux_commission_applique BETWEEN 0 AND 100)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Historique des statuts (RG-16)
-- ---------------------------------------------------------------------
CREATE TABLE historique_statut (
    id               INT           NOT NULL AUTO_INCREMENT,
    appareil_id      INT           NOT NULL,
    ancien_statut    VARCHAR(30)   NULL,              -- NULL à la création
    nouveau_statut   VARCHAR(30)   NOT NULL,
    date_changement  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    utilisateur_id   INT           NOT NULL,
    commentaire      VARCHAR(255)  NULL,
    CONSTRAINT pk_historique_statut PRIMARY KEY (id),
    CONSTRAINT fk_historique_appareil FOREIGN KEY (appareil_id) REFERENCES appareil (id),
    CONSTRAINT fk_historique_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Paiement client (RG-25 à RG-30) : une ligne par versement
-- ---------------------------------------------------------------------
CREATE TABLE paiement (
    id              INT            NOT NULL AUTO_INCREMENT,
    reparation_id   INT            NOT NULL,
    montant         DECIMAL(10,2)  NOT NULL,
    date_paiement   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    type_paiement   VARCHAR(20)    NOT NULL DEFAULT 'ESPECES',
    utilisateur_id  INT            NOT NULL,          -- qui a encaissé
    note            VARCHAR(255)   NULL,
    CONSTRAINT pk_paiement PRIMARY KEY (id),
    CONSTRAINT fk_paiement_reparation FOREIGN KEY (reparation_id) REFERENCES reparation (id),
    CONSTRAINT fk_paiement_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id),
    CONSTRAINT ck_paiement_montant CHECK (montant > 0),
    CONSTRAINT ck_paiement_type CHECK (type_paiement IN ('ESPECES', 'CARTE', 'VIREMENT', 'CHEQUE')),
    INDEX idx_paiement_date (date_paiement)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Mouvement de caisse (RG-36 à RG-39). Montant toujours positif,
-- le sens est donné par le type. ENCAISSEMENT <=> paiement_id renseigné.
-- ---------------------------------------------------------------------
CREATE TABLE mouvement_caisse (
    id              INT            NOT NULL AUTO_INCREMENT,
    caisse_id       INT            NOT NULL,
    type            VARCHAR(30)    NOT NULL,
    montant         DECIMAL(10,2)  NOT NULL,
    date_mouvement  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paiement_id     INT            NULL,
    utilisateur_id  INT            NOT NULL,
    libelle         VARCHAR(255)   NULL,
    CONSTRAINT pk_mouvement_caisse PRIMARY KEY (id),
    CONSTRAINT uq_mouvement_paiement UNIQUE (paiement_id),
    CONSTRAINT fk_mouvement_caisse FOREIGN KEY (caisse_id) REFERENCES caisse (id),
    CONSTRAINT fk_mouvement_paiement FOREIGN KEY (paiement_id) REFERENCES paiement (id),
    CONSTRAINT fk_mouvement_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id),
    CONSTRAINT ck_mouvement_montant CHECK (montant > 0),
    CONSTRAINT ck_mouvement_type CHECK (type IN ('ENCAISSEMENT', 'ACHAT_PIECES',
        'VERSEMENT_PROPRIETAIRE', 'PAIEMENT_COMMISSION')),
    CONSTRAINT ck_mouvement_lien_paiement CHECK ((type = 'ENCAISSEMENT') = (paiement_id IS NOT NULL)),
    INDEX idx_mouvement_caisse_date (caisse_id, date_mouvement)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Crédit inter-boutiques (RG-40 à RG-44). L'autre boutique est externe.
-- ---------------------------------------------------------------------
CREATE TABLE credit_interboutique (
    id                  INT            NOT NULL AUTO_INCREMENT,
    sens                VARCHAR(10)    NOT NULL,       -- ACCORDE : on nous doit / RECU : on doit
    nom_boutique_tiers  VARCHAR(100)   NOT NULL,
    proprietaire_tiers  VARCHAR(100)   NULL,
    patente_tiers       VARCHAR(30)    NOT NULL,
    telephone_tiers     VARCHAR(20)    NULL,
    montant             DECIMAL(10,2)  NOT NULL,
    date_credit         DATE           NOT NULL,
    description         VARCHAR(500)   NULL,
    cree_par            INT            NOT NULL,
    date_creation       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_credit PRIMARY KEY (id),
    CONSTRAINT fk_credit_cree_par FOREIGN KEY (cree_par) REFERENCES utilisateur (id),
    CONSTRAINT ck_credit_sens CHECK (sens IN ('ACCORDE', 'RECU')),
    CONSTRAINT ck_credit_montant CHECK (montant > 0),
    INDEX idx_credit_patente (patente_tiers)
) ENGINE = InnoDB;

CREATE TABLE remboursement_credit (
    id                  INT            NOT NULL AUTO_INCREMENT,
    credit_id           INT            NOT NULL,
    montant             DECIMAL(10,2)  NOT NULL,
    date_remboursement  DATE           NOT NULL,
    note                VARCHAR(255)   NULL,
    utilisateur_id      INT            NOT NULL,
    CONSTRAINT pk_remboursement PRIMARY KEY (id),
    CONSTRAINT fk_remboursement_credit FOREIGN KEY (credit_id) REFERENCES credit_interboutique (id),
    CONSTRAINT fk_remboursement_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id),
    CONSTRAINT ck_remboursement_montant CHECK (montant > 0)
) ENGINE = InnoDB;
