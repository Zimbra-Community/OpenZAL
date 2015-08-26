/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2014 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzal.zal;

import com.zextras.mobile.v2.it.base.ContinuationHttpServletRequest;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

public final class Jetty
{
  @NotNull
  public static Continuation getContinuation( HttpServletRequest req )
  {
    if( req instanceof ContinuationHttpServletRequest)
    {
      return ((ContinuationHttpServletRequest)req).getContinuation();
    }
    else
    {
      return new ContinuationJetty(req);
    }
  }
}