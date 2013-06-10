package it.webookia.backend.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Settings {
    public final static String CURRENT_HOST = "http://87.8.47.42:8888";

    public final static int LOAN_PAGINATION_SIZE = 15;

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat(
        "dd/MM/yyyy HH:mm");
}