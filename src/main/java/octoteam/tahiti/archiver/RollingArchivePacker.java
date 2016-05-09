package octoteam.tahiti.archiver;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RollingCalendar;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class RollingArchivePacker {

    protected Context ctx = new ContextBase();

    protected String[] srcPatterns;
    protected FileNamePattern destPattern;
    protected DatePatternFileMatcher[] srcMatchers;

    protected RollingCalendar rc;

    protected ScheduledExecutorService executorService;

    protected boolean started = false;

    //encryptor with password="pswd"
    private Encryptor encryptor = new Encryptor("pswd");

    public RollingArchivePacker(String[] srcFilePatterns, String destFilePattern) {
        this.srcPatterns = srcFilePatterns;
        this.destPattern = new FileNamePattern(destFilePattern, ctx);
        this.srcMatchers = new DatePatternFileMatcher[srcPatterns.length];
        for (int i = 0; i < srcPatterns.length; ++i) {
            this.srcMatchers[i] = new DatePatternFileMatcher(this.srcPatterns[i], ctx);
        }
    }

    public void start() {
        if (started) {
            return;
        }

        Date now = new Date();

        DateTokenConverter<Object> dtc = destPattern.getPrimaryDateTokenConverter();
        if (dtc.getTimeZone() != null) {
            rc = new RollingCalendar(dtc.getDatePattern(), dtc.getTimeZone(), Locale.getDefault());
        } else {
            rc = new RollingCalendar(dtc.getDatePattern());
        }

        long nextTriggerTime = rc.getNextTriggeringDate(now).getTime();
        long period = rc.getEndOfNextNthPeriod(now, 2).getTime() - nextTriggerTime;
        long delay = nextTriggerTime - now.getTime();

        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                archive();
            }
        }, delay + 100, period, TimeUnit.MILLISECONDS);

        started = true;
    }

    protected void archive() {
        Date now = new Date();
        Date periodStart = rc.getsStartOfCurrentPeriod(now.getTime());
        Date lastPeriodStart = rc.getEndOfNextNthPeriod(periodStart, -1);
        long periodStartInMilliSec = periodStart.getTime();
        long lastPeriodStartInMilliSrc = lastPeriodStart.getTime();

        String zipFileName = destPattern.convert(lastPeriodStart);

        try {
            FileOutputStream fout = new FileOutputStream(zipFileName);
            ZipOutputStream zout = new ZipOutputStream(fout);

            for (DatePatternFileMatcher matcher : srcMatchers) {
                File[] files = matcher.matchFilesInDateRange(lastPeriodStartInMilliSrc, periodStartInMilliSec);
                for (File file : files) {
                    try {
                        if (IOUtils.isZipFile(file)) {
                            // input stream is a zipped file: pipe every entry of it into out stream
                            try {
                                //encryptor.decryptFile("Encrypt"+file.getName(),file.getName());
                                ZipFile zip = new ZipFile(file);
                                Enumeration<? extends ZipEntry> entries = zip.entries();
                                while (entries.hasMoreElements()) {
                                    ZipEntry entry = entries.nextElement();
                                    if (!entry.isDirectory()) {
                                        zout.putNextEntry(entry);
                                        try {
                                            IOUtils.pipe(zip.getInputStream(entry), zout);
                                        } catch (IOException ignore) {
                                        }
                                        zout.closeEntry();
                                    }
                                }
                                zip.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // input stream is not a zipped file: pipe it into out stream
                            FileInputStream fin = new FileInputStream(file);
                            zout.putNextEntry(new ZipEntry(file.getName()));
                            try {
                                IOUtils.pipe(fin, zout);
                            } catch (IOException ignore) {
                            }
                            zout.closeEntry();
                            fin.close();
                        }
                    } catch (FileNotFoundException ignore) {
                    }
                }
            }
            encryptor.encryptFile(zipFileName,"Encrypt"+zipFileName);
            zout.close();
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDestFilePattern() {
        return destPattern.getPattern();
    }

    public String[] getSrcFilePatterns() {
        return srcPatterns;
    }

    public boolean isStarted() {
        return started;
    }

    public static  void main(String[] args){

        String[] dailySourceFilePatterns = new String[]{ "resource/tahiti/message/client_message_%d{yyyy_MM_dd}.log"};

        String dailyDestFilePattern="resource/tahiti_archive/daily/%d{yyyy_MM_dd}.zip";


        RollingArchivePacker r=  new RollingArchivePacker(dailySourceFilePatterns, dailyDestFilePattern);

        r.start();
    }

}
