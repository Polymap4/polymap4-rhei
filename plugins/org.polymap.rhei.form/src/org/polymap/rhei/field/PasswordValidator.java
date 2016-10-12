/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.Mandatory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PasswordValidator
        extends Configurable
        implements IFormFieldValidator {
    
    @Mandatory
    @DefaultBoolean(true)
    public Config2<PasswordValidator,Boolean>   oneDigit;
    
    @Mandatory
    @DefaultBoolean(true)
    public Config2<PasswordValidator,Boolean>   oneLowerCase;
    
    @Mandatory
    @DefaultBoolean(true)
    public Config2<PasswordValidator,Boolean>   oneUpperCase;
    
    @Mandatory
    @DefaultBoolean(false)
    public Config2<PasswordValidator,Boolean>   oneSpecial;
    
    @Mandatory
    @DefaultBoolean(true)
    public Config2<PasswordValidator,Boolean>   noWhitespace;

    @Mandatory
    @DefaultInt(8)
    public Config2<PasswordValidator,Integer>   minLength;

    /**
     * The message to be send by {@link #validate(Object)} if given password is
     * invalid. Default is created from current configuration if null.
     */
    public Config2<PasswordValidator,String>    msg;

    private Lazy<Pattern>                       pattern;
    

    /**
     * Constructs a new instance with default settings.
     */
    public PasswordValidator() {
        // compile after configuration has been done
        pattern = new PlainLazyInit( () -> {
            return Pattern.compile(
                "^" +   
                (oneDigit.get()     ? "(?=.*[0-9])" : "") +
                (oneLowerCase.get() ? "(?=.*[a-z])" : "") +
                (oneUpperCase.get() ? "(?=.*[A-Z])" : "") +
                (oneSpecial.get()   ? "(?=.*[@#$%^&+=])" : "") +
                (noWhitespace.get() ? "(?=\\S+$)" : "") +
                ".{" + minLength.get() + ",}" +
                "$" );
        });
    }

    @Override
    public String validate( Object fieldValue ) {
        Matcher matcher = pattern.get().matcher( fieldValue != null ? fieldValue.toString() : "_dontMatch_" );
        if (matcher.matches()) {
            return null; 
        }
        else if (msg.isPresent()) {
            return msg.get();
        }
        else {
            return "Password is not valid";
        }
    }

    @Override
    public Object transform2Model( Object fieldValue ) throws Exception {
        return fieldValue;
    }

    @Override
    public Object transform2Field( Object modelValue ) throws Exception {
        return modelValue;
    }
    
}
