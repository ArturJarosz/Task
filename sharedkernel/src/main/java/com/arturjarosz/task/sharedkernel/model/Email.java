package com.arturjarosz.task.sharedkernel.model;

import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.util.regex.Pattern;

@Embeddable
public class Email extends AbstractValueObject<Email> implements ValueObject<Email> {
    @Serial
    private static final long serialVersionUID = -8861608245148282355L;

    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^[\\w+_.?^-]+@+\\w+\\.+(\\w{2,})");

    @Column(name = "EMAIL")
    private String value;

    protected Email() {
        // needed by JPA
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
    public Email copy() {
        return new Email(this.value);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != this.getClass()) {
            return false;
        }

        Email other = (Email) object;

        return this.hasSameValueAs(other);
    }
}
