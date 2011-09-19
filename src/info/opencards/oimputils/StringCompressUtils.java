package info.opencards.oimputils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


/**
 * Some static utility function to  inflate/deflate strings.
 *
 * @author Holger Brandl
 */
public class StringCompressUtils {

    public static char[] hexChar = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'
    };


    public static String compress2(String s) {
        Deflater defl = new Deflater(Deflater.BEST_COMPRESSION);
        defl.setInput(s.getBytes());
        defl.finish();
        boolean done = false;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (!done) {
            byte[] buf = new byte[256];
            int bufnum = defl.deflate(buf);
            bos.write(buf, 0, bufnum);
            if (bufnum < buf.length)
                done = true;
        }
        try {
            bos.flush();
            bos.close();
        }
        catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
        return toHexString(bos.toByteArray());
    }


    public static String uncompress2(String compressedHex) {
        byte[] b = toBinArray(compressedHex);

        Inflater infl = new Inflater();
        infl.setInput(b);

        StringBuffer retval = new StringBuffer();
        boolean done = false;
        while (!done) {
            byte[] buf = new byte[256];
            try {
                int bufnum = infl.inflate(buf);
                retval.append(new String(buf, 0, bufnum));
                if (bufnum < buf.length)
                    done = true;
            }
            catch (DataFormatException dfe) {
                done = true;
                System.err.println(dfe.toString());
            }
        }

        return (retval.toString());
    }


    public static String toHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            // look up high nibble char
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]); // fill left with zero bits

            // look up low nibble char
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }


    public static byte[] toBinArray(String hexStr) {
        byte bArray[] = new byte[hexStr.length() / 2];
        for (int i = 0; i < (hexStr.length() / 2); i++) {
            byte firstNibble = Byte.parseByte(hexStr.substring(2 * i, 2 * i + 1), 16); // [x,y)
            byte secondNibble = Byte.parseByte(hexStr.substring(2 * i + 1, 2 * i + 2), 16);
            int finalByte = (secondNibble) | (firstNibble << 4); // bit-operations only with numbers, not bytes.
            bArray[i] = (byte) finalByte;
        }
        return bArray;
    }


    private static void reallyLastEval() {
        String input = "hallo world";
        String compressedHex = compress2(input);

        String undoCompressed = uncompress2(compressedHex);
        System.out.println("before=" + input);
        System.out.println("after=" + undoCompressed);
    }
}
