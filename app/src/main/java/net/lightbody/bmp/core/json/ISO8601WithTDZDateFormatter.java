package net.lightbody.bmp.core.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Damien Jubeau <damien.jubeau@dareboost.com>
 * Allows Date Format to be compliant with Har 1.2 Spec : ISO 8601 with Time Zone Designator
 * @see <a href="https://github.com/lightbody/browsermob-proxy/issues/44">https://github.com/lightbody/browsermob-proxy/issues/44</a>
 *
 */
public class ISO8601WithTDZDateFormatter extends JsonSerializer<Date> {

    @Override
    public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(format(value));
    }

    private String format(Date date) {
        final String ISO8061_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        final SimpleDateFormat sdf = new SimpleDateFormat(ISO8061_FORMAT);
        return sdf.format(date);
    }
}
