/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.smartactivitystream.command;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: CommandThreadFactory.java 00000 Oct 3, 2019 pnedonosko $
 * 
 */
/**
 * Command thread factory adapted from {@link Executors#DefaultThreadFactory}.
 */
public class CommandThreadFactory implements ThreadFactory {

  /** The group. */
  protected final ThreadGroup   group;

  /** The thread number. */
  protected final AtomicInteger threadNumber = new AtomicInteger(1);

  /** The name prefix. */
  protected final String        namePrefix;

  /**
   * Instantiates a new command thread factory.
   *
   * @param namePrefix the name prefix
   */
  public CommandThreadFactory(String namePrefix) {
    SecurityManager s = System.getSecurityManager();
    this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    this.namePrefix = namePrefix;
  }

  public Thread newThread(Runnable r) {
    Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0) {

      /**
       * {@inheritDoc}
       */
      @Override
      protected void finalize() throws Throwable {
        super.finalize();
        threadNumber.decrementAndGet();
      }

    };
    if (t.isDaemon()) {
      t.setDaemon(false);
    }
    if (t.getPriority() != Thread.NORM_PRIORITY) {
      t.setPriority(Thread.NORM_PRIORITY);
    }
    return t;
  }
}
