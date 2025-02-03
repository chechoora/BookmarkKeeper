package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkGroup;
import com.intellij.ide.bookmark.BookmarksManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import kotlin.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public final class BookmarkKeeperManager {
  private List<Container> bookmarks;
  private List<String> groupNames;

  public static BookmarkKeeperManager getInstance() {
    return ApplicationManager.getApplication().getService(BookmarkKeeperManager.class);
  }

  public void saveBookmarks(Project project) {
    try {
      final BookmarksManager bookmarksManager = BookmarksManager.getInstance(project);
      if (bookmarksManager == null) return;

      // Save group names
      groupNames = bookmarksManager.getGroups()
          .stream()
          .map(BookmarkGroup::getName)
          .collect(Collectors.toList());

      bookmarks = bookmarksManager.getBookmarks()
          .stream()
          .map(bookmark -> {
            BookmarkGroup group = bookmarksManager.getGroups(bookmark)
                .stream()
                .findFirst()
                .orElse(null);
            return new Container(
                bookmarksManager.getType(bookmark),
                bookmark,
                group != null ? group.getName() : null
            );
          })
          .collect(Collectors.toList());
    } catch (Exception ignored) {
    }
  }

  public void loadBookmarks(Project project) {
    try {
      final BookmarksManager bookmarksManager = BookmarksManager.getInstance(project);
      if (bookmarksManager == null || bookmarks == null) {
        return;
      }
      bookmarksManager.remove();

      // Create a temporary default group
      final BookmarkGroup tempDefaultGroup = bookmarksManager.addGroup("__temp_default__", true);

      // Add all bookmarks to temp default group
      for (Container container : bookmarks) {
        bookmarksManager.add(container.bookmark, container.type);
      }

      // Recreate original groups
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

      for (Container container : bookmarks) {
        Optional<Bookmark> matchingBookmark = savedBookmarks.stream()
            .filter(b -> b.getAttributes().equals(container.bookmark.getAttributes()))
            .findFirst();

        if (matchingBookmark.isPresent()) {
          Bookmark bookmark = matchingBookmark.get();
          if (container.groupName != null && !container.groupName.isEmpty()) {
            BookmarkGroup targetGroup = groupMap.get(container.groupName);
            if (targetGroup != null) {
              targetGroup.add(bookmark, container.type, null);
            }
          }
        }
      }
      // Set the original default group
      if (originalDefaultGroup != null) {
        originalDefaultGroup.setDefault(true);
      }
      // Remove the temporary default group
      tempDefaultGroup.remove();
    } catch (Exception ignored) {
    }
  }
}