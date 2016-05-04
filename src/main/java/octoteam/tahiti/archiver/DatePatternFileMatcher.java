package octoteam.tahiti.archiver;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RollingCalendar;

import java.io.File;
import java.util.*;

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

    Date[] getPeriodsInRange(long start, long end) {
        long periods = rc.periodBarriersCrossed(start, end);
        Date[] ret = new Date[(int) periods];
        Date lastDate = new Date(start);
        for (int i = 0; i < periods; ++i) {
            lastDate = rc.getNextTriggeringDate(lastDate);
        }
        return ret;
    }

    protected File getParentDirForDate(Date now) {
        File archive0 = new File(filePattern.convertMultipleArguments(now, 0));
        return archive0.getAbsoluteFile().getParentFile();
    }

    File[] matchFilesInDateRange(long start, long end) {
        Date[] periods = getPeriodsInRange(start, end);
        List<File> files = new ArrayList<File>();
        for (Date d : periods) {
            if (filePattern.hasIntegerTokenCOnverter()) {
                // contains integer token: match files in directory
                File parentDir = getParentDirForDate(d);
                String regex = filePattern.toRegexForFixedDate(d);
                String stemRegex = FileFilterUtil.afterLastSlash(regex);
                File[] matchingFileArray = FileFilterUtil.filesInFolderMatchingStemRegex(parentDir, stemRegex);
                Collections.addAll(files, matchingFileArray);
            } else {
                // doesn't contain integer token: test existence directly
                File f = new File(filePattern.convert(d));
                if (f.exists() && !f.isDirectory()) {
                    files.add(f);
                }
            }
        }
        return files.toArray(new File[files.size()]);
    }

    String getFilePattern() {
        return filePatternStr;
    }

}
