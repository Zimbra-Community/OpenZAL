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

import java.io.File;
import java.util.*;

import org.jetbrains.annotations.Nullable;
import org.openzal.zal.exceptions.*;
import org.openzal.zal.exceptions.ZimbraException;

import com.zimbra.cs.volume.*;

public class VolumeManager
{
  private static final short sMboxGroupBits = 8;
  private static final short sMboxBits = 12;
  private static final short sFileGroupBits = 8;
  private static final short sFileBits = 12;

  private final com.zimbra.cs.volume.VolumeManager  mVolumeManager;

  public VolumeManager()
  {
    mVolumeManager = com.zimbra.cs.volume.VolumeManager.getInstance();
  }

  public List<StoreVolume> getAll()
  {
    List<Volume> list = mVolumeManager.getAllVolumes();

    return ZimbraListWrapper.wrapVolumes(list);
  }

  public StoreVolume update(String id, short type,
                            String name, String path,
                            boolean compressBlobs, long compressionThreshold)
    throws ZimbraException
  {

    StoreVolume volumeToUpdate = getById(id);

    Volume vol;
    try
    {
      Volume.Builder builder = Volume.builder();
      builder.setId(Short.parseShort(id));
      builder.setName(name);
      builder.setType(type);
      if (!path.startsWith(File.separator))
      {
        builder.setPath(path, false);
      }
      else
      {
        builder.setPath(path, true);
      }
      builder.setMboxGroupBits(volumeToUpdate.getMboxGroupBits());
      builder.setMboxBit(volumeToUpdate.getMboxBits());
      builder.setFileGroupBits(volumeToUpdate.getFileGroupBits());
      builder.setFileBits(volumeToUpdate.getFileBits());
      builder.setCompressBlobs(compressBlobs);
      builder.setCompressionThreshold(compressionThreshold);
      vol = builder.build();
      vol = mVolumeManager.update(vol);
    }
    catch (com.zimbra.cs.volume.VolumeServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
    catch (com.zimbra.common.service.ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
    return new StoreVolume(vol);
  }

  public StoreVolume create(short id, short type,
                            String name, String path,
                            boolean compressBlobs, long compressionThreshold)
    throws ZimbraException
  {
    Volume vol;
    try
    {
      Volume.Builder builder = Volume.builder();
      builder.setId(id);
      builder.setType(type);
      builder.setName(name);
      if (!path.startsWith(File.separator))
      {
        builder.setPath(path, false);
      }
      else
      {
        builder.setPath(path, true);
      }
      builder.setMboxGroupBits(sMboxGroupBits);
      builder.setMboxBit(sMboxBits);
      builder.setFileGroupBits(sFileGroupBits);
      builder.setFileBits(sFileBits);
      builder.setCompressBlobs(compressBlobs);
      builder.setCompressionThreshold(compressionThreshold);

      vol = builder.build();
      vol = mVolumeManager.create(vol);
    }
    catch (com.zimbra.cs.volume.VolumeServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
    catch (com.zimbra.common.service.ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
    return new StoreVolume(vol);
  }

  public void setCurrentVolume(short volType, short id)
    throws ZimbraException
  {
    try
    {
      mVolumeManager.setCurrentVolume(volType, id);
    }
    catch (com.zimbra.common.service.ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public void setCurrentSecondaryVolume(String id)
    throws ZimbraException
  {
    Short volType = Short.valueOf("2");
    try
    {
      mVolumeManager.setCurrentVolume(volType, Short.valueOf(id));
    }
    catch (com.zimbra.common.service.ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean delete(String id) throws ZimbraException
  {
    try
    {
      return mVolumeManager.delete(Short.valueOf(id));
    }
    catch (com.zimbra.common.service.ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public StoreVolume getById(String vid) throws org.openzal.zal.exceptions.ZimbraException
  {
    try
    {
      return new StoreVolume(mVolumeManager.getVolume(vid));
    }
    catch (com.zimbra.common.service.ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  @Nullable
  public StoreVolume getCurrentSecondaryMessageVolume()
  {
    Volume vol = mVolumeManager.getCurrentSecondaryMessageVolume();

    if( vol != null )
    {
      return new StoreVolume(vol);
    }
    else
    {
      return null;
    }
  }

  public boolean hasSecondaryMessageVolume()
  {
    return getCurrentSecondaryMessageVolume() != null;
  }

  public List<StoreVolume> getByType(short type)
  {
    List<Volume> list = mVolumeManager.getAllVolumes();

    ArrayList<StoreVolume> newList = new ArrayList<StoreVolume>(list.size());
    for( Volume vol : list )
    {
      if( vol.getType() == type ) {
        newList.add(new StoreVolume(vol));
      }
    }
    return newList;
  }

  public StoreVolume getCurrentMessageVolume()
  {
    return new StoreVolume(mVolumeManager.getCurrentMessageVolume());
  }

  public StoreVolume getCurrentIndex()
  {
    return new StoreVolume(mVolumeManager.getCurrentIndexVolume());
  }

  public StoreVolume getVolumeByName(String volumeName)
  {
    for (StoreVolume storeVolume : getAll())
    {
      if (storeVolume.getName().equals(volumeName))
      {
        return storeVolume;
      }
    }

    return null;
  }

  public boolean isValidVolume(String id)
  {
    // check type message?
    List<StoreVolume> volumeList = getAll();

    for (StoreVolume v:volumeList){
      if (v.getId().equals(id))
      {
        return true;
      }
    }

    return false;
  }
}
