package org.openecard.client.common.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class HTMLFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();
        builder.append("        <tr>");
        builder.append("\n");
        builder.append("          <td>");
        builder.append(record.getLevel());
        builder.append("</td>");
        builder.append("\n");
        builder.append("          <td>");
        builder.append(calcDate(record.getMillis()));
        builder.append("</td>");
        builder.append("\n");
        builder.append("          <td>");
        builder.append(formatMessage(record));
        builder.append("</td>");
        builder.append("\n");
        builder.append("        <tr>");
        builder.append("\n");
        return builder.toString();
    }

    @Override
    public String getHead(Handler h) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
                + "\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "\n"
                + "  <head>"
                + "\n"
                + "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\" />"
                + "\n"
                + "    <title>Log " + calcDate(System.currentTimeMillis()) + "</title>"
                + "\n"
                + "  </head>"
                + "\n"
                + "  <body>"
                + "\n"
                + "    <table style=\"text-align: left; width: 1000px; margin: auto;\" border=\"1\" cellpadding=\"1\" cellspacing=\"0\">"
                + "\n"
                + "      <thead style=\"text-align: center;\">"
                + "\n"
                + "        <tr>"
                + "\n"
                + "          <th>Log Level</th>"
                + "\n"
                + "          <th>Date</th>"
                + "\n"
                + "          <th>Message</th>"
                + "\n"
                + "        </tr>"
                + "\n"
                + "      </thead>"
                + "\n"
                + "      <tbody>"
                + "\n";
    }

    @Override
    public String getTail(Handler h) {
        return "      </tbody>"
                + "\n"
                + "    </table>"
                + "\n"
                + "  </body>"
                + "\n"
                + "</html>";
    }

    private String calcDate(long millisecs) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
        Date date = new Date(millisecs);
        return sdf.format(date);
    }
}
