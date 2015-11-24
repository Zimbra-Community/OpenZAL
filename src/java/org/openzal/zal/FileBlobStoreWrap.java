package org.openzal.zal;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.store.file.VolumeMailboxBlob;
import com.zimbra.cs.store.file.VolumeStagedBlob;
/* $if ZimbraVersion < 8.0.0 $
import com.zimbra.cs.store.StorageCallback;
/* $endif $ */

import java.io.IOException;
import java.io.InputStream;

interface FileBlobStoreWrap
{
    void startup() throws IOException, ServiceException;

    void shutdown();

    boolean supports(Object feature);

    BlobBuilder getBlobBuilder() throws IOException, ServiceException;

    Blob storeIncoming(InputStream in, boolean storeAsIs) throws IOException, ServiceException;

    Blob storeIncoming(InputStream in, Object callback, boolean storeAsIs) throws IOException, ServiceException;

    Blob storeIncoming(InputStream in, long sizeHint, Object callback, boolean storeAsIs) throws IOException, ServiceException;

    VolumeStagedBlob stage(InputStream in, long actualSize, Mailbox mbox) throws IOException, ServiceException;

    VolumeStagedBlob stage(InputStream in, long actualSize, Object callback, Mailbox mbox) throws IOException, ServiceException;

    VolumeStagedBlob stage(Blob blob, Mailbox mbox) throws IOException;

    VolumeMailboxBlob copy(MailboxBlob src, Mailbox destMbox, int destItemId, int destRevision)
    throws IOException, ServiceException;

    VolumeMailboxBlob copy(Blob src, Mailbox destMbox, int destItemId, int destRevision, short destVolumeId)
    throws IOException, ServiceException;

    VolumeMailboxBlob link(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision)
    throws IOException, ServiceException;

    VolumeMailboxBlob link(Blob src, Mailbox destMbox, int destItemId, int destRevision, short destVolumeId)
    throws IOException, ServiceException;

    VolumeMailboxBlob renameTo(StagedBlob src, Mailbox destMbox, int destItemId, int destRevision)
    throws IOException, ServiceException;

    boolean delete(MailboxBlob mblob) throws IOException;

    boolean delete(StagedBlob staged) throws IOException;

    boolean delete(Blob blob) throws IOException;

    MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator) throws ServiceException;

    InputStream getContent(MailboxBlob mboxBlob) throws IOException;

    InputStream getContent(Blob blob) throws IOException;

    boolean deleteStore(Mailbox mbox, Iterable blobs) throws IOException, ServiceException;

    boolean deleteStore(Mailbox mbox) throws IOException, ServiceException;
}