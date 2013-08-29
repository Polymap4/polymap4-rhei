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
package org.polymap.rhei.um.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;

import org.polymap.rhei.um.Messages;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.auth.PasswordEncryptor;

/**
 * Creates the given user in the
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewUserOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( NewUserOperation.class );

    public static final IMessages i18n = Messages.forPrefix( "NewUserOperation" );

    private User            user;

    
    public NewUserOperation( User user ) {
        super( i18n.get( "title" ) );
        this.user = user;
    }

    
    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        // password hash
        PasswordEncryptor encryptor = PasswordEncryptor.instance();
        String password = encryptor.createPassword( 8 );
        String hash = encryptor.encryptPassword( password );
        user.passwordHash().set( hash );
        
        // username = email
        String username = user.email().get();
        user.username().set( username );
        
        // commit
        UserRepository.instance().commitChanges();
        
        // XXX email
        
        return Status.OK_STATUS;
    }

    
    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
