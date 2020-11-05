package com.arturjarosz.application.model;

import com.arturjarosz.application.exceptions.BaseValidator;
import com.arturjarosz.application.exceptions.ExceptionCodes;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public class Email extends AbstractValueObject<Email> implements ValueObject<Email> {
    private static final long serialVersionUID = -8861608245148282355L;

    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^[a-zA-Z0-9.+/=?^_-]+@[a-zA-Z0-9-]+(\\.[\\w])*(\\.[A-Za-z]{2,})+$");

    @Column(name = "EMAIL")
    private String value;

    public Email() {
        //needed by Hibernate
    }

    public Email(String value) {
        this.setValue(value);
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        BaseValidator.assertIsTrue(this.isValid(value),
                BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID, ModelExceptionCodes.EMAIL), value);
        this.value = value;
    }

    public boolean isValid(String email) {
        if (email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.value).toHashCode();
    }

    @Override
    public boolean hasSameValueAs(Email other) {
        return this.value.equals(other.getValue());
    }

    @Override
    public Email copy(Email email) {
        return new Email(this.value);
    }
}
