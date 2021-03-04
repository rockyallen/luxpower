package solar.model;

import java.text.DecimalFormat;
import javax.measure.Quantity;
import javax.measure.format.UnitFormat;
import tech.units.indriya.format.SimpleUnitFormat;

/**
 * Pretty printer for a quantity.
 *
 * @design Mutable
 *
 * @todo Looks like a value class with a fluent builder but isn't. Make it one?
 *
 * @author rocky
 */
public class QuantityFormatter {

    DecimalFormat df = new DecimalFormat();
    boolean append = true;
    boolean prepend = false;
    UnitFormat uf = SimpleUnitFormat.getInstance();
    String separator = " ";

    public QuantityFormatter decimalFormat(DecimalFormat df) {
        this.df = df;
        return this;
    }

    public QuantityFormatter prependUnits(boolean b) {
        prepend = b;
        return this;
    }

    public QuantityFormatter appendUnits(boolean b) {
        append = b;
        return this;
    }

    public QuantityFormatter unitFormat(UnitFormat uf) {
        this.uf = uf;
        return this;
    }

    public QuantityFormatter separator(String s) {
        this.separator = s;
        return this;
    }

    public QuantityPrettyPrinter getFormatter() {
        return new QuantityPrettyPrinter() {
            @Override
            public String toString(Quantity q) {
                StringBuilder sb = new StringBuilder();
                if (prepend) {
                    sb.append(uf.format(q.getUnit())).append(separator);
                }
                sb.append(df.format(q.getValue().doubleValue()));
                if (append) {
                    sb.append(separator).append(uf.format(q.getUnit()));
                }
                return sb.toString();
            }
        };
    }
}
