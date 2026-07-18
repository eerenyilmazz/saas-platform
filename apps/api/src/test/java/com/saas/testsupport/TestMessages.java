package com.saas.testsupport;

import org.springframework.context.support.ResourceBundleMessageSource;

import com.saas.core.i18n.Messages;

/** A real, non-mocked Messages backed by the actual messages_tr.properties - for plain unit tests
 * that construct a class needing Messages without a Spring context. */
public final class TestMessages {

    private TestMessages() {
    }

    public static Messages create() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return new Messages(messageSource);
    }
}
