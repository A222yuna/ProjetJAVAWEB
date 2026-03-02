package tn.psy.gestioncabinet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.psy.gestioncabinet.model.Cabinet;
import tn.psy.gestioncabinet.service.CabinetService;
import tn.psy.gestioncabinet.util.DbInitializer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires CRUD pour CabinetService.
 * Utilise un cabinet de test créé en @BeforeEach et nettoyé en @AfterEach
 * pour ne pas polluer la base principale.
 */
@DisplayName("CabinetService - Tests CRUD")
class CabinetServiceTest {

    private static final String ADRESSE_TEST = "Adresse Test MindConnect UNIQUE";
    private static final String VILLE_TEST = "Ville Test UNIQUE";
    private static final String HORAIRES_TEST = "Lun-Ven 9h-18h";
    private static final String DESCRIPTION_TEST = "Cabinet de test pour JUnit";
    /** ID psychologue existant . */
    private static final int ID_PSYCHOLOGUE_TEST = 1;

    private CabinetService cabinetService;

    private int createdCabinetId;

    @BeforeAll
    static void initDatabase() {
        DbInitializer.initialize();
    }

    @BeforeEach
    void setUp() {
        cabinetService = new CabinetService();
        createdCabinetId = -1;
        Cabinet cabinet = new Cabinet(ADRESSE_TEST, VILLE_TEST, HORAIRES_TEST, DESCRIPTION_TEST);
        int id = cabinetService.ajouterCabinet(cabinet, ID_PSYCHOLOGUE_TEST);
        if (id > 0) {
            createdCabinetId = id;
        }
    }

    @AfterEach
    void tearDown() {
        if (createdCabinetId > 0) {
            cabinetService.supprimerCabinet(createdCabinetId, true);
        }
    }

    @Test
    @DisplayName("Create - ajout d'un cabinet et vérification en base")
    void testCreateCabinet() {
        Cabinet nouveau = new Cabinet(
                "Nouvelle Adresse Test " + System.currentTimeMillis(),
                "Nouvelle Ville Test",
                "Mar-Dim 10h-19h",
                "Description nouveau cabinet"
        );
        int id = cabinetService.ajouterCabinet(nouveau, ID_PSYCHOLOGUE_TEST);

        assertTrue(id > 0, "L'ID du cabinet créé doit être strictement positif");

        Optional<Cabinet> enBase = cabinetService.findById(id);
        assertTrue(enBase.isPresent(), "Le cabinet doit être présent en base après création");
        assertEquals(nouveau.getAdresse(), enBase.get().getAdresse());
        assertEquals(nouveau.getVille(), enBase.get().getVille());
        assertEquals(nouveau.getHoraires(), enBase.get().getHoraires());
        assertEquals(nouveau.getDescription(), enBase.get().getDescription());
        assertFalse(enBase.get().isValide(), "Un nouveau cabinet doit être en attente (valide = false)");

        cabinetService.supprimerCabinet(id, true);
    }

    @Test
    @DisplayName("Read - récupération d'un cabinet par ID")
    void testGetCabinetById() {
        Assumptions.assumeTrue(createdCabinetId > 0, "Un cabinet de test doit avoir été créé en @BeforeEach");

        Optional<Cabinet> opt = cabinetService.findById(createdCabinetId);

        assertTrue(opt.isPresent(), "Le cabinet de test doit être trouvé");
        Cabinet c = opt.get();
        assertEquals(createdCabinetId, c.getIdCabinet());
        assertEquals(ADRESSE_TEST, c.getAdresse());
        assertEquals(VILLE_TEST, c.getVille());
        assertEquals(HORAIRES_TEST, c.getHoraires());
        assertEquals(DESCRIPTION_TEST, c.getDescription());
    }

    @Test
    @DisplayName("Update - modification d'un cabinet et vérification en base")
    void testUpdateCabinet() {
        Assumptions.assumeTrue(createdCabinetId > 0, "Un cabinet de test doit avoir été créé en @BeforeEach");

        Optional<Cabinet> opt = cabinetService.findById(createdCabinetId);
        assertTrue(opt.isPresent());
        Cabinet c = opt.get();

        String nouvelleAdresse = "Adresse modifiée " + System.currentTimeMillis();
        String nouvelleVille = "Ville modifiée";
        c.setAdresse(nouvelleAdresse);
        c.setVille(nouvelleVille);
        c.setHoraires("Sam-Dim 8h-14h");
        c.setDescription("Description mise à jour");

        boolean modif = cabinetService.modifierCabinet(c);
        assertTrue(modif, "La modification doit réussir");

        Optional<Cabinet> apres = cabinetService.findById(createdCabinetId);
        assertTrue(apres.isPresent());
        assertEquals(nouvelleAdresse, apres.get().getAdresse());
        assertEquals(nouvelleVille, apres.get().getVille());
        assertEquals("Sam-Dim 8h-14h", apres.get().getHoraires());
        assertEquals("Description mise à jour", apres.get().getDescription());
    }

    @Test
    @DisplayName("Delete - suppression d'un cabinet non validé")
    void testDeleteCabinet() {
        Cabinet pourSuppression = new Cabinet(
                "Adresse à supprimer " + System.currentTimeMillis(),
                "Ville à supprimer",
                "",
                ""
        );
        int idSuppr = cabinetService.ajouterCabinet(pourSuppression, ID_PSYCHOLOGUE_TEST);
        assertTrue(idSuppr > 0);

        boolean supprime = cabinetService.supprimerCabinet(idSuppr, true);
        assertTrue(supprime, "La suppression doit réussir pour un cabinet non validé");

        Optional<Cabinet> apres = cabinetService.findById(idSuppr);
        assertTrue(apres.isEmpty(), "Le cabinet ne doit plus exister en base après suppression");
    }
}
