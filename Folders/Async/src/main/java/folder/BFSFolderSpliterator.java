package folder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * In conjunction with {@link StreamSupport#stream} and {@link
 * Spliterators#spliterator} this class creates a sequential or
 * parallel stream of {@link Dirent} objects from a
 * recursively-structured directory folder.
 */
public class BFSFolderSpliterator
       extends Spliterators.AbstractSpliterator<Dirent> {
    /**
     * Iterator traverses the folder contents one entry at a time.
     */
    private final Iterator<Dirent> mIterator;
        
    /**
     * Constructor initializes the fields and super class.
     */
    BFSFolderSpliterator(Folder folder) {
        super(folder.getSize(), NONNULL + IMMUTABLE);

        // Initialize the iterator.
        mIterator = new BFSIterator(folder);
    }

    /**
     * Attempt to advance the {@link Spliterator} by one {@link
     * Dirent}.
     */
    public boolean tryAdvance(Consumer<? super Dirent> action) {
        // If there's a Dirent available.
        if (mIterator.hasNext()) {
            // Obtain and accept the current Dirent.
            action.accept(mIterator.next());
            // Keep going.
            return true;
        } else
            // Bail out.
            return false;
    }

    /**
     * This iterator traverses each element in the {@link Folder}.
     */
    private static class BFSIterator
            implements Iterator<Dirent> {
        /**
         * The current Dirent to process.
         */
        private Dirent mCurrentEntry;

        /**
         * The list of (sub)folders to process.
         */
        private final List<Dirent> mFoldersList;

        /**
         * The list of documents to process.
         */
        private final List<Dirent> mDocsList;

        /**
         * Constructor initializes the fields.
         */
        BFSIterator(Folder rootFolder) {
            // Make the rootFolder the current entry. 
            mCurrentEntry = rootFolder;

            // Add all the subfolders in the rootFolder.
            mFoldersList = new ArrayList<>(rootFolder.getSubFolders());

            // Add all the document sin the rootFolder.
            mDocsList = new ArrayList<>(rootFolder.getDocuments());
        }

        /**
         * @return True if the iterator can continue, false if it's at
         *         the end
         */
        public boolean hasNext() {
            // See if we need to refresh the current entry.
            if (mCurrentEntry == null) {
                // See if there are any subfolders left to process.
                if (mFoldersList.size() > 0) {
                    // If any subfolders are left then pop the one at
                    // the end and make it the current entry.
                    mCurrentEntry =
                        mFoldersList.remove(mFoldersList.size() - 1);

                    // Add any/all subfolders from the new current
                    // entry to the end of the subfolders list.
                    mFoldersList.addAll(mCurrentEntry.getSubFolders());

                    // Add any/all documents from the new current
                    // entry to the end of the documents list.
                    mDocsList.addAll(mCurrentEntry.getDocuments());
                }
                // See if there are any documents left to process.
                else if (mDocsList.size() > 0) 
                    // Pop the document at the end of the list off and
                    // make it the current entry.
                    mCurrentEntry = mDocsList.remove(mDocsList.size() - 1);
            }

            // Return false if there are no more entries, else true.
            return mCurrentEntry != null;
        }

        /**
         * @return The next unseen entry in the {@link Folder}
         */
        public Dirent next() {
            // Store the current entry.
            Dirent nextDirent = mCurrentEntry;

            // Reset current entry to null.
            mCurrentEntry = null;
            
            // Return the current entry.
            return nextDirent;
        }
    }
}
