package io.github.askmeagain.bookmarkkeeper;

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

  public static BookmarkKeeperManager getInstance() {
    return ApplicationManager.getApplication().getService(BookmarkKeeperManager.class);
  }

  public void saveBookmarks(Project project) {
    try {
      final BookmarksManager bookmarksManager = BookmarksManager.getInstance(project);
      if (bookmarksManager == null) return;

      // Save bookmarks grouped by their group names
      bookmarkContainers = bookmarksManager.getGroups().stream()
          .map(group -> {
            // Get bookmarks for this group
            List<Container.BookmarkInfo> groupBookmarks = group.getBookmarks().stream()
                .map(bookmark -> new Container.BookmarkInfo(
                    bookmark,
                    bookmarksManager.getType(bookmark)
                ))
                .collect(Collectors.toList());

            return new Container(group.getName(), group.isDefault(), groupBookmarks);
          })
          .collect(Collectors.toList());

    } catch (Exception e) {
      // Silent catch
    }
  }

  public void loadBookmarks(Project project) {
    try {
      final BookmarksManager bookmarksManager = BookmarksManager.getInstance(project);
      if (bookmarksManager == null || bookmarkContainers == null) {
        return;
      }
      bookmarksManager.remove();

      // Load Saved Groups
      for (Container container : bookmarkContainers) {
        // Create a new group
        BookmarkGroup group = bookmarksManager.addGroup(container.groupName, container.isDefault);
        if (group == null) {
          continue;
        }
        // Load bookmarks for this group
        for (Container.BookmarkInfo bookmarkInfo : container.bookmarks) {
          group.add(bookmarkInfo.bookmark, bookmarkInfo.type, null);
        }
      }

    } catch (Exception ignored) {
    }
  }
}