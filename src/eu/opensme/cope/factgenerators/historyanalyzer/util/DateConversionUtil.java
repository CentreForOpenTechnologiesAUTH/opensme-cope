package eu.opensme.cope.factgenerators.historyanalyzer.util;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: george
 * Date: 29/12/2010
 * Time: 9:24 πμ
 * To change this template use File | Settings | File Templates.
 */
public class DateConversionUtil {
    public static Date convertSVNSData2JavaDate(String sdate) throws IllegalArgumentException {
        java.util.Date utilDate = null;

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            utilDate = formatter.parse(sdate);
            System.out.println("utilDate:" + utilDate);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return utilDate;
    }
}
