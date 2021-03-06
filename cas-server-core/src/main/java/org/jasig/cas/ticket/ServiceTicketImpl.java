/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;


/**
 * Domain object representing a Service Ticket. A service ticket grants specific
 * access to a particular service. It will only work for a particular service.
 * Generally, it is a one time use Ticket, but the specific expiration policy
 * can be anything.
 *
 * @author Scott Battaglia

 * @since 3.0
 */
@Entity
@Table(name="SERVICETICKET")
public final class ServiceTicketImpl extends AbstractServiceTicket implements
    ServiceTicket {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -4223319704861765405L;

    /** The TicketGrantingTicket this is associated with. */
    @ManyToOne(targetEntity = TicketGrantingTicketImpl.class)
    private TicketGrantingTicket ticketGrantingTicket;

    /** The service this ticket is valid for. */
    @Lob
    @Column(name="SERVICE", nullable=false)
    private Service service;

    /** Is this service ticket the result of a new login. */
    @Column(name="FROM_NEW_LOGIN", nullable=false)
    private boolean fromNewLogin;

    /**
     * Instantiates a new service ticket impl.
     */
    public ServiceTicketImpl() {
        // exists for JPA purposes
    }

    /**
     * Constructs a new ServiceTicket with a Unique Id, a TicketGrantingTicket,
     * a Service, Expiration Policy and a flag to determine if the ticket
     * creation was from a new Login or not.
     *
     * @param id the unique identifier for the ticket.
     * @param ticketGrantingTicket the TicketGrantingTicket parent.
     * @param service the service this ticket is for.
     * @param fromNewLogin is it from a new login.
     * @param policy the expiration policy for the Ticket.
     * @throws IllegalArgumentException if the TicketGrantingTicket or the
     * Service are null.
     */
    protected ServiceTicketImpl(final String id,
        final TicketGrantingTicket ticketGrantingTicket, final Service service,
        final boolean fromNewLogin, final ExpirationPolicy policy) {
        super(id, policy);

        Assert.notNull(ticketGrantingTicket, "ticket cannot be null");
        Assert.notNull(service, "service cannot be null");

        this.ticketGrantingTicket = ticketGrantingTicket;
        this.service = service;
        this.fromNewLogin = fromNewLogin;
    }

    public boolean isFromNewLogin() {
        return this.fromNewLogin;
    }

    public Service getService() {
        return this.service;
    }

    /**
     * {@inheritDoc}
     * <p>The state of the ticket is affected by this operation and the
     * ticket will be considered used regardless of the match result.
     * The state update subsequently may impact the ticket expiration
     * policy in that, depending on the policy configuration, the ticket
     * may be considered expired.
     */
    @Override
    public boolean isValidFor(final Service serviceToValidate) {
        updateState();
        return serviceToValidate.matches(this.service);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (!(object instanceof ServiceTicket)) {
            return false;
        }

        final Ticket ticket = (Ticket) object;

        return new EqualsBuilder()
                .append(ticket.getId(), this.getId())
                .isEquals();
    }

    public Authentication getAuthentication() {
        return null;
    }

    public TicketGrantingTicket getGrantingTicket() {
        return this.ticketGrantingTicket;
    }

}
