package info.opencards.oimputils.test;

import com.thoughtworks.xstream.converters.basic.DoubleConverter;

import java.text.DecimalFormat;


/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
class ShortFloatConverter extends DoubleConverter {

    final static DecimalFormat df = new DecimalFormat("0.00");


    public java.lang.String toString(java.lang.Object o) {
        return df.format(o);
    }
}
