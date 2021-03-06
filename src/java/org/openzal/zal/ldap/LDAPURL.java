package org.openzal.zal.ldap;

import org.jetbrains.annotations.NotNull;

public class LDAPURL
{
  @NotNull
  private final com.unboundid.ldap.sdk.LDAPURL mLDAPURL;

  public LDAPURL(@NotNull Object mLdapUrl)
  {
    mLDAPURL = (com.unboundid.ldap.sdk.LDAPURL)mLdapUrl;
  }

  public LDAPURL(@NotNull String url) throws LDAPException
  {
    try
    {
      mLDAPURL = new com.unboundid.ldap.sdk.LDAPURL(url);
    }
    catch (com.unboundid.ldap.sdk.LDAPException e)
    {
      throw new LDAPException(e);
    }
  }

  protected <T> T toZimbra(Class<T> cls)
  {
    return cls.cast(mLDAPURL);
  }

  public String getHost()
  {
    return mLDAPURL.getHost();
  }

  public int getPort()
  {
    return mLDAPURL.getPort();
  }
}
