package com.saas.core.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * ADR 0006 / localization.md: user-facing text is never a Java string literal, even though the
 * system targets a single fixed locale (tr-TR, no language switcher) today - resolves an
 * error.code to its messages_tr.properties text so a second language later only means adding a
 * new properties file, not hunting through Java source for embedded strings.
 */
@Component
@RequiredArgsConstructor
public class Messages {

    private static final Locale TURKISH = Locale.forLanguageTag("tr");

    private final MessageSource messageSource;

    public String get(String code) {
        return messageSource.getMessage(code, null, TURKISH);
    }
}
