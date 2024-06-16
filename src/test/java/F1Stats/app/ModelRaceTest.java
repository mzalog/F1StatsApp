package F1Stats.app;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class ModelRaceTest {

    @Test
    public void testSetAndGetId() {
        ModelRace race = new ModelRace();
        race.setId(1);
        assertEquals(1, race.getId());
    }

    @Test
    public void testRaceNameIsNotEmpty() {
        ModelRace race = new ModelRace();
        race.setRaceName("Australian GP");
        assertTrue(!race.getRaceName().isEmpty());
    }

    @Test
    public void testRaceListContainsRace() {
        List<ModelRace> races = new ArrayList<>();
        ModelRace race = new ModelRace();
        race.setId(1);
        race.setRaceName("Australian GP");
        races.add(race);

        assertTrue(races.contains(race));
    }

    @Test
    public void testRaceNameIsNotNull() {
        ModelRace race = new ModelRace();
        race.setRaceName("Australian GP");
        assertNotNull(race.getRaceName());
    }

    @Test
    public void testRaceIdIsIncorrect() {
        ModelRace race = new ModelRace();
        race.setId(1);
        assertNotEquals(2, race.getId());
    }
}
