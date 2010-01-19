/*
 * TrackerShutdown - Makes sure everything ends correctly when shutting down.
 * Copyright (C) 2003 Mark J. Wielaard
 * 
 * This file is part of Snark.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.klomp.snark;

import java.util.logging.Logger;

/**
 * Makes sure everything ends correctly when shutting down.
 */
public class SnarkSeederShutdown extends Thread
{
    private final SnarkSeeder seeder;

    private final ShutdownListener listener;

    public SnarkSeederShutdown (SnarkSeeder snark, ShutdownListener listener)
    {
        this.seeder = snark;
        this.listener = listener;
    }

    @Override
    public void run ()
    {
        seeder.halt();

        if (listener != null)
        	listener.shutdown();
    }

    /** The Java logger used to process our log events. */
    protected static final Logger log = Logger.getLogger("org.klomp.snark");
}
