package ru.rt.eip.simpletariffs.validation.service;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ItcLcrDateFormat {

    private static final String dateFormat = "yyyy-MM-dd";

    private final Date date;

    private ItcLcrDateFormat(Date date) {
        this.date = date;
    }

    public static ItcLcrDateFormat from(Date date) {
        return new ItcLcrDateFormat(date);
    }

    public String format() {
        return new SimpleDateFormat(dateFormat).format(this.date);
    }

}
