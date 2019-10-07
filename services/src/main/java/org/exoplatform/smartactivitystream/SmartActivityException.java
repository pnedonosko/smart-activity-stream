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
package org.exoplatform.smartactivitystream;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: SmartActivityException.java 00000 Oct 2, 2019 pnedonosko $
 * 
 */
public class SmartActivityException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -4858796558817116020L;

  /**
   * 
   */
  public SmartActivityException() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public SmartActivityException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public SmartActivityException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public SmartActivityException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public SmartActivityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    // TODO Auto-generated constructor stub
  }

}
