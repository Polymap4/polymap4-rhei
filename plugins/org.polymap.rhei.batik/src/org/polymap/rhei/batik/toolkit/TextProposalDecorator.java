/* 
 * polymap.org
 * Copyright (C) 2014-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.toolkit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.config.Check;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.NumberRangeValidator;

/**
 * Decorates a {@link Text} widget with a proposal popup.
 * <p/>
 * The proposals are supplied by the abstract
 * {@link #proposals(String, int, IProgressMonitor)} method which is called from
 * within an {@link UIJob}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class TextProposalDecorator
        extends Configurable {

    private final static Log log = LogFactory.getLog( TextProposalDecorator.class );
    
    /**
     * True specifies that a {@link SWT#Selection} event is send to the Text control.
     * For {@link AbstractSearchField} this triggers the search to be performed.
     */
    @DefaultBoolean( true )
    public Config2<TextProposalDecorator,Boolean> eventOnAccept;

    /**
     * The delay before the proposal job gets started. So, the time the popup is
     * opened actually is the the delay + proposal job execution time.
     */
    @DefaultInt( 750 )
    @Check( value=NumberRangeValidator.class, args={"0","10000"} )
    public Config2<TextProposalDecorator,Integer> activationDelayMillis;
    
    /**
     * By default the popup size is calculated based on the shell. This may cause
     * problems as the shell is layouted which forces the popup to immediately close.
     * Setting the popup size may help.
     * <p/>
     * This property can be set only once, right after the construction of this
     * {@link TextProposalDecorator}.
     */
    @Immutable
    public Config2<TextProposalDecorator,Point>  popupSize;
    
    private Text                            control;
    
    private SimpleContentProposalProvider   proposalProvider;

    private XContentProposalAdapter         proposal;

    protected String                        currentSearchTxtValue;
    

    public TextProposalDecorator( Text control ) {
        this.control = control;

        // proposal
        proposalProvider = new SimpleContentProposalProvider( new String[0] );
        proposalProvider.setFiltering( false );
        TextContentAdapter controlAdapter = new TextContentAdapter() {
            public void insertControlContents( @SuppressWarnings("hiding") Control control, String text, int cursorPosition ) {
                ((Text)control).setText( text );
                ((Text)control).setSelection( text.length() );
            }
        };
        proposal = new XContentProposalAdapter( control, controlAdapter, proposalProvider, null, null );
        proposal.setPropagateKeys( true );
        proposal.setProposalAcceptanceStyle( ContentProposalAdapter.PROPOSAL_IGNORE );
        // delay until popupSize is filled
        control.getDisplay().asyncExec( () -> {
            popupSize.ifPresent( size -> proposal.setPopupSize( size ) );
        });

        proposal.addContentProposalListener( new IContentProposalListener() {
            public void proposalAccepted( IContentProposal _proposal ) {
                control.setText( _proposal.getContent() + " " );
                control.setSelection( _proposal.getCursorPosition()+1, _proposal.getCursorPosition()+1 );
                
                if (eventOnAccept.get()) {
                    Event event = new Event();
                    event.keyCode = SWT.Selection;
                    event.display = control.getDisplay();
                    event.type = SWT.KeyUp;
                    control.notifyListeners( SWT.KeyUp, event );
                }
            }
        });

        control.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                // close popup: visual feedback for user that key has been recognized;
                // also, otherwise proposal would check in the current entries
                proposalProvider.setProposals( new String[0] );
                // proposal.closeProposalPopup();

                currentSearchTxtValue = control.getText();
                if (currentSearchTxtValue.length() < 1) {
                    proposalProvider.setProposals( new String[0] );
                }
                else {
                    new ProposalJob().schedule( activationDelayMillis.get() );
                }
            }
        });
    }    

    
    public XContentProposalAdapter proposalAdapter() {
        return proposal;
    }


    /**
     * Supplies the proposals. Called from within an {@link UIJob}.
     *
     * @param text
     * @param monitor 
     * @return
     */
    protected abstract String[] proposals( String text, int maxResults, IProgressMonitor monitor );


    /**
     * Updates the ...? 
     */
    class ProposalJob
            extends UIJob {

        private String      value = control.getText();
        
        public ProposalJob() {
            super( "Fulltext proposal" );
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            // skip if control is disposed or no longer focused
            if (control == null || control.isDisposed()) {
                log.info( "Control is disposed." );
                return;
            }
            // skip if search text has changed
            if (value != currentSearchTxtValue) {
                log.info( "Search text has changed: " + value + " -> " + currentSearchTxtValue );
                return;
            }

            // find proposals
            final String[] results = proposals( value, 10, monitor );

            // display
            control.getDisplay().asyncExec( () -> {
                if (!control.isFocusControl()) {
                    log.info( "Control is no longer focused." );
                    return;
                }

                proposalProvider.setProposals( results );

                if (results.length > 0 && !results[0].equals( value )) {
                    proposal.openProposalPopup();
                    //proposal.setProposalPopupFocus();
                }
                else {
                    //proposal.closeProposalPopup();
                }
            });
        }        
    }

    
    /**
     * Expose some protected methods.
     */
    public static class XContentProposalAdapter
            extends ContentProposalAdapter {

        public XContentProposalAdapter( Control control, IControlContentAdapter controlContentAdapter,
                IContentProposalProvider proposalProvider, KeyStroke keyStroke, char[] autoActivationCharacters ) {
            super( control, controlContentAdapter, proposalProvider, keyStroke, autoActivationCharacters );
        }

        @Override
        protected void closeProposalPopup() {
            super.closeProposalPopup();
        }

        @Override
        protected void openProposalPopup() {
            super.openProposalPopup();
        }
        
    }

}
