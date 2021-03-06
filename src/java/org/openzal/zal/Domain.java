/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2016 ZeXtras S.r.l.
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

import org.jetbrains.annotations.Nullable;
import org.openzal.zal.exceptions.ExceptionWrapper;
import com.zimbra.common.service.ServiceException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Domain extends Entry
{
  @NotNull private final com.zimbra.cs.account.Domain mDomain;

  public Domain(@NotNull Object domain)
  {
    super(domain);
    mDomain = (com.zimbra.cs.account.Domain) domain;
  }

  public Domain(
    String name,
    String id,
    Map<String, Object> attrs,
    Map<String, Object> defaults,
    @NotNull Provisioning prov
  )
  {
    this(
      new com.zimbra.cs.account.Domain(
        name,
        id,
        attrs,
        defaults,
        prov.toZimbra(com.zimbra.cs.account.Provisioning.class)
      )
    );
  }

  public void unsetPasswordChangeListener()
  {
    try
    {
      mDomain.unsetPasswordChangeListener();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public String getDomainDefaultCOSId()
  {
    return mDomain.getDomainDefaultCOSId();
  }

  public String getId()
  {
    return mDomain.getId();
  }

  public String getName()
  {
    return mDomain.getName();
  }

  @NotNull
  public Map<String, Object> getAttrs(boolean applyDefaults)
  {
    return new HashMap<String, Object>(mDomain.getAttrs(applyDefaults));
  }

  public long getMailDomainQuota()
  {
    return mDomain.getMailDomainQuota();
  }

  public void setDomainCOSMaxAccounts(@NotNull Collection<String> zimbraDomainCOSMaxAccounts)
  {
    try
    {
      mDomain.setDomainCOSMaxAccounts(zimbraDomainCOSMaxAccounts.toArray(new String[zimbraDomainCOSMaxAccounts.size()]));
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nullable
  public String getDomainAliasTargetId()
  {
    return mDomain.getDomainAliasTargetId();
  }

  public boolean isAliasDomain()
  {
    return mDomain.getDomainAliasTargetId() != null;
  }

  public int getDomainMaxAccounts()
  {
    return mDomain.getDomainMaxAccounts();
  }

  public void setDomainTypeAsString(String zimbraDomainType)
  {
    try
    {
      mDomain.setDomainTypeAsString(zimbraDomainType);
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @NotNull
  public Collection<String> getDomainCOSMaxAccounts()
  {
    return Arrays.asList(mDomain.getDomainCOSMaxAccounts());
  }

  public String getPasswordChangeListener()
  {
    return mDomain.getPasswordChangeListener();
  }

  @NotNull
  public List<Account> getAllAccounts()
  {
    try
    {
      return ZimbraListWrapper.wrapAccounts(mDomain.getAllAccounts());
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Override
  com.zimbra.cs.account.Domain toZimbra()
  {
    return mDomain;
  }

  public long getLongAttr(String name, int defaultValue)
  {
    return mDomain.getLongAttr(name, defaultValue);
  }

  public String getPublicHostname()
  {
    return mDomain.getAttr(ProvisioningImp.A_zimbraPublicServiceHostname, null);
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;

    Domain domain = (Domain) o;

    return mDomain.getId().equals(domain.mDomain.getId());

  }

  @Override
  public int hashCode()
  {
    return mDomain.getId().hashCode();
  }
}

