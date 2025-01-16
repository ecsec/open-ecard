/*
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.ws;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *
 * @author Tobias Wich
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ECardApiMethod {

    /**
     * Name of the wsdl:operation matching this method.
     */
    String operationName() default "";

    /**
     * The action for this operation.
     * <p>
     * For SOAP bindings, this determines the value of the soap action.
     */
    String action() default "";

    /**
     * Marks a method to NOT be exposed as a web method.
     * <p>
     * Used to stop an inherited method from being exposed as part of this web service.
     * If this element is specified, other elements MUST NOT be specified for the @WebMethod.
     * <p>
     * <i>This member-value is not allowed on endpoint interfaces.</i>
     *
     * @since 2.0
     */
    boolean exclude() default false;

}
