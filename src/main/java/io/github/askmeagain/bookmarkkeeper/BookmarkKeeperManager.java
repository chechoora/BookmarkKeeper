package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkGroup;
import com.intellij.ide.bookmark.BookmarksManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.*;
import java.util.stream.Collectors;

@Service
public final class BookmarkKeeperManager {
  private List<Container> bookmarkContainers;
  private List<String> groupNames;

  public static BookmarkKeeperManager getInstance() {
    return ApplicationManager.getApplication().getService(BookmarkKeeperManager.class);
  }

  public void saveBookmarks(Project project) {
    try {
      final BookmarksManager bookmarksManager = BookmarksManager.getInstance(project);
      if (bookmarksManager == null) return;

      // Save all group names in order
      groupNames = bookmarksManager.getGroups().stream()
          .map(BookmarkGroup::getName)
          .collect(Collectors.toList());

      // Save bookmarks with all their group names
      bookmarkContainers = bookmarksManager.getBookmarks()
          .stream()
          .map(bookmark -> {
            // Get all group names for this bookmark
            List<String> bookmarkGroupNames = bookmarksManager.getGroups(bookmark)
                .stream()
                .map(BookmarkGroup::getName)
                .collect(Collectors.toList());

            return new Container(
                bookmarksManager.getType(bookmark),
                bookmark,
                bookmarkGroupNames
            );
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      // Log the exception if needed
      System.err.println("Error saving bookmarks: " + e.getMessage());
    }
  }

  public void loadBookmarks(Project project) {
    try {
      final BookmarksManager bookmarksManager = BookmarksManager.getInstance(project);
      if (bookmarksManager == null || bookmarkContainers == null) {
        return;
      }

      // Remove existing bookmarks
      bookmarksManager.remove();

      // Recreate original groups in the saved order
      Map<String, BookmarkGroup> groupMap = new HashMap<>();
      BookmarkGroup originalDefaultGroup = null;
      for (String groupName : groupNames) {
        BookmarkGroup newGroup = bookmarksManager.addGroup(groupName, false);
        if (newGroup != null) {
          groupMap.put(groupName, newGroup);

          // Find the original default group (typically project name group)
          if (groupName.equals(project.getName())) {
            originalDefaultGroup = newGroup;
          }
        }
      }

      // Get saved bookmarks
      final List<Bookmark> savedBookmarks = bookmarksManager.getBookmarks();

      for (Container container : bookmarkContainers) {
        Optional<Bookmark> matchingBookmark = savedBookmarks.stream()
            .filter(b -> b.getAttributes().equals(container.bookmark.getAttributes()))
            .findFirst();

        if (matchingBookmark.isPresent()) {
          Bookmark bookmark = matchingBookmark.get();

          // Add to all groups this bookmark was originally in, respecting the saved order
          for (String groupName : groupNames) {
            if (container.groupNames.contains(groupName)) {
              BookmarkGroup targetGroup = groupMap.get(groupName);
              if (targetGroup != null) {
                targetGroup.add(bookmark, container.type, null);
              }
            }
          }
        }
      }

      // Set the original default group
      if (originalDefaultGroup != null) {
        originalDefaultGroup.setDefault(true);
      }
    } catch (Exception e) {
      // Log the exception if needed
      System.err.println("Error loading bookmarks: " + e.getMessage());
    }
  }

  // Optional method to check if bookmarks are available
  public boolean hasBookmarksSaved() {
    return bookmarkContainers != null && !bookmarkContainers.isEmpty();
  }

  // Optional method to clear saved bookmarks
  public void clearSavedBookmarks() {
    bookmarkContainers = null;
    groupNames = null;
  }
}