package octoteam.tahiti.archiver;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RollingCalendar;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class DatePatternFileMatcher {

    protected String filePatternStr;

    protected FileNamePattern filePattern;

    protected RollingCalendar rc;

    DatePatternFileMatcher(String filePattern, Context ctx) {
        this.filePatternStr = filePattern;
        this.filePattern = new FileNamePattern(filePattern, ctx);
        DateTokenConverter<Object> dtc = this.filePattern.getPrimaryDateTokenConverter();
        if (dtc.getTimeZone() != null) {
            rc = new RollingCalendar(dtc.getDatePattern(), dtc.getTimeZone(), Locale.getDefault());
        } else {
            rc = new RollingCalendar(dtc.getDatePattern());
        }
    }

    File[] matchFilesInDateRange(long start, long end) {
        long periods = rc.periodBarriersCrossed(start, end);
        List<File> files = new ArrayList<File>();
        Date lastDate = new Date(start);

        for (int i = 0; i < periods; ++i) {
            File f = new File(filePattern.convert(lastDate));
            if (f.exists() && !f.isDirectory()) {
                files.add(f);
            }
            lastDate = rc.getNextTriggeringDate(lastDate);
        }

        return files.toArray(new File[files.size()]);
    }

    String getFilePattern() {
        return filePatternStr;
    }

}
