package octoteam.tahiti.archiver;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class DatePatternFileMatcherTest {

    @Test
    public void testMultiplePeriods() {
        Context ctx = new ContextBase();
        DatePatternFileMatcher matcher = new DatePatternFileMatcher("logs/log_%d{yyyy_MM_dd_H}.log", ctx);
        String[] files = matcher.getFileNamesInDateRange(
                new GregorianCalendar(2016, 3, 5, 0, 0, 0).getTime().getTime(),
                new GregorianCalendar(2016, 3, 6, 0, 0, 0).getTime().getTime()
        );
        assertEquals(24, files.length);
        for (int i = 0; i < 24; ++i) {
            assertEquals(String.format("logs/log_2016_04_05_%d.log", i), files[i]);
        }
    }

    @Test
    public void testSinglePeriod() {
        Context ctx = new ContextBase();
        DatePatternFileMatcher matcher = new DatePatternFileMatcher("logs/log_%d{yyyy_MM_dd}.log", ctx);
        String[] files = matcher.getFileNamesInDateRange(
                new GregorianCalendar(2016, 3, 5, 0, 0, 0).getTime().getTime(),
                new GregorianCalendar(2016, 3, 6, 0, 0, 0).getTime().getTime()
        );
        assertEquals(1, files.length);
        assertEquals("logs/log_2016_04_05.log", files[0]);
    }

}