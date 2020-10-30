package com.arturjarosz.application.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public class Email extends AbstractValueObject<Email> implements Value<Email> {
    private static final long serialVersionUID = -8861608245148282355L;

    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^[a-zA-Z0-9.+/=?^_-]+@[a-zA-Z0-9-]+(\\.[\\w])*(\\.[A-Za-z]{2,})+$");

    @Column(name = "EMAIL")
    private String value;

    public Email() {
        //needed by Hibernate
    }

    public Email(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public Email copy(Email email) {
        return new Email(this.value);
    }
}
