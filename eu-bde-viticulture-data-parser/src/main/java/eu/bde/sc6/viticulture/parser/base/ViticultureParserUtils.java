package eu.bde.sc6.viticulture.parser.base;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.openrdf.model.Literal;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author http://www.turnguard.com/turnguard
 */
public class ViticultureParserUtils {
    public static Literal createDateTimeLiteral(String year, String month, String day){
        Calendar calendar = new GregorianCalendar();

        calendar.set(
                Integer.parseInt(year), 
                Integer.parseInt(month)-1, 
                Integer.parseInt(day),
                12,
                0,
                0
        );

        return ValueFactoryImpl.getInstance().createLiteral(calendar.getTime());
    }
}
