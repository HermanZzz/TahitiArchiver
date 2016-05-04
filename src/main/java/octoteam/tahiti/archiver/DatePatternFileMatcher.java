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

    String[] getFileNamesInDateRange(long start, long end) {
        long periods = rc.periodBarriersCrossed(start, end);
        String[] ret = new String[(int)periods];
        Date lastDate = new Date(start);

        for (int i = 0; i < periods; ++i) {
            ret[i] = filePattern.convert(lastDate);
            lastDate = rc.getNextTriggeringDate(lastDate);
        }

        return ret;
    }

    File[] matchFilesInDateRange(long start, long end) {
        List<File> files = new ArrayList<File>();
        String[] fileNames = getFileNamesInDateRange(start, end);
        for (String fileName : fileNames) {
            File f = new File(fileName);
            if (f.exists() && !f.isDirectory()) {
                files.add(f);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    String getFilePattern() {
        return filePatternStr;
    }

}
