package com.mic.server;

import java.util.regex.Pattern;

public class PatternConst {

    public static final String CHARSET_REGEX = "[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;]*)['|\"]?";

    public static final Pattern CHARSET_PATTERN = Pattern.compile(CHARSET_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String BOUNDARY_REGEX = "[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;]*)['|\"]?";

    public static final Pattern BOUNDARY_PATTERN = Pattern.compile(BOUNDARY_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";

    public static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile(CONTENT_DISPOSITION_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";

    public static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(CONTENT_TYPE_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";

    public static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile(CONTENT_DISPOSITION_ATTRIBUTE_REGEX);
}
