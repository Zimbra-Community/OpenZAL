package com.zimbra.cs.store;

import com.zextras.lib.Error.MissingReadPermissions;
import com.zextras.lib.Error.MissingWritePermissions;
import com.zextras.lib.vfs.FileStreamWriter;
import com.zextras.lib.vfs.OutputStreamFileWriterWrapper;
import com.zextras.lib.vfs.RelativePath;
import com.zextras.lib.vfs.ramvfs.RamFS;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.file.FileBlobStore;
/* $if ZimbraVersion >= 8.0.0 $ */
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
/* $else$
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.store.*;
import com.zimbra.cs.store.file.*;
 $endif$ */
import io.netty.buffer.Unpooled;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Exception;
import java.lang.RuntimeException;
import java.nio.channels.FileChannel;
import java.util.UUID;


public final class StoreManagerSimulator extends StoreManager {

  private final RamFS mStoreRoot;

  public RamFS getStoreRoot()
  {
    return mStoreRoot;
  }

  public StoreManagerSimulator()
  {
//        DebugConfig.disableMessageStoreFsync = true;
    mStoreRoot = new RamFS();
  }

  public void startup() throws IOException
  {
    BlobInputStream.setFileDescriptorCache(new FileDescriptorCache(null));
  }

  public void shutdown()
  {
    purge();
    BlobInputStream.setFileDescriptorCache(null);
  }

  /* $if ZimbraVersion >= 7.2.0 $ */
  public boolean supports(StoreFeature feature)
  {
    return false;
  }
/* $endif$ */

  public void purge()
  {
    try
    {
      ((RamFS)mStoreRoot).getRoot().removeRecursive();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    catch (MissingWritePermissions missingWritePermissions)
    {
      throw new RuntimeException(missingWritePermissions);
    }
  }

  public BlobBuilder getBlobBuilder()
  {
    return new MockBlobBuilder();
  }

  public Blob storeIncoming(InputStream data, boolean storeAsIs) throws IOException
  {
    com.zextras.lib.vfs.File file = mStoreRoot.getRoot().resolveFile(
      UUID.randomUUID().toString()
    );
    MockBlob mockblob = new MockBlob();
    mockblob.setFile(file);

    OutputStream writer;
    try
    {
      writer = file.openOutputStreamWrapper();
    }
    catch (MissingWritePermissions missingWritePermissions)
    {
      throw new IOException(missingWritePermissions);
    }
    try
    {
      IOUtils.copy(data, writer);
    }
    finally
    {
      writer.close();
    }

    return mockblob;
  }

/* $if ZimbraVersion < 8.0.0 $
  public Blob storeIncoming(InputStream data, long sizeHint, StorageCallback callback, boolean storeAsIs)
    throws IOException, ServiceException
  {
    return storeIncoming(data, storeAsIs);
  }

  public Blob storeIncoming(InputStream data, StorageCallback callback, boolean storeAsIs)
    throws IOException, ServiceException
  {
    return storeIncoming(data, storeAsIs);
  }

  public StagedBlob stage(InputStream data, long actualSize, StorageCallback callback, Mailbox mbox)
    throws IOException, ServiceException
  {
    return stage(data, actualSize, mbox );
  }

  public StagedBlob stage(InputStream data, StorageCallback callback, Mailbox mbox)
    throws IOException, ServiceException
  {
    return stage(data, 0, mbox );
  }
 $endif$ */

  public StagedBlob stage(InputStream data, long actualSize, Mailbox mbox) throws IOException
  {
    return new MockStagedBlob(mbox,(MockBlob)storeIncoming(data,false));
  }

  public StagedBlob stage(Blob blob, Mailbox mbox)
  {
    return new MockStagedBlob(mbox,(MockBlob)blob);
  }

  /* $if ZimbraVersion >= 8.0.0 $*/
  private String getBlobDir(short volumeId, int mboxId, int itemId )
  {
    Volume vol;
    try
    {
      vol = VolumeManager.getInstance().getVolume(volumeId);
      return vol.getBlobDir(mboxId, itemId);
    }
    catch (ServiceException e)
    {
      throw new RuntimeException(e);
    }
  }
/* $else$

  private String getBlobDir(short volumeId, long mboxId, int itemId )
  {
    try
    {
      Volume vol = Volume.getById(volumeId);
      return vol.getBlobDir((int)mboxId, itemId);
    }
    catch(ServiceException e)
    {
      throw new RuntimeException(e);
    }
  }

  $endif$ */


  public RelativePath getBlobPath(
    long mboxId,
    int itemId,
    int revision,
    short volumeId
  )
  {
    String path = getBlobDir(volumeId, (int)mboxId, itemId);

    int buflen = path.length() + 15 + (revision < 0 ? 0 : 11);
    StringBuilder sb = new StringBuilder(buflen);

    sb.append(path).append(File.separator).append(itemId);
    if( revision >= 0 ) {
      sb.append('-').append(revision);
    }
    sb.append(".msg");

    String finalPath = sb.toString();
    return new RelativePath(finalPath.substring(1, finalPath.length()));
  }

  public RelativePath getBlobPath(MailboxBlob mboxBlob)
  {
    MockMailboxBlob blob = (MockMailboxBlob)mboxBlob;
    return getBlobPath(
      blob.getMailbox().getId(),
      blob.getItemId(),
      blob.getRevision(),
      blob.volumeId()
    );
  }

  public short currentVolume()
  {
/* $if ZimbraVersion >= 8.0.0 $ */
    return VolumeManager.getInstance().getCurrentMessageVolume().getId();
/* $else$
    return Volume.getCurrentMessageVolume().getId();
   $endif$ */
  }


  public MailboxBlob copy(
    MockBlob src,
    Mailbox destMbox,
    int destItemId,
    int destRevision,
    String locator
  )
  {
    com.zextras.lib.vfs.File destinationFile = mStoreRoot.getRoot().resolveFile(
      getBlobPath(destMbox.getId(), destItemId, destRevision, currentVolume())
    );

    try
    {
      destinationFile.getParent().createRecursive();
      src.getVirtualFile().copy(destinationFile);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    catch (MissingWritePermissions missingWritePermissions)
    {
      throw new RuntimeException(missingWritePermissions);
    }

    MockBlob mockBlob;
    try
    {
      mockBlob = new MockBlob();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    mockBlob.setFile(destinationFile);

    MockStagedBlob mockStagedBlob = new MockStagedBlob(destMbox, mockBlob);

    return new MockMailboxBlob(
      destMbox,
      destItemId,
      destRevision,
      locator,
      mockStagedBlob
    );
  }

  public MailboxBlob copy(MailboxBlob srcOr, Mailbox destMbox, int destItemId, int destRevision)
  {
    return copy(
      ((MockMailboxBlob)srcOr).getMockStagedBlob().getMockBlob(),
      destMbox,
      destItemId,
      destRevision,
      srcOr.getLocator()
    );
  }

  public MailboxBlob link(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision)
  {
    MailboxBlob newBlob = copy(
      ((MockStagedBlob) src).getMockBlob(),
      destMbox,
      destItemId,
      destRevision,
      String.valueOf(currentVolume())
    );
    return newBlob;
  }

  public MailboxBlob link(MailboxBlob src, Mailbox destMbox, int destItemId, int destRevision)
    throws IOException, ServiceException
  {
    MailboxBlob newBlob = copy(
      ((MockMailboxBlob) src).getMockStagedBlob().getMockBlob(),
      destMbox,
      destItemId,
      destRevision,
      String.valueOf(currentVolume())
    );
    return newBlob;
  }

  public MailboxBlob renameTo(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision)
  {
    MailboxBlob newBlob = copy(
      ((MockStagedBlob) src).getMockBlob(),
      destMbox,
      destItemId,
      destRevision,
      String.valueOf(currentVolume())
    );

    try
    {
      ((MockStagedBlob) src).getMockBlob().getVirtualFile().remove();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    catch (MissingWritePermissions missingWritePermissions)
    {
      throw new RuntimeException(missingWritePermissions);
    }
    return newBlob;
  }

  public boolean delete(Blob blob) throws IOException
  {
    try
    {
      ((MockBlob)blob).getVirtualFile().remove();
    }
    catch (MissingWritePermissions missingWritePermissions)
    {
      throw new IOException(missingWritePermissions);
    }
    return true;
  }

  public boolean delete(StagedBlob staged)
  {
    try
    {
      ((MockStagedBlob)staged).getMockBlob().getVirtualFile().remove();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    catch (MissingWritePermissions missingWritePermissions)
    {
      throw new RuntimeException(missingWritePermissions);
    }
    return true;
  }

  public boolean delete(MailboxBlob mblob) throws IOException {
    mStoreRoot.getRoot().resolveFile(
      getBlobPath(mblob)
    );
    return true;
  }

  public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator)
  {
    RelativePath path = getBlobPath(mbox.getId(), itemId, revision, Short.valueOf(locator));

    MockBlob mockBlob;
    try
    {
      mockBlob = new MockBlob();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    mockBlob.setFile(mStoreRoot.getRoot().resolveFile(path));

    MockStagedBlob mockStagedBlob = new MockStagedBlob(
      mbox,
      mockBlob
    );

    return new MockMailboxBlob(mbox, itemId, revision, locator, mockStagedBlob);
  }

  public InputStream getContent(MailboxBlob mblob) throws IOException {
    return mblob.getLocalBlob().getInputStream();
  }

  public InputStream getContent(Blob blob) throws IOException {
    return blob.getInputStream();
  }

  public boolean deleteStore(Mailbox mbox) throws IOException
  {
    throw new UnsupportedOperationException();
  }

/* $if ZimbraVersion >= 7.2.1 $ */
  public boolean deleteStore(Mailbox mbox, Iterable<MailboxBlob.MailboxBlobInfo> mblobs) throws IOException
  {
    throw new UnsupportedOperationException();
  }
/* $else$
  public boolean deleteStore(Mailbox mbox, Iterable<MailboxBlob> blobs)
  {
    throw new UnsupportedOperationException();
  }
 $endif$ */

  static java.io.File sNonExistingPath = new java.io.File("/tmp/i/dont/exist");

  private static class MockBlob extends Blob
  {
    private com.zextras.lib.vfs.File mFile;

    MockBlob() throws IOException
    {
      super(File.createTempFile("fakestore",".tmp"));
    }

    public void setFile(com.zextras.lib.vfs.File file)
    {
      mFile = file;
    }

    public com.zextras.lib.vfs.File getVirtualFile()
    {
      return mFile;
    }

    public InputStream getInputStream() throws IOException
    {
      try
      {
        return mFile.openInputStreamWrapper();
      }
      catch (MissingReadPermissions missingReadPermissions)
      {
        throw new IOException(missingReadPermissions);
      }
    }

    public long getRawSize()
    {
      return mFile.size();
    }
  }

  private class MockStagedBlob extends StagedBlob
  {
    public MockBlob getMockBlob()
    {
      return mMockBlob;
    }

    private final MockBlob mMockBlob;

    MockStagedBlob(Mailbox mbox, MockBlob mockBlob)
    {
      super(mbox, "xxx", 123);
      mMockBlob = mockBlob;
    }

    public String getStagedLocator()
    {
      return getLocator();
    }

    public String getLocator()
    {
      return "1";
    }
  }

  @SuppressWarnings("serial")
  private class MockMailboxBlob extends MailboxBlob
  {
    public MockStagedBlob getMockStagedBlob()
    {
      return mMockStagedBlob;
    }

    private final MockStagedBlob mMockStagedBlob;

    MockMailboxBlob(Mailbox mbox, int itemId, int revision, String locator, MockStagedBlob mockStagedBlob)
    {
      super(mbox, itemId, revision, locator);
      mMockStagedBlob = mockStagedBlob;
    }

    public Blob getLocalBlob() throws IOException
    {
      return mMockStagedBlob.getMockBlob();
    }

    public short volumeId()
    {
      return Short.valueOf(getLocator());
    }
  }

  private static MockBlob createMockBlob()
  {
    try
    {
      return new MockBlob();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private class MockBlobBuilder extends BlobBuilder
  {
    private ByteArrayOutputStream out;

    protected MockBlobBuilder()
    {
      super(createMockBlob());
    }

    protected OutputStream createOutputStream(java.io.File file) throws FileNotFoundException
    {
      assert out == null : "Output stream already created";
      out = new ByteArrayOutputStream();
      return out;
    }

    protected FileChannel getFileChannel()
    {
      return null;
    }

    public Blob finish() throws IOException, ServiceException
    {
      MockBlob mockblob = (MockBlob) super.finish();

      if (out != null)
      {
        byte content[] = out.toByteArray();
        com.zextras.lib.vfs.File file = mStoreRoot.getRoot().resolveFile(
          UUID.randomUUID().toString()
        );
        mockblob.setFile(file);
        FileStreamWriter writer = null;
        try
        {
          writer = file.openWriterStream();
        }
        catch (MissingWritePermissions missingWritePermissions)
        {
          throw new IOException(missingWritePermissions);
        }
        try
        {
          writer.write(Unpooled.wrappedBuffer(content));
        }
        finally
        {
          writer.close();
        }
        out = null;
      }

      return mockblob;
    }
  }
}