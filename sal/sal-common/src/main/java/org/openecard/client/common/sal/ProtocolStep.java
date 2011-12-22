package org.openecard.client.common.sal;

import iso.std.iso_iec._24727.tech.schema.RequestType;
import iso.std.iso_iec._24727.tech.schema.ResponseType;
import java.util.Map;


/**
 * Interface which must be implemented to perform a step in the protocol.<br/>
 * The request and response parameters are parameterized, so that the actual type becomes apparent in the step.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface ProtocolStep <Request extends RequestType, Response extends ResponseType> {

    /**
     * Get the type of SAL function this step can be applied to.
     * @return SAL function type.
     */
    public FunctionType getFunctionType();

    /**
     * @param request Request object from the actual SAL function.
     * @param internalData Map with objects from previous steps.
     * @return Response object for the actual SAL function.
     */
    public Response perform(Request request, Map<String,Object> internalData);

}
