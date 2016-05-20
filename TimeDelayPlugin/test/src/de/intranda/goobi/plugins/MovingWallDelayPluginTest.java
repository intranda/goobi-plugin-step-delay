package de.intranda.goobi.plugins;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import org.goobi.beans.Step;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MovingWallDelayPluginTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        Date now = new Date();
        Date inOneSecond = new Date(now.getTime() + 1000);
        LocalDate today = new LocalDate(now.getTime());
        LocalDate tomorrow = today.plusDays(1);
        LocalDate yesterday = today.minusDays(1);
        MockMovingWallDelayPlugin plugin = new MockMovingWallDelayPlugin(inOneSecond);
        assertFalse(plugin.delayIsExhausted());
        assertEquals(0, plugin.getRemainingDelay(), 0);
        plugin = new MockMovingWallDelayPlugin(yesterday.toDateTimeAtStartOfDay().toDate());
        assertTrue(plugin.delayIsExhausted());
        plugin = new MockMovingWallDelayPlugin(tomorrow.toDateTimeAtStartOfDay().toDate());
        assertFalse(plugin.delayIsExhausted());
        assertEquals(1, plugin.getRemainingDelay(),0);
    }

    private static class MockMovingWallDelayPlugin extends MovingWallDelayPlugin {
        
        private Date date;
        
        public MockMovingWallDelayPlugin(Date date) {
            this.date = date;
        }
        
        @Override
        protected Date getMovingWallDate(Step step) throws ParseException, IllegalArgumentException {
            return date;
        }

    }
    
}
